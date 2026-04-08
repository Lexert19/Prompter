class ChatClient {
    constructor() {
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
    }

    getProvider(){
        return window.settings.provider;
    }

    newMessage(){
        this.currentMessage = new Message("assistant");
        const messageView = new MessageView(this.currentMessage);
        this.outputInput = messageView.createHtmlElement(window.chat.chatMessages);
        this.parser.setRootElement(this.outputInput);
        //this.outputInput = window.chat.createMessage(this.currentMessage);
        //window.chat.startDurationCounter(this.currentMessage);
    }

    sendStreamingMessage(request) {
        this.parser.clear();
        this.jsonAccumulator = '';
        this.abortController = new AbortController();
        console.log(request.toRequestJSON());
        this.newMessage();
        fetchWithCsrf(this.url, {
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

        window.chat.requestBuilder.addMessage(this.currentMessage);
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
                    this.handleStreamEnd(reader);
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
//
//    readChunk(decoder, value) {
//
//        this.buffer += decoder.decode(value, { stream: true });
//
//        let lines = this.buffer.split('\n');
//        this.buffer = lines.pop();
//
//        for (let line of lines) {
//            this.processLine(line);
//        }
//    }

    processLine(line) {
        if (!line.trim()) return;

        if (line.startsWith('data:')) {
            line = line.slice(5).trim();
        }

        this.readChunkData(line);
    }

    readChunkData(rootNode){
        try{
            //event:error
//            if (!chunk.trim() || chunk === "[DONE]" ||
//            chunk.startsWith(': ping') ||
//            chunk.startsWith('event:error') ||
//            chunk.startsWith(': OPENROUTER PROCESSING')) {
//                return;
//            }
//
//            const rootNode = JSON.parse(chunk);
            if(rootNode.error){
                this.outputInput.textContent += error;
                return;
            }

            let content = "";
//            if(this.getProvider() == "ANTHROPIC"){
//                content = rootNode.delta.text;
//            } else if (rootNode.choices && rootNode.choices.length > 0) {
//                const choice = rootNode.choices[0];
//                if (choice?.delta?.content) content = choice.delta.content;
//            }  else if (rootNode.candidates?.[0]?.content?.parts?.[0]?.text) {
//                content = rootNode.candidates[0].content.parts[0].text;
//            }
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
            window.inputView.setIsBlocked(false);
            this.currentMessage.end = Date.now();
            window.chat.saveMessage(this.currentMessage);
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
//            this.processLine(this.buffer);
            this.buffer = '';
        }

        if (reader) {
            reader.releaseLock();
        }

        this.finalizeMessage();
    }

    finalizeMessage() {
        window.inputView.setIsBlocked(false);
        if (this.currentMessage) {
            this.currentMessage.end = Date.now();
            window.chat.saveMessage(this.currentMessage);
        }
    }



}

document.addEventListener('DOMContentLoaded', function() {
    window.chatClient = new ChatClient();
});