class ChatApi {
    constructor() {
        this.url = "/client/chat";
        this.parser = new HtmlParser();
        this.firstReason = false;
    }

    getProvider(){
        return window.settings.provider;
    }

    newMessage(){
        this.currentMessage = new Message("assistant");

        this.outputInput = window.chat.createMessage(this.currentMessage);
    }

    sendStreamingMessage(request) {
        console.log(request.toRequestJSON());
        this.newMessage();
        fetch(this.url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: request.toRequestJSON()
        }).then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.body;
        })
            .then(this.handleStream.bind(this))
            .catch(error => {
                console.error('Error fetching data:', error);
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
           
        }catch(error){
            console.debug(chunk);
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
                window.chat.blockedInput = false;
                this.parser.parse("\n\n");
                return;
            }

            this.readChunk(decoder, value);

            this.read(reader, decoder);
        }).catch(error => {
            window.chat.blockedInput = false;
            reader.releaseLock();
        });
    }

}