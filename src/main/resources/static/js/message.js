
class Message {
    constructor(role = "", text = "", images = [], documents = []) {
        this.role = role;
        this.content = [];
        this.id = Math.random().toString(36);
        this.cache = false;
        this.documents = [];
        this.text = "";

        this.setText(text, documents);
        images.forEach(img => {
            this.addBase64Image(img);
        });
    }

    setText(text, documents = [], cache = false) {
        let textContent = "";
        this.documents = documents;
        this.text = text;
        documents.forEach(doc => textContent += "<document>" + doc + "</document>");

        this.content.push({
            type: "text",
            text: textContent + text,
            cache: cache
        });

        return this;
    }

    appendText(text) {
        const textItem = this.content.find(item => item.type === "text");
        if (textItem) {
            textItem.text += text;
        }
    }

    getText() {
        //return this.content.find(item => item.type === "text")?.text;
        return this.text;
    }

    addImageUrl(url, mediaType, cache = false) {
        this.content.push({
            type: "image_url",
            url: url,
            mediaType: mediaType,
            cache: cache
        });
        return this;
    }

    addBase64Image(data, cache = false) {
        const regex = /^data:(image\/\w+);base64,(.+)$/;
        const matches = data.match(regex);

        if (matches) {
            const mediaType = matches[1];
            const base64Data = matches[2];

            this.content.push({
                type: "image",
                mediaType: mediaType,
                data: base64Data,
                cache: cache
            });
        } else {
            throw new Error('Invalid base64 image data');
        }

        return this;
    }



    getHtmlImages() {
        return this.content
            .filter(item => ["image_url", "image"].includes(item.type))
            .map(item => {
                if (item.type === "image_url") {
                    return `<img class="image" src="${item.url}" alt="Uploaded image">`;
                }
                return `<img class="image" src="data:${item.mediaType};base64,${item.data}" alt="Inline image">`;
            })
            .join("");
    }

    getHtmlFiles() {
        let documentsText = "";
        this.documents.forEach((document, index) => {
            documentsText += "Dokument: " + document.length;
        });
        return documentsText;
    }
}

class RequestBuilder {
    constructor() {
        this.provider = "OPENAI";
        this.model = "gpt-4o-mini";
        this.messages = [];
        this.maxTokens = 32000;
        this.temperature = 0.0;
        this.stream = true;
        this.id = Math.random().toString(36);
        this.url = "https://api.openai.com/v1/chat/completions";
        this.key = "";
    }

    toRequestJSON() {

        let messagesToInclude;
        if(window.CharacterData.settings.memory){
            messagesToInclude = [...this.messages];
        }else{
            messagesToInclude = [...this.messages.filter(message => message.cache === true), this.messages[this.messages.length - 1]];
        }

      
        return JSON.stringify({
            model: this.model,
            url: this.url,
            provider: this.provider,
            messages: messagesToInclude.map(message => ({
                role: message.role,
                content: message.content.map(content => this.mapContent(content))
              })),
            maxTokens: this.maxTokens,
            temperature: this.temperature,
            stream: this.stream,
            key: this.key
        });
    }

    mapContent(content) {
        const baseContent = {
            type: content.type,
            text: content.text || "",
            cache: content.cache || false
        };

        switch (content.type) {
            case 'image_url':
                return {
                    ...baseContent,
                    url: content.url,
                    mediaType: content.mediaType
                };

            case 'image':
                return {
                    ...baseContent,
                    mediaType: content.mediaType,
                    data: content.data
                };

            default:
                return baseContent;
        }
    }

    addMessage(message) {
        this.messages.push(message);
        return this;
    }
}