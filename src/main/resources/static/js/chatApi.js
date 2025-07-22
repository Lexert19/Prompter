class ChatApi {
    constructor() {
        this.url = "/client/chat";
        this.parser = new HtmlParser();
        this.firstReason = false;
        this.abortController = null;
        this.currentMessage = false;
        this.requestBuilder = new RequestBuilder();
    }

    getProvider(){
        return window.settings.provider;
    }

    async continueMessage(){
        if (window.data.blockedInput == true){
            this.stopStreaming();
            return;
        }
        window.data.setBlockedInput(true);
        this.sendStreamingMessage(this.requestBuilder);

    }

    async chat(text, role){
        if (window.data.blockedInput == true){
            this.stopStreaming();
            return false;
        }
        if(text == ""){
            return false;
        }
        window.data.setBlockedInput(true);

        const fragments = await window.projects.getContext(text);


        this.currentMessage = new Message(
            role,
            text,
            window.data.images,
            window.data.documents,
            fragments
        );

        const messageView = new MessageView(this.currentMessage);
        messageView.createHtmlElement(document.getElementById("chatMessages"));
        window.data.clearDocumentsAndImages();

        this.requestBuilder.addMessage(this.currentMessage);
        this.sendStreamingMessage(this.requestBuilder);
    }



    newMessage(){
        this.currentMessage = new Message("assistant");
        const messageView = new MessageView(this.currentMessage);
        this.outputInput = messageView.createHtmlElement(window.chat.chatMessages);
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
        const chunk = decoder.decode(value, { stream: true });

        this.readJsonChunk(chunk);
       
        //this.outputInput.innerHTML = this.parser.toHTML();
    }

    readJsonChunk(chunksString){
        const chunks = chunksString.split("\n");
        chunks.forEach(chunk => {
           if (chunk.startsWith('data:')) {
                chunk = chunk.slice(5).trim();
           }
           this.readChunkData(chunk);
        });
    }

    readChunkData(chunk){
        try{
            if(!chunk.trim())
                return;
            const rootNode = JSON.parse(chunk);
            let error = rootNode.error;
            if(error){
                this.outputInput.textContent += error;
                return;
            }

            let content = "";
            if(this.getProvider() == "ANTHROPIC"){
                content = rootNode.delta.text;
            }else if(this.getProvider() == "DEEPSEEK"){
                content = this.deepseekParseContent(rootNode);
            }else{
                content = rootNode.choices[0].delta.content;
            }

            if(!content)
                return;

            this.parser.parse(content);
            const html = this.parser.toHTML();
            //this.outputInput.textContent += content;
            this.outputInput.innerHTML  = html;
            hljs.highlightAll();
            this.currentMessage.appendText(content);

        }catch(error){
            console.log(chunk);
            //console.log(error);
        }
    }

    stopStreaming() {
        if (this.abortController) {
            this.abortController.abort(); 
            this.abortController = null; 
            window.chat.setBlockedInput(false);
            this.currentMessage.end = Date.now();
            window.chat.saveMessage(this.currentMessage);
        }
    }

    deepseekParseContent(rootNode){
        let content = rootNode.choices[0].delta.content;

        if(content == null){
          
            content = rootNode.choices[0].delta.reasoning_content;
            if(this.firstReason == false){
                this.firstReason = true
                content = "<think>\n" + content;
            }
            return content;
        } else{
            if(this.firstReason == true){
                this.firstReason = false;
                content = "\n</think>\n" + content
            }
            return content;
        }
        
    }

    read(reader, decoder) {
        reader.read().then(({ done, value }) => {
            if (done) {
                reader.releaseLock();
                window.data.setBlockedInput(false);
                this.currentMessage.end = Date.now();
                window.chat.saveMessage(this.currentMessage);

                this.parser.parse("\n\n");
                return;
            }

            this.readChunk(decoder, value);

            this.read(reader, decoder);
        }).catch(error => {
            window.data.setBlockedInput(false);
            this.currentMessage.end = Date.now();
            window.chat.saveMessage(this.currentMessage);

            reader.releaseLock();
        });
    }

}

document.addEventListener('DOMContentLoaded', function() {
    window.chatApi = new ChatApi();
});