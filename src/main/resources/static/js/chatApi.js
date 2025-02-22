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

    // getMessagesToSend() {
    //     let messagesToSend = [];
    //     for (let i = 0; i < this.messages.length; i++) {
    //         if (this.prompter.memory || i == this.messages.length - 1 || this.messages[i].cache) {
    //             messagesToSend.push(this.convertMessage(this.messages[i]));
    //         }
    //     }
    //     return messagesToSend;
    // }


    // saveMessage(message) {
    //     const saveMessageObject = {
    //         id: this.id,
    //         content: JSON.stringify(this.convertMessage(message)),
    //         cache: message.cache,
    //     };

    //     fetch('/history/save', {
    //         method: 'POST',
    //         headers: {
    //             'Content-Type': 'application/json'
    //         },
    //         credentials: 'include',
    //         body: JSON.stringify(saveMessageObject)
    //     })
    //         .then(response => {
    //             if (!response.ok) {
    //                 throw new Error('Network response was not ok');
    //             }
    //             return response.text();
    //         })
    //         .then(data => {

    //         })
    //         .catch(error => {
    //             console.error('There was a problem with the request:', error);
    //         });
    // }

    handleStream(stream) {
        const reader = stream.getReader();
        const decoder = new TextDecoder();

        window.chat.requestBuilder.addMessage(this.currentMessage);
        this.read(reader, decoder);
    }

    readChunk(decoder, value) {
        const chunk = decoder.decode(value, { stream: true });

        this.parser.parse(chunk);
        this.outputInput.textContent += chunk;
        this.parser.toHTML();
        //this.outputInput.innerHTML = this.parser.toHTML();
        this.currentMessage.appendText(chunk);
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

    // loadChat(id) {
    //     this.id = id;

    //     return fetch(`/history/get?chatId=${encodeURIComponent(this.id)}`, {
    //         method: 'GET',
    //         headers: {
    //             'Content-Type': 'application/json'
    //         }
    //     })
    //         .then(response => {
    //             if (!response.ok) {
    //                 throw new Error('Network response was not ok');
    //             }
    //             return response.json();
    //         })
    //         .then(data => {
    //             this.messages = data;
    //             this.messages = this.messages.sort((a, b) => {
    //                 const dateA = createDateFromComponents(a.createTime);
    //                 const dateB = createDateFromComponents(b.createTime);
    //                 return dateA - dateB;
    //             });

    //             this.messages = this.returnNormalizedMessages(this.messages);
    //             return this.messages;
    //         })
    //         .catch(error => {
    //             console.error('There has been a problem with your fetch operation:', error);
    //         });

    // }

    // returnNormalizedContent(messages) {
    //     let contentList = [];
    //     messages.forEach(message => {
    //         const content = JSON.parse(message.content);
    //         let text = "";
    //         let images = [];
    //         content.forEach(element => {
    //             if (element.type == "image_url") {
    //                 images.push(element.image_url.url);
    //             } else if (element.type == "") {
    //                 image = "";
    //             } else if (element.type == "text") {
    //                 text = element.text;
    //             }
    //         })
    //         contentList.push({
    //             role: content.role,
    //             text: text,
    //             images: images
    //         });

    //     });
    //     return contentList;
    // }

    // createBody() {
    //     const jsonBody = {
    //         model: this.model,
    //         max_tokens: this.maxTokens,
    //         temperature: this.temperature,
    //         messages: this.getMessagesToSend(),
    //         stream: this.stream
    //     };

    //     const jsonBodyConverted = 
    //     return jsonBody;
    // }

    // createJsonBody() {
    //     const jsonBody = {
    //         model: this.model,
    //         max_tokens: this.maxTokens,
    //         temperature: this.prompter.temperature,
    //         messages: this.getMessagesToSend(),
    //         stream: this.stream
    //     };

    //     const jsonBodyConverted = JSON.stringify(jsonBody);
    //     return jsonBodyConverted;
    // }

    // createMessages(message, documents) {
    //     let content = this.createContent(message, documents);
    //     this.addMessage("user", content);
    //     return this.createJsonBody();
    // }

    // createChat() {
    //     fetch('/history/create', {
    //         method: 'POST',
    //         headers: {
    //             'Content-Type': 'application/json'
    //         },
    //         credentials: 'include',
    //         body: this.name
    //     })
    //         .then(response => {
    //             if (!response.ok) {
    //                 throw new Error('Network response was not ok');
    //             }
    //             return response.text();
    //         })
    //         .then(data => {
    //             this.id = data;
    //         })
    //         .catch(error => {
    //             console.error('There was a problem with the request:', error);
    //         });
    // }

}