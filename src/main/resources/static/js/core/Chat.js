class Chat {
    static _instance = null;

    static instance() {
        if (!Chat._instance) {
            Chat._instance = new Chat();
        }
        return Chat._instance;
    }

    constructor() {
        if (Chat._instance) {
            return Chat._instance;
        }
        Chat._instance = this;

        this.chatView = ChatView.instance();
        this.message = document.getElementById("input");
        this.chatMessages = document.getElementById("chatMessages");
        this.chatClient = ChatClient.instance();

        const path = window.location.pathname;
        const parts = path.split('/').filter(Boolean);
        this.requestBuilder = new RequestBuilder();
        this.session = parts[0] === "chat" && parts[1] ? parts[1] : "";

        if (this.session) {
            this.loadChat(this.session);
        }

        this._bindNewChatButton();
    }

    _bindNewChatButton() {
        const btn = document.getElementById('newChatBtn');
        if (!btn) return;
        btn.addEventListener('click', () => this.startNewChat());
    }

    startNewChat() {
        ChatClient.instance().stopStreaming();
        this.session = "";
        this.requestBuilder = new RequestBuilder();
        this.chatView.clearMessages();
        if (window.InfoPanel && InfoPanel.instance) {
            InfoPanel.instance._promptsChatUuid = undefined;
        }
        if (window.location.pathname !== '/chat') {
            window.history.pushState({}, document.title, '/chat');
        }

    }

    stopStreaming() {
        ChatClient.instance().stopStreaming();
    }

    ensureSession() {
        if (this.session) return this.session;

        this.session = (window.crypto?.randomUUID)
            ? crypto.randomUUID()
            : 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
                const r = (Math.random() * 16) | 0;
                return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
            });

        window.history.pushState(
            { chatId: this.session },
            document.title,
            `/chat/${this.session}`
        );
        return this.session;
    }

    async sendMessage(content, role, images, longTexts) {
        this.ensureSession();

        const fragments = await Projects.instance().getContext(content);
        const userMessage = new Message(role, content, images, longTexts, fragments);

        this.chatView.appendMessage(userMessage);
        this.requestBuilder.addMessage(userMessage);

        await this._sendRequest(userMessage);
        this.message.value = "";

        History.instance().loadHistory();
    }

    async _sendRequest(userMessage) {
        this.ensureSession();
        ChatClient.instance().sendStreamingMessage(this.session, userMessage);
    }

    async resendUserMessage(userMessage) {
        await this._sendRequest(userMessage);
    }

    async rerunMessage(messageId) {
        const messages = this.requestBuilder.messages;
        const index = messages.findIndex(m => m.id === messageId);
        if (index === -1 || messages[index].role !== 'assistant') return;

        let userIndex = index - 1;
        while (userIndex >= 0 && messages[userIndex].role !== 'user') userIndex--;
        if (userIndex < 0) return;

        const userMessage = messages[userIndex];

        try {
            await fetchWithAuth(`/api/chat/session/${this.session}`, {
                method: 'DELETE', credentials: 'include'
            });
        } catch (_) {}

        this.requestBuilder.messages = [userMessage];
        this.chatView.clearMessages();
        this.chatView.renderMessages(this.requestBuilder);
        await this._sendRequest(userMessage);
        InputView.instance().setIsBlocked(true);
    }

    async loadChat(id) {
        ChatClient.instance().stopStreaming();
        this.session = id;
        if (window.InfoPanel && InfoPanel.instance) {
            InfoPanel.instance._promptsChatUuid = undefined;
        }
        if (window.location.pathname !== `/chat/${id}`) {
            window.history.pushState({ chatId: id }, document.title, `/chat/${id}`);
        }

        try {
            const response = await fetchWithAuth(
                `/api/chat/session/${id}/messages`,
                { credentials: 'include' }
            );
            if (!response.ok) {
                this.requestBuilder = new RequestBuilder();
                this.chatView.clearMessages();
                return;
            }

            const raw = await response.json();
            this.requestBuilder = new RequestBuilder();

            let lastRole = null;
            let lastAssistant = null;

            for (const m of raw) {
                if (m.role === 'tool') {
                    if (lastAssistant) {
                        lastAssistant.toolResults = lastAssistant.toolResults || [];
                        lastAssistant.toolResults.push({
                            id: m.toolCallId, name: m.toolName, content: m.toolResult
                        });
                    }
                    lastRole = 'tool';
                    continue;
                }

                if (m.role === 'assistant'
                    && lastAssistant
                    && (lastRole === 'assistant' || lastRole === 'tool')) {

                    if (m.toolCalls?.length) {
                        lastAssistant.toolCalls =
                            (lastAssistant.toolCalls || []).concat(m.toolCalls);
                    }
                    if (m.text?.trim()) {
                        lastAssistant.text += (lastAssistant.text ? '\n' : '') + m.text;
                        lastAssistant.appendText('');
                    }
                    if (m.end) lastAssistant.end = m.end;
                    lastRole = 'assistant';
                    continue;
                }

                let displayText = m.text || '';
                if (m.reasoning) {
                    displayText = `<think>\n${m.reasoning}\n</think>\n` + displayText;
                }

                const msg = new Message(m.role, displayText,
                    m.images || [], m.documents || []);
                msg.id    = m.id;
                msg.start = m.start;
                msg.end   = m.end;
                msg.cache = !!m.cached;
                msg.toolCalls   = m.toolCalls || null;
                msg.toolResults = [];
                this.requestBuilder.addMessage(msg);
                if (msg.role === 'assistant') lastAssistant = msg;
                lastRole = m.role;
            }

            this.chatView.clearMessages();
            this.chatView.renderMessages(this.requestBuilder);
            this.chatView.scrollToBottom();
        } catch (e) {
            console.error('[Chat] loadChat failed', e);
        }
    }
}

document.addEventListener('DOMContentLoaded', function() {
    if (!getToken()) {
        window.location.href = '/auth/login';
        return;
    }
    Chat.instance();
});