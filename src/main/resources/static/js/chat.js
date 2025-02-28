
class Chat {
    constructor() {
        this.documents = [];
        this.images = [];
        this.chatInput = document.getElementById("input");
        this.documentsHtml = document.getElementById("documents");
        this.message = document.getElementById("input");
        this.chatMessages = document.getElementById("chatMessages");
        this.chatOptions = document.getElementById("chatOptions");
        this.chatSettings = document.getElementById("chatSettings");
        this.chatHistory = document.getElementById("chatHistory");
        this.modelOptions = document.getElementById("modelOptions");
        this.history = document.getElementById("history");
        this.editMessageView = new EditMessageView();
        this.requestBuilder = new RequestBuilder();
        this.chatClient = new ChatApi();
        this.blockedInput = false;
        this.models = [
            { name: "gpt-4o-mini", text: "gpt-4o-mini", provider: "OPENAI", url: "https://api.openai.com/v1/chat/completions" },
            { name: "o3-mini", text: "o3-mini", provider: "OPENAI", url: "https://api.openai.com/v1/chat/completions" },

            { name: "gpt-4o", text: "gpt-4o", provider: "OPENAI", url: "https://api.openai.com/v1/chat/completions" },
            { name: "gpt-4-turbo", text: "gpt-4-turbo", provider: "OPENAI", url: "https://api.openai.com/v1/chat/completions" },
            { name: "claude-3-haiku-20240307", text: "Claude Haiku 3", provider: "ANTHROPIC", url: "https://api.anthropic.com/v1/messages" },
            { name: "claude-3-5-sonnet-20241022", text: "Claude Sonnet 3.5", provider: "ANTHROPIC", url: "https://api.anthropic.com/v1/messages" },
            { name: "claude-3-opus-20240229", text: "Claude Opus 3", provider: "ANTHROPIC", url: "https://api.anthropic.com/v1/messages" },
            {
                name: "meta-llama/Llama-3.3-70B-Instruct-Turbo",
                text: "meta-llama/Llama-3.3-70B-Instruct-Turbo",
                provider: "DEEPINFRA",
                url: "https://api.deepinfra.com/v1/openai/chat/completions"
            },
            {
                name: "deepseek-ai/DeepSeek-V3",
                text: "deepinfra/DeepSeek-V3",
                provider: "DEEPINFRA",
                url: "https://api.deepinfra.com/v1/openai/chat/completions"
            },
            {
                name: "deepseek-ai/DeepSeek-R1",
                text: "deepinfra/DeepSeek-R1",
                provider: "DEEPINFRA",
                url: "https://api.deepinfra.com/v1/openai/chat/completions"
            },
            {
                name: "deepseek-chat",
                text: "deepseek-v3",
                provider: "DEEPSEEK",
                url: "https://api.deepseek.com/chat/completions"
            },
            {
                name: "deepseek-reasoner",
                text: "deepseek-r1",
                provider: "DEEPSEEK",
                url: "https://api.deepseek.com/chat/completions"
            },
            {
                name: "gemini-2.0-flash-thinking-exp-01-21",
                text: "gemini-2.0-flash-thinking-exp-01-21",
                provider: "GEMINI",
                url: "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions"
            }
        ];
        this.addPasteListener();
        this.loadHistory();
        this.loadKeys();
        this.initSettings();
    }

    initSettings() {
        this.showSettings();
        //this.clearMessages();
        this.models.forEach(model => {
            modelOptions.innerHTML += `<option value="${model.name}">${model.text}</option>`;
        });
    }

    showSettings(){
        hidePages();
        this.chatSettings.classList.add("active");
    }

    appendText(event) {
        if (event.key === "Enter") {
            event.preventDefault();
            this.chat();
        }
    }

