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
        this.abortController = new AbortController();
        console.log(request.toRequestJSON());
        this.newMessage();
        fetch(this.url, {
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
            this.outputInput.textContent += 'Error fetching data:', error;
        });
    }

    handleStream(stream) {
        const reader = stream.getReader();
        const decoder = new TextDecoder();

        window.chat.requestBuilder.addMessage(this.currentMessage);
        this.read(reader, decoder);
    }



    readChunk(decoder, value) {

        this.buffer += decoder.decode(value, { stream: true });

        let lines = this.buffer.split('\n');
        this.buffer = lines.pop();

        for (let line of lines) {
            this.processLine(line);
        }
    }

    processLine(line) {
        if (!line.trim()) return;

        if (line.startsWith('data:')) {
            line = line.slice(5).trim();
        }

        this.readChunkData(line);
    }

    readChunkData(chunk){
        try{
            if(!chunk.trim())
            return;
            if(chunk == "[DONE]")
            return;
            if (chunk.startsWith(': ping')) {
                return;
            }
            if(chunk.startsWith(': OPENROUTER PROCESSING')){
                return;
            }
            const rootNode = JSON.parse(chunk);
            let error = rootNode.error;
            if(error){
                this.outputInput.textContent += error;
                return;
            }

            let content = "";
            if(this.getProvider() == "ANTHROPIC"){
                content = rootNode.delta.text;
            }else{
                if(rootNode.choices[0].delta.content)
                content += rootNode.choices[0].delta.content;
            }

            let reasoningContent = this.parseReasoningContent(rootNode);
            if(reasoningContent)
            content += reasoningContent;

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

    parseReasoningContent(rootNode){
        let content = rootNode.choices[0].delta.reasoning_content;

        if(content != null){
            if(this.firstReason == false){
                this.firstReason = true
                content = "<think>\n" + content;
            }
        } else{
            if(this.firstReason == true){
                this.firstReason = false;
                content = "\n</think>\n";
            }
        }
        return content;
    }

    read(reader, decoder) {
        reader.read().then(({ done, value }) => {
            if (done) {
                this.handleStreamEnd(reader);
                return;
            }

            this.readChunk(decoder, value);

            this.read(reader, decoder);
        }).catch(error => {
            this.handleStreamEnd(reader);
            //            window.data.setBlockedInput(false);
            //            this.currentMessage.end = Date.now();
            //            window.chat.saveMessage(this.currentMessage);
            //
            //            reader.releaseLock();
        });
    }


    handleStreamEnd(reader) {
        if (this.buffer) {
            this.processLine(this.buffer);
            this.buffer = '';
        }

        reader.releaseLock();
        window.inputView.setIsBlocked(false);
        this.currentMessage.end = Date.now();
        window.chat.saveMessage(this.currentMessage);
    }
}

document.addEventListener('DOMContentLoaded', function() {
    window.chatClient = new ChatClient();
});