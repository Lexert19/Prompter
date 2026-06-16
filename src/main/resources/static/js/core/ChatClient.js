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

        this.url = "/client/chat";
        this.parser = new HtmlParser();
        this.firstReason = false;
        this.abortController = null;
        this.currentMessage = false;
        this.requestBuilder = new RequestBuilder();
        setInterval(this.renderHtml.bind(this), 100);
        this.rerender = false;
        this.buffer = '';
        this.jsonAccumulator = '';
        this.streamStartTime = 0;
        this.streamCharCount = 0;
    }

    getProvider(){
        return Settings.instance().provider;
    }

    newMessage(){
        this.currentMessage = new Message("assistant");
        this.streamStartTime = Date.now();
        this.streamCharCount = 0;
        const messageView = new MessageView(this.currentMessage);
        this.outputInput = messageView.createHtmlElement(Chat.instance().chatMessages);
        this.parser.setRootElement(this.outputInput);
    }

    sendStreamingMessage(request) {
        this.parser.clear();
        this.jsonAccumulator = '';
        this.abortController = new AbortController();
        console.log(request.toRequestJSON());
        this.newMessage();
        fetchWithAuth(this.url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: request.toRequestJSON(),
            signal: this.abortController.signal
        }).then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            this.requestBuilder.addMessage(this.currentMessage);
            return response.body;
        })
            .then(this.handleStream.bind(this))
            .catch(error => {
            this.outputInput.textContent += t.t("errorFetchingData") + ": " + error;
            this.finalizeMessage();
        });
    }

    handleStream(stream) {
        const reader = stream.getReader();
        const decoder = new TextDecoder();

        Chat.instance().requestBuilder.addMessage(this.currentMessage);
        this.read(reader, decoder);
    }

    read(reader, decoder) {
        reader.read().then(({ done, value }) => {
            if (done) {
                this.handleStreamEnd(reader);
                return;
            }
            this.buffer += decoder.decode(value, { stream: true });
            let events = this.buffer.split('\n\n');
            this.buffer = events.pop();
            for (let event of events) {
                this.processEvent(event);
            }

            this.read(reader, decoder);
        }).catch(error => {
            this.handleStreamEnd(reader);
        });
    }

    processEvent(event, reader) {
        const lines = event.split('\n');
        for (let line of lines) {
            if (line.startsWith('data:')) {
                const data = line.slice(5).trim();

                if (data === '[DONE]') {
                    //this.handleStreamEnd(reader);
                    return;
                }

                this.jsonAccumulator += data;
                this.tryParseAndProcess();
            }
        }
    }

    tryParseAndProcess() {
        try {
            const rootNode = JSON.parse(this.jsonAccumulator);

            this.readChunkData(rootNode);
            this.jsonAccumulator = '';
        } catch (e) {
        }
    }

    processLine(line) {
        if (!line.trim()) return;

        if (line.startsWith('data:')) {
            line = line.slice(5).trim();
        }

        this.readChunkData(line);
    }

    readChunkData(rootNode){
        try{
            if(rootNode.error){
                this.outputInput.textContent += rootNode.error;
                return;
            }

            let content = "";
            if (rootNode.choices?.[0]?.delta?.content) {
                content = rootNode.choices[0].delta.content;
            }
            else if (rootNode.delta?.text) {
                content = rootNode.delta.text;
            }
            else if (rootNode.candidates?.[0]?.content?.parts?.[0]?.text) {
                content = rootNode.candidates[0].content.parts[0].text;
            }

            content = this.appendReasoningContent(rootNode, content);

            if(!content)
            return;

            this.streamCharCount += content.length;

            this.parser.parse(content)
            this.currentMessage.appendText(content);

        }catch(error){
            console.error(chunk);
            console.error(error);
        }
    }

    renderHtml(){
        if(this.rerender){
            this.rerender = false;
        }
    }

    stopStreaming() {
        if (this.abortController) {
            this.abortController.abort();
            this.abortController = null;
            InputView.instance().setIsBlocked(false);
            this.currentMessage.end = Date.now();
        }
    }


    appendReasoningContent(rootNode, content){
        const choice = rootNode.choices?.[0];
        const openAiReasoning = rootNode.choices?.[0]?.delta?.reasoning_content;
        const geminiReasoning = rootNode.delta?.type === 'thought_summary'
            ? rootNode.delta.content?.text
            : "";
        let reasoningContent = openAiReasoning || geminiReasoning || "";
        if(!reasoningContent)
                reasoningContent="";
        if(content == ""){
            if(this.firstReason===false && reasoningContent != ""){
                this.firstReason=true;
                return "\n<think>\n"+reasoningContent;
            }
            return reasoningContent;
        } else {
            if(this.firstReason===true){
                this.firstReason=false;
                return reasoningContent + "\n</think>\n" + content;
            }
            return content;
        }
    }

    handleStreamEnd(reader) {
        if (this.buffer) {
            this.processEvent(this.buffer, reader);
            this.buffer = '';
        }

        if (reader) {
            reader.releaseLock();
        }

        this.finalizeMessage();
    }

    finalizeMessage() {
        InputView.instance().setIsBlocked(false);
        if (this.currentMessage) {
            this.currentMessage.end = Date.now();
            Chat.instance().saveMessage(this.currentMessage);
        }
    }
}