    loadHistory() {
        fetch('/history/all', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                return response.json();
            })
            .then(chatHistories => {
                chatHistories = chatHistories.sort((a, b) => {
                    const dateA = createDateFromComponents(a.createTime);
                    const dateB = createDateFromComponents(b.createTime);
                    return dateB - dateA;
                })
                chatHistories.forEach(historyIndex => {
                    this.addHtmlHistoryIndex(historyIndex);
                })
            })
            .catch(error => {
                console.error('There was a problem with the fetch operation:', error);
            });
    }

    addHtmlHistoryIndex(historyIndex) {
        let index = `<button onclick="window.chat.loadChat('${historyIndex.id}','${historyIndex.name}')">${historyIndex.name}: ${historyIndex.title}</button>`;
        this.history.innerHTML += index;
    }

    loadChat(id, name) {
        hidePages();
        this.clearMessages();
        this.currentChat = this.chats[name];
        this.chatSettings.classList.add("active");
        this.currentChat.loadChat(id)
            .then(messages => {
                messages.forEach(message => {
                    this.createMessage(message);
                })
            })

        this.currentChat.models.forEach(model => {
            modelOptions.innerHTML += `<option value="${model.name}">${model.text}</option>`;
        });
    }

    createMessage(message) {
        
        var receivedMessage = `
        <div id="${message.id}" class="message ${message.role}">
            ${message.getHtmlImages()}
            ${message.getHtmlFiles()}
            <div id="input-${message.id}" class="code-wrap">${this.escapeHtml(message.getText())}</div>
            <span id="cached-${message.id}">${message.cache ? "cached" : ""}<span>
        </div>
        `;

        this.chatMessages.insertAdjacentHTML('afterbegin', receivedMessage);
        const messageElement = document.getElementById(message.id);
        messageElement.addEventListener('contextmenu', (event) => {
            event.preventDefault();
            this.editMessageView.showEditMenu(event, message.id);
        });
        return document.getElementById("input-" + message.id);
    }

    addDocument(content) {
        let doc = {
            type: "text",
            content: content
        };
        this.documentsHtml.append("text:" + content.length + " ");
        this.documents.push(doc);
    }

    addImage(content, type) {
        let doc = {
            type: "image",
            imageType: type,
            content: content
        };
        this.documentsHtml.append("image:" + content.length + " ");
        this.documents.push(doc);
    }

    addPasteListener() {
        this.chatInput.addEventListener("paste", function (event) {
            const pastedText = event.clipboardData.getData("text");

            const items = event.clipboardData.items;
            let hasImage = false;

            for (let i = 0; i < items.length; i++) {
                const item = items[i];

                if (item.kind === "file" && item.type.startsWith("image/")) {
                    event.preventDefault();

                    const file = item.getAsFile();
                    const reader = new FileReader();

                    reader.onload = function (event) {
                        window.chat.images.push(event.target.result);
                    };

                    reader.readAsDataURL(file);
                    hasImage = true;
                    break;
                }
            }

            if (!hasImage) {
                if (pastedText.length > 2000) {
                    event.preventDefault();
                    window.chat.documents.push(pastedText);
                } else {
                }
            }

        });
    }

    chat() {
        if (this.blockedInput == true)
            return;
        this.blockedInput = true;
        this.newMessage();
        this.createMessage(this.currentMessage);
        this.requestBuilder.addMessage(this.currentMessage);
        this.chatClient.sendStreamingMessage(this.requestBuilder);
        this.message.value = "";
    }

    newMessage() {
        this.currentMessage = new Message(
            "user",
            this.message.value,
            this.images,
            this.documents
        );
        this.images = [];
        this.documents = [];
    }

    clearMessages() {
        this.modelOptions.innerHTML = "";
        document.getElementById("chatMessages").innerHTML = "";
    }

    escapeHtml(content) {
        try {
            return content
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#039;");
        } catch (error) {
            return "";
        }
    }

   

    loadKeys() {
        fetch('/account/keys', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch keys: ' + response.statusText);
                }
                return response.json();
            })
            .then(keys => {
                this.keys = keys;
                window.settings.key = this.keys["OPENAI"];
            })
            .catch(error => {
                console.error('Error loading keys:', error);
            });
    }
}

window.chat = new Chat();