
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

        this.currentMessage = new Message();
        this.currentMessage.role = "user";


        this.blockedInput = false;
        this.addPasteListener();
        this.currentChat = "";

        this.chats = { chatgpt: new ChatGPTApi, claude: new ClaudeApi, llama: new LlamaApi };
        this.initView();
        this.loadHistory();
    }

    initView() {
        const chatNames = Object.keys(this.chats);
        chatNames.forEach((name, index) => {
            const selectedAttribute = index === 0 ? ' selected' : '';
            this.chatOptions.innerHTML += `<option value="${name}">${name}</option>`;
        });
    }

    initSettings() {
        this.clearMessages();
        hidePages();
        this.currentChat = this.chats[this.chatOptions.value];
        this.chatSettings.classList.add("active");
        this.currentChat.createChat();

        this.currentChat.models.forEach(model => {
            modelOptions.innerHTML += `<option value="${model.name}">${model.text}</option>`;
        });
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
        if (this.currentChat == "")
            return;
        if (this.blockedInput == true)
            return;
        this.blockedInput = true;

        this.newMessage();
        this.currentChat.saveMessage(this.currentMessage);


        this.createMessage(this.currentMessage);

        this.currentChat.sendStreamingMessage(this.currentMessage);


        this.message.value = "";
    }

    newMessage() {
        this.currentMessage = new Message();
        this.currentMessage.role = "user";
        this.currentMessage.text += this.currentChat.prompter.getPrompts();
        this.currentMessage.text += this.escapeHtml(this.message.value);

        this.currentMessage.cache = this.currentChat.prompter.cache;
        this.currentMessage.images = this.images;
        this.currentMessage.documents = this.documents;

        this.images = [];
        this.documents = [];
    }


    // createHtmlFiles() {
    //     let files = "";
    //     this.documents.forEach(doc => {
    //         if (doc.type == "image") {
    //             files += "<img src=\"data:" + doc.imageType + ";base64," + doc.content + "\">"
    //         } else if (doc.type == "text") {
    //             files += "<p>plik: " + doc.content.length + "</p>"
    //         }
    //     });
    //     return files;
    // }

    createMessage(message) {
        const uniqueId = `message-${Math.random().toString(36).substr(2, 8)}`;
        var receivedMessage = `
        <div class="message ${message.role}">
            ${message.getHtmlImages()}
            ${message.getHtmlFiles()}
            <pre><code class="code-wrap" id="${uniqueId}">${message.text}</code></pre>
            ${message.cache ? "cached" : ""}
        </div>
        `;

        this.chatMessages.insertAdjacentHTML('afterbegin', receivedMessage);
        return document.getElementById(uniqueId);
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

    changeModel(event) {
        window.chat.currentChat.model = event.target.value;
    }

    changeSettings(event) {
        switch (event.target.name) {
            case "memory":
                window.chat.currentChat.prompter.memory = event.target.checked;
                break;
            case "longDocument":
                window.chat.currentChat.prompter.longDocument = event.target.checked;
                break;
            case "cache":
                window.chat.currentChat.prompter.cache = event.target.checked;
                break;
            case "programmer":
                window.chat.currentChat.prompter.programmer = event.target.checked;
                break;
            case "chainOfThoughts":
                window.chat.currentChat.prompter.chainOfThoughts = event.target.checked;
                break;
            case "manySolutions":
                window.chat.currentChat.prompter.manySolutions = event.target.checked;
                break;
            case "temperature":
                window.chat.currentChat.prompter.temperature = event.target.value / 100;

        }
    }

}

window.chat = new Chat();