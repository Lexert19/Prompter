class ChatGPTApi extends ChatApi {
    constructor() {
        super();
        this.model = "gpt-4o-mini";
        this.url = "/chatgpt/chat";
        this.name = "chatgpt";
        this.buffer = '';


        this.models = [
            { name: "gpt-4o-mini", text: "gpt-4o-mini" },
            { name: "gpt-4o", text: "gpt-4o" },
            { name: "gpt-4-turbo", text: "gpt-4-turbo" },
            { name: "o1-mini-2024-09-12", text: "o1-mini" },
            { name: "o1-preview-2024-09-12", text: "o1-preview" },

        ];
    }

    convertMessage(message){
        return message.toChatgptMessage();
    }


    readChunk(decoder, value) {
        const chunk = decoder.decode(value, { stream: true });
        this.buffer += chunk;

        const lines = this.buffer.split('\n');
        
        this.buffer = lines.pop() || '';

        lines.forEach(line => {
            if (!line.trim()) return;
            try {
                const objectChunk = JSON.parse(line);
                if (objectChunk.object === "chat.completion.chunk") {
                    if (objectChunk.choices?.[0]?.delta?.content) {
                        window.chat.currentChat.outputInput.textContent += objectChunk.choices[0].delta.content;
                        this.currentMessage.text += objectChunk.choices[0].delta.content;
                    }                    
                }
            } catch (error) {
                window.chat.currentChat.outputInput.textContent += line;
                this.currentMessage.text += line;
                console.log(line);
            }
        });
    }

    flush() {
        if (this.buffer.trim()) {
            try {
                const objectChunk = JSON.parse(this.buffer);
                if (objectChunk.object === "chat.completion.chunk") {
                    if (objectChunk.choices?.[0]?.delta?.content) {
                        window.chat.currentChat.outputInput.textContent += objectChunk.choices[0].delta.content;
                        this.currentMessage.text += objectChunk.choices[0].delta.content;
                    }                    
                }
            } catch (error) {
                window.chat.currentChat.outputInput.textContent += this.buffer;
                this.currentMessage.text += this.buffer;
                console.log(this.buffer);
            }
            this.buffer = '';
        }
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

   



}