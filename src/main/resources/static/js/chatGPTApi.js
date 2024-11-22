class ChatGPTApi extends ChatApi {
    constructor() {
        super();
        this.model = "gpt-4o-mini";
        this.url = "/chatgpt/chat";
        this.name = "chatgpt";


        this.models = [
            { name: "gpt-4o-mini", text: "gpt-4o-mini" },
            { name: "gpt-4o", text: "gpt-4o" },
            { name: "o1-mini-2024-09-12", text: "o1-mini" },
            { name: "o1-preview-2024-09-12", text: "o1-preview" },

        ];
    }

    convertMessage(message){
        return message.toChatgptMessage();
    }


    readChunk(decoder, value) {
        const chunk = decoder.decode(value, { stream: true });
        const jsonObjects = chunk.split('\n');
        jsonObjects.forEach(json => {
            if (!json.trim()) return;
            try {
                const objectChunk = JSON.parse(json);
                if (objectChunk.object == "chat.completion.chunk") {
                    if (objectChunk.choices?.[0]?.delta?.content) {
                        window.chat.currentChat.outputInput.textContent += objectChunk.choices[0].delta.content;
                        this.currentMessage.text += objectChunk.choices[0].delta.content;
                    }                    
                }
            } catch (error) {
                console.log(error);
            }

        });
    }

    returnNormalizedMessages(messages) {
        let normalizedMessages = [];
        messages.forEach(message => {
            let normalizedMessage = new Message();
            normalizedMessage.readChatgptMessage(message);
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
    //                     type: "image_url",
    //                     image_url: {
    //                         url: "data:" + doc.imageType + ";base64," + doc.content,

    //                     },
    //                 }
    //             );
    //         }
    //     });

    //     allContent.push({
    //         type: "text",
    //         text: contentText,
    //     });

    //     return allContent;
    // }



}