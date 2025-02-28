class ChatApi {
    constructor() {
        this.url = "/client/chat";
        this.parser = new HtmlParser();
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

        try{
            console.log(chunk)
            const rootNode = JSON.parse(chunk); 
            const content = rootNode.choices[0].delta.content;

            this.parser.parse(content);
            this.outputInput.textContent += content;
            this.parser.toHTML();
            this.currentMessage.appendText(content);


        }catch(error){
            console.log(error);
        }
       
        //this.outputInput.innerHTML = this.parser.toHTML();
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