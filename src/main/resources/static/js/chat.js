
class Chat {
    constructor() {
        this.documents = [];
        this.images = [];
        this.chatInput = document.getElementById("input");
        this.documentsHtml = document.getElementById("documents");
        this.message = document.getElementById("input");
        this.chatMessages = document.getElementById("chatMessages");
        this.chatOptions = document.getElementById("chatOptions");
        this.chatHistory = document.getElementById("chatHistory");
        this.modelOptions = document.getElementById("modelOptions");
        this.chatSettings = document.getElementById("chatSettings");
        this.history = document.getElementById("history");
        this.editMessageView = new EditMessageView();
        this.requestBuilder = new RequestBuilder();
        this.chatClient = new ChatApi();
        this.blockedInput = false;
       
        this.addPasteListener();
        this.loadHistory();
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
            <div class="assitant-data">
                <div class="date"></div>
                <div class="duration" id="duration-${message.id}"></div>
            </div>
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
        this.updateDocumentsDisplay(); 
    }

    addImage(content, type) {
        let doc = {
            type: "image",
            imageType: type,
            content: content
        };
        this.documentsHtml.append("image:" + content.length + " ");
        this.documents.push(doc);
        this.updateDocumentsDisplay(); 
    }

    updateDocumentsDisplay() {
        const docElement = this.documentsHtml;
        let content = '';
        const imgElement = docElement.querySelector('img');
        if (imgElement) content += imgElement.outerHTML;
    
        const texts = [];
        for (const doc of this.documents) texts.push(`text: ${doc.content.length}`);
        for (const img of this.images) texts.push(`image: ${img.content.length}`);
    
        if (texts.length) content += ' ' + texts.join(' ');
        docElement.innerHTML = content;
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
                        window.chat.updateDocumentsDisplay(); 
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
                    window.chat.updateDocumentsDisplay(); 

                } else {
                }
            }

        });
    }

    async chat() {
        if (this.blockedInput == true)
            return;
        this.blockedInput = true;

        const fragments = await this.getContext();
        console.log(fragments);
        this.newMessage([]);
        this.createMessage(this.currentMessage);


        this.requestBuilder.addMessage(this.currentMessage);
        this.chatClient.sendStreamingMessage(this.requestBuilder);
        this.message.value = "";
    }

    async getContext(){
        if (window.settings.projectSwitch && window.settings.project) {
            const query = this.message.value;
            const fragments = await window.projects.searchSimilarFragments(window.settings.project, encodeURIComponent(query));
            return fragments;
        }
        return [];
    }

    newMessage(fragments) {
        this.currentMessage = new Message(
            "user",
            this.message.value,
            this.images,
            this.documents,
            fragments
        );
        this.images = [];
        this.documents = [];
        this.updateDocumentsDisplay(); 
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

   

   
}

window.chat = new Chat();
