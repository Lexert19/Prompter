class Message {
    constructor() {
        this.cache = false;
        this.role = "";
        this.text = "";
        this.documents = [];
        this.images = [];
        this.createTime = [];
    }

    getHtmlImages(){
        let html = "";
        this.images.forEach(img =>{
            html  += "<img src=\""+img+"\" >"
        });
        return html;
    }

    getHtmlFiles(){
        let html = "";
        this.documents.forEach(doc =>{
            html  += `<p>plik: ${doc.length}</p>`;
        });
        return html;
    }

    getTextdocuments(text){
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = text;
    
        const documents = tempDiv.getElementsByTagName('document');
        let result = [];
    
        for (let i = 0; i < documents.length; i++) {
            result.push(documents[i].textContent); 
        }
    
        return result;
    }

    readChatgptMessage(message){
        this.cache = message.cache;
        this.role = message.role;
        this.createTime = message.createTime;
        let rawMessage = JSON.parse(message.content);
        this.role = rawMessage.role;


        rawMessage.content.forEach(content =>{
            if (content.type == "image_url") {
                this.images.push(content.image_url.url);
            }else if(content.type == "text"){
                const documentRegex = /<document>(.*?)<\/document>/gs;
                let match;
    
                while ((match = documentRegex.exec(content.text)) !== null) {
                    this.documents.push(match[1].trim());
                }
    
                this.text = content.text.replace(documentRegex, '').trim();
            }
        });
    }

    readClaudeMessage(message){
        this.cache = message.cache;
        this.createTime = message.createTime;
        let rawMessage = JSON.parse(message.content);
        this.role = rawMessage.role;

        rawMessage.content.forEach(content =>{
            if (content.type == "image") {
                this.images.push("data:"+content.source.media_type+";base64,"+content.source.data);
            }else if(content.type == "text"){
                this.text += content.text;
            }
        });
    }

    readLlamaMessage(message){
        this.cache = message.cache;
        let rawMessage = JSON.parse(message.content);
        this.createTime = message.createTime;
        this.role = rawMessage.role;

        this.text = rawMessage.content;

        // if(this.role == "assistant"){
        //     rawMessage.content.forEach(content =>{
        //         if(content.type == "text"){
        //             this.text += content.text;
        //         }
        //     });
        // }else{
        // }
    }

    toLlamaMessage(){
        let finalText = "";
        this.documents.forEach(doc => {
            finalText += `<document>${doc}</document>`;
        });
        finalText += this.text;


        // if(this.role == "assistant"){
        //     let content = [];
        //     content.push({
        //         type: "text",
        //         text: finalText
        //     });
        //     return {
        //         role: this.role,
        //         content: content
        //     };
        // }

        return {
            role: this.role,
            content: finalText
        };
    }

    toChatgptMessage(){
        let content = [];

        let finalText = "";
        this.documents.forEach(doc => {
            finalText += `<document>${doc}</document>`;
        });
        finalText += this.text;

        this.images.forEach(img =>{
            content.push({
                type: "image_url",
                image_url: {
                    url: img
                }
            });
        });

        content.push({
            type: "text",
            text: finalText
        });


        return {
            role: this.role,
            content: content
        };
    }



    toClaudeMessage() {
        let content = [];

        let finalText = "";
        this.documents.forEach(doc => {
            finalText += `<document>${doc}</document>`;
        });
        finalText += this.text;

        this.images.forEach(img =>{
            content.push({
                type: "image",
                source: {
                    type: "base64",
                    media_type: getMediaType(img),
                    data: getImageData(img)
                },
                ...(this.cache && { cache_control: { type: "ephemeral" } })
            });
        })

        content.push({
            type: "text",
            text: finalText
        });


        return {
            role: this.role,
            content: content
        };
    }

  

}