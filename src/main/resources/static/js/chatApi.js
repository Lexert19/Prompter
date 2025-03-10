class ChatApi {
    constructor() {
        this.url = "/client/chat";
        this.parser = new HtmlParser();
        this.firstReason = false;
        this.abortController = null; 
    }

    getProvider(){
        return window.settings.provider;
    }

    newMessage(){
        this.currentMessage = new Message("assistant");

        this.outputInput = window.chat.createMessage(this.currentMessage);
    }

    sendStreamingMessage(request) {
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
            this.outputInput.textContent += content;
            this.parser.toHTML();
            this.currentMessage.appendText(content);
            this.setDuration();
           
        }catch(error){
            console.debug(chunk);
        }
    }

    setDuration(){
        const durationElement = document.getElementById(`duration-${this.currentMessage.id}`);
        if(durationElement){
            const currentTime = Date.now();
            const elapsedTimeMs = currentTime - this.currentMessage.time;

            const elapsedTimeSec = (elapsedTimeMs / 1000).toFixed(1); 

            durationElement.textContent = `${elapsedTimeSec}s`; 
        }
    }

    stopStreaming() {
        if (this.abortController) {
            this.abortController.abort(); 
            this.abortController = null; 
            window.chat.setBlockedInput(false);
            window.chat.saveMessage(this.currentMessage);
            console.log('Strumieniowanie zostało zatrzymane');
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
                window.chat.setBlockedInput(false);
                window.chat.saveMessage(this.currentMessage);

                this.parser.parse("\n\n");
                return;
            }

            this.readChunk(decoder, value);

            this.read(reader, decoder);
        }).catch(error => {
            window.chat.setBlockedInput(false);
            window.chat.saveMessage(this.currentMessage);

            reader.releaseLock();
        });
    }

}