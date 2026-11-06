class ChatClient {
    static _instance = null;

    static instance() {
        if (!ChatClient._instance) {
            ChatClient._instance = new ChatClient();
        }
        return ChatClient._instance;
    }

    constructor() {
        if (ChatClient._instance) {
            return ChatClient._instance;
        }
        ChatClient._instance = this;

        this.parser = new HtmlParser();
        this.firstReason = false;
        this.abortController = null;
        this.currentMessage = null;
        this.buffer = '';
        this.streamCharCount = 0;
    }

    newMessage() {
        this.currentMessage = new Message("assistant");
        this.streamCharCount = 0;
        this.firstReason = false;
        const messageView = new MessageView(this.currentMessage);
        this.outputInput = messageView.createHtmlElement(Chat.instance().chatMessages);
        this.parser.setRootElement(this.outputInput);
    }

    buildSessionRequest(userMessage) {
        const settings = Settings.instance();
        let rawText = userMessage ? (userMessage.text || '') : '';

        if (settings.systemSwitch && settings.system) {
            const suffix = `{${settings.system}}`;
            if (rawText.endsWith(suffix)) rawText = rawText.slice(0, -suffix.length);
        }

        let text = '';
        if (userMessage?.context?.length) {
            userMessage.context.forEach(c => { text += `<context>${c}</context>`; });
        }
        if (userMessage?.documents?.length) {
            userMessage.documents.forEach(d => { text += `<document>${d}</document>`; });
        }
        text += rawText;

        const imageIds = (userMessage?.images || []).map(img =>
            typeof img === 'object' && img.id != null ? String(img.id) : String(img)
        );

        const body = {
            text,
            model: settings.model,
            provider: settings.provider,
            providerStrategy: settings.provider === 'OPENROUTER' ? 'OPENROUTER' : (settings.providerStrategy || settings.provider),
            url: settings.url,
            systemPrompt: settings.systemSwitch ? settings.system : null,
            ...(settings.useTools ? { enabledTools: settings.enabledTools || [] } : {}),
            useSharedKeys: !!settings.useSharedKeys,
            documents: userMessage?.documents || [],
            images: imageIds,
            start: userMessage?.start || null
        };

        if (settings.includeAdvancedParams) {
            body.temperature      = settings.temperature;
        }

        if (settings.thinkingEffort && settings.thinkingEffort !== 'lack') {
            body.reasoningEffort = settings.thinkingEffort;
        }

        if ((settings.provider || '').toUpperCase() === 'OPENROUTER') {
            const orderRaw = (settings.openRouterProviderOrder || '').trim();
            if (orderRaw) {
                const orderArray = orderRaw.split(',').map(s => s.trim()).filter(Boolean);
                if (orderArray.length > 0) {
                    body.providerConfig = {
                        order: orderArray,
                        allow_fallbacks: settings.openRouterAllowFallbacks !== false
                    };
                }
            }
        }

        return body;
    }

    sendStreamingMessage(chatUuid, userMessage) {
        if (!chatUuid) {
            console.error('[ChatClient] chatUuid is required for /api/chat/session');
            return;
        }

        this.parser.clear();
        this.buffer = '';
        this.abortController = new AbortController();
        this.newMessage();

        const url = `/api/chat/session/${chatUuid}/chat`;
        const body = this.buildSessionRequest(userMessage);

        fetchWithAuth(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(body),
            signal: this.abortController.signal
        }).then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            Chat.instance().requestBuilder.addMessage(this.currentMessage);
            return response.body;
        })
            .then(this.handleStream.bind(this))
            .catch(error => {
                if (error.name !== 'AbortError') {
                    this.outputInput.textContent +=
                        (t.t("errorFetchingData") || 'Error') + ": " + error.message;
                }
                this.finalizeMessage();
            });
    }

    handleStream(stream) {
        if (!stream) {
            this.finalizeMessage();
            return;
        }
        const reader = stream.getReader();
        const decoder = new TextDecoder();
        this.read(reader, decoder);
    }

    read(reader, decoder) {
        reader.read().then(({ done, value }) => {
            if (done) {
                this.handleStreamEnd(reader);
                return;
            }
            this.buffer += decoder.decode(value, { stream: true });
            const events = this.buffer.split('\n\n');
            this.buffer = events.pop();
            for (const event of events) this.processEvent(event);
            this.read(reader, decoder);
        }).catch(() => {
            this.handleStreamEnd(reader);
        });
    }

    processEvent(event) {
        const lines = event.split('\n');
        let eventType = 'message';
        const dataParts = [];

        for (const line of lines) {
            if (line.startsWith('event:')) {
                eventType = line.slice(6).trim();
            } else if (line.startsWith('data:')) {
                dataParts.push(line.slice(5).trim());
            }
        }
        if (dataParts.length === 0) return;

        const data = dataParts.join('\n');
        let parsed;
        try {
            parsed = JSON.parse(data);
        } catch (e) {
            return;
        }

        console.log(`[SSE EVENT] Typ: ${eventType}`, parsed);

        this.dispatchEvent(eventType, parsed);
    }

    dispatchEvent(eventType, data) {
        switch (eventType) {
            case 'token':
                if (data.text) {
                    let chunk = data.text;
                    if (this.firstReason === true) {
                        chunk = "\n</think>\n" + chunk;
                        this.firstReason = false;
                    }
                    this.streamCharCount += chunk.length;
                    this.parser.parse(chunk);
                    this.currentMessage.appendText(chunk);
                }
                break;

            case 'reasoning':
                if (data.text) {
                    let chunk = data.text;
                    if (this.firstReason === false) {
                        chunk = "\n<think>\n" + chunk;
                        this.firstReason = true;
                    }
                    this.streamCharCount += chunk.length;
                    this.parser.parse(chunk);
                    this.currentMessage.appendText(chunk);
                }
                break;

            case 'error':
                this.outputInput.textContent += (data.error || 'Unknown error');
                this.handleStreamEnd(null);
                break;

            case 'tool_call':
                this.currentMessage.toolCalls = this.currentMessage.toolCalls || [];
                this.currentMessage.toolCalls.push({
                    id: data.id, name: data.name, arguments: data.arguments
                });
                this._refreshCurrentMessage();
                break;

            case 'tool_result':
                this.currentMessage.toolResults = this.currentMessage.toolResults || [];
                this.currentMessage.toolResults.push({
                    id: data.id, name: data.name, content: data.content
                });
                this._refreshCurrentMessage();
                break;

            case 'tool_error':
                this.currentMessage.toolResults = this.currentMessage.toolResults || [];
                this.currentMessage.toolResults.push({
                    id: data.id, name: data.name, error: data.error
                });
                this._refreshCurrentMessage();
                break;
            case 'stats':
                this.currentMessage.stats = data;
                this._updateStatsDisplay();
                break;

            case 'done':
                this.currentMessage.stats = data;
                this.handleStreamEnd(null);
                break;

            default:
                break;
        }
    }

    _updateStatsDisplay() {
        if (!this.currentMessage) return;
        const s = this.currentMessage.stats;
        if (!s) return;

        const durationEl = document.getElementById(`duration-${this.currentMessage.id}`);
        const tpsEl      = document.getElementById(`tps-${this.currentMessage.id}`);

        if (durationEl && s.elapsedMs) {
            durationEl.textContent = `${(s.elapsedMs / 1000).toFixed(1)}s`;
        }

        if (tpsEl) {
            const prefix = s.inThinking ? 'thinking: ' : '';
            tpsEl.textContent = ` | ${prefix}${s.tps} tps`;
        }
    }

    _refreshCurrentMessage() {
        if (!this.currentMessage) return;

        const container = document.getElementById(`tool-counter-container-${this.currentMessage.id}`);
        if (container) {
            const view = new MessageView(this.currentMessage);
            container.innerHTML = view.renderToolCounter();
        }
    }

    stopStreaming() {
        if (this.abortController) {
            this.abortController.abort();
            this.abortController = null;
            InputView.instance().setIsBlocked(false);
            if (this.currentMessage) this.currentMessage.end = Date.now();
        }
    }

    handleStreamEnd(reader) {
        if (this.buffer) {
            this.processEvent(this.buffer);
            this.buffer = '';
        }
        if (reader) {
            try { reader.releaseLock(); } catch (e) {
            }
        }
        this.finalizeMessage();
    }

    finalizeMessage() {
        InputView.instance().setIsBlocked(false);
        if (this.currentMessage && this.currentMessage.end === null) {
            this.currentMessage.end = Date.now();
        }
    }
}