
class ClaudeApi extends ChatApi {
    constructor() {
        super();
        this.model = "claude-3-haiku-20240307";
        this.url = "/claude/chat";
        this.name = "claude";

        this.models = [
            { name: "claude-3-haiku-20240307", text: "Claude Haiku 3" },
            { name: "claude-3-5-sonnet-20241022", text: "Claude Sonnet 3.5" },
            { name: "claude-3-opus-20240229", text: "Claude Opus 3" }
        ];
    }

    convertMessage(message){
        return message.toClaudeMessage();
    }

    readChunk(decoder, value){
        const chunk = decoder.decode(value, { stream: true });
        const jsonObjects = chunk.split('\n');
        jsonObjects.map(json => {
            try {
                const objectChunk = JSON.parse(json);
                if (objectChunk.type == "content_block_delta") {
                    if(objectChunk.delta?.text != null){
                        window.chat.currentChat.outputInput.textContent += objectChunk.delta.text;
                        this.currentMessage.text += objectChunk.delta.text;
                    }
                  
                }
            } catch (error) {
               
            }

        });
    }

    // sendMessage(message, documents) {
    //     return fetch('/claude/chat', {
    //         method: 'POST',
    //         headers: {
    //             'Content-Type': 'application/json',
    //         },
    //         credentials: 'include',
    //         body: this.createBody(message, documents),
    //     })
    //         .then(response => {
    //             return response.text()
    //                 .then(text => {
    //                     try {
    //                         return JSON.parse(text);
    //                     } catch (error) {
    //                         return text;
    //                     }
    //                 })
    //         })
    //         .then(data => {
    //             console.log(data);
    //             this.addMessage("assistant", this.getResponse(data))
    //             return this.getResponse(data);
    //         })
    //         .catch((error) => {
    //             return String(error);

    //         });
    // }

    returnNormalizedMessages(messages) {
        let normalizedMessages = [];
        messages.forEach(message => {
            let normalizedMessage = new Message();
            normalizedMessage.readClaudeMessage(message);
            normalizedMessages.push(normalizedMessage);
        });
        return normalizedMessages;
    }


    // createContent(message, documents) {
    //     let allContent = [];

    //     let contentText = "";

    //     documents.forEach(doc => {
    //         if (doc.type == "text") {
    //             contentText += `<document>${doc.content}</document>`;
    //         }
    //     });


    //     contentText += this.prompter.getPrompts();
    //     contentText += message;


    //     documents.forEach(doc => {
    //         if (doc.type == "image") {
    //             allContent.push(
    //                 {
    //                     type: "image",
    //                     source: {
    //                         type: "base64",
    //                         media_type: doc.imageType,
    //                         data: doc.content
    //                     },
    //                     ...(this.prompter.cache && { cache_control: { type: "ephemeral" } })
    //                 }
    //             );
    //         }
    //     });

    //     allContent.push({
    //         type: "text",
    //         text: contentText,
    //         ...(this.prompter.cache && { cache_control: { type: "ephemeral" } })

    //     });

    //     return allContent;
    // }

   

  

    // getResponse(data) {
    //     try {
    //         return data.content[0].text;
    //     } catch (error) {
    //         return data;
    //     }
    // }


}

