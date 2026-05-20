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

        this.documents = [];
        this.images = [];
        this.chatView = new ChatView();

        this.message = document.getElementById("input");
        this.chatMessages = document.getElementById("chatMessages");
        this.requestBuilder = new RequestBuilder();
        this.chatClient = new ChatClient();
        this.blockedInput = false;

        const path = window.location.pathname;
        const parts = path.split('/').filter(Boolean);
        this.session = parts[0] === "chat" && parts[1] ? parts[1] : "";
        if(this.session != ""){
            this.loadChat(this.session);
        }

        History.instance().loadHistory();
    }

    async saveMessage(content){
        if(!Settings.instance().activeHistory)
        return;
        if(this.session == ""){
            const id = await History.instance().createChatSession(content);
            this.session = id;
        }
        await History.instance().saveMessage(this.session, content);
    }

    async loadChat(id) {
        this.session = id;
        History.instance().updateUrlForChat(id);
        this.requestBuilder = await History.instance().getRequestBuilderForChat(this.session);
        ChatClient.instance().requestBuilder = this.requestBuilder;
        this.chatView.renderMessages(this.requestBuilder);
    }

    stopStreaming(){
        ChatClient.instance().stopStreaming();
    }


    async sendMessage(content, role, images, longTexts) {
        const fragments = await Projects.instance().getContext(content);
        const userMessage = new Message(role, content, images, longTexts, []);

        this.chatView.appendMessage(userMessage);
        this.saveMessage(userMessage);
        this.requestBuilder.addMessage(userMessage);

        await this._sendRequest();
        this.message.value = "";
    }

    async _sendRequest() {
        ChatClient.instance().sendStreamingMessage(this.requestBuilder);
    }

    async resendUserMessage(userMessage) {
        await this._sendRequest();
    }


    async rerunMessage(messageId) {
        const messages = this.requestBuilder.messages;
        const index = messages.findIndex(m => m.id === messageId);
        if (index === -1) return;

        const targetMessage = messages[index];

        if (targetMessage.role === 'assistant') {
            let userIndex = index - 1;
            while (userIndex >= 0 && messages[userIndex].role !== 'user') {
                userIndex--;
            }
            if (userIndex < 0) return;

            const userMessage = messages[userIndex];
            this.requestBuilder.messages = messages.slice(0, userIndex + 1);
            this.chatView.clearMessages();
            this.chatView.renderMessages(this.requestBuilder);
            await this._sendRequest();
            InputView.instance().setIsBlocked(true);
        }
    }

}

document.addEventListener('DOMContentLoaded', function() {
    Chat.instance()
});