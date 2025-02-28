class ChatApi {
    constructor() {
        this.url = "/client/chat";
        this.parser = new HtmlParser();
        this.provider = "";
        this.firstReason = false;
    }

    newMessage(){
        this.currentMessage = new Message("assistant");

        this.outputInput = window.chat.createMessage(this.currentMessage);
    }

    sendStreamingMessage(request) {
        console.log(request.toRequestJSON());
        this.provider = request.provider;
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
            try{
                if(chunk.trim()){
                    const rootNode = JSON.parse(chunk);
                    let content = rootNode.choices[0].delta.content;
    
                    content = this.deepseekParseContent(content, rootNode);
        
                    this.parser.parse(content);
                    this.outputInput.textContent += content;
                    this.parser.toHTML();
                    this.currentMessage.appendText(content);
                }
               
            }catch(error){
                console.debug(chunk);
                console.debug(error);
                //this.outputInput.textContent += chunksString;
            }
        });
    }

    deepseekParseContent(content, rootNode){
        if(this.provider != "DEEPSEEK")
            return content;

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