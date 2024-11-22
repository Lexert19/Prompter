class LlamaApi extends ChatGPTApi{
    constructor(){
        super();
        this.model = "nvidia/llama-3.1-nemotron-70b-instruct";
        this.url = "/llama/chat";
        this.maxTokens = 1024;
        this.name = "llama";
        this.models = [
            {name: "nvidia/llama-3.1-nemotron-70b-instruct", text: "nvidia/llama-3.1-nemotron-70b-instruct"},
        ];
    }

    convertMessage(message){
        return message.toLlamaMessage();
    }

    createJsonBody() {
        const jsonBody = {
            model: this.model,
            max_tokens: this.maxTokens,
            temperature: this.temperature,
            top_p: 1,
            messages: this.getMessagesToSend(),
            stream: this.stream
        };

        const jsonBodyConverted = JSON.stringify(jsonBody);
        return jsonBodyConverted;
    }

    returnNormalizedMessages(messages) {
        let normalizedMessages = [];
        messages.forEach(message => {
            let normalizedMessage = new Message();
            normalizedMessage.readLlamaMessage(message);
            normalizedMessages.push(normalizedMessage);
        });
        return normalizedMessages;
    }

    // returnNormalizedContent(messages){
    //     let contentList = [];
    //     messages.forEach(message => {
    //         const content = JSON.parse(message.content);
    //         let text = "";
    //         if(content.role == "user"){
    //             text = content.content;
    //         }else{
    //             text = content.content[0].text;
    //         }

    //         contentList.push({
    //             role: content.role,
    //             text: text
    //         });
            
    //     });
    //     return contentList;
    // }

    // createContent(message, documents) {
    //     let contentText = "";

    //     documents.forEach(doc => {
    //         if (doc.type == "text") {
    //             contentText += `<document>${doc.content}</document>`;
    //         }
    //     });

    //     contentText += this.prompter.getPrompts();
    //     contentText += message;

    //     return contentText;
    // }
}