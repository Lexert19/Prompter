
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


    updateDocumentsDisplay() {
        const docElement = this.documentsHtml;
        let content = '';
    
        const texts = [];
        this.documents.forEach(doc =>{
            texts.push(`text: ${doc.length}`);
        });
        this.images.forEach(img =>{
            texts.push(`image: ${img.length}`);
        });
    
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
        if (this.blockedInput == true){
            return;
        }
        this.changeToStopIcon();
        this.blockedInput = true;

        const fragments = await this.getContext();
        console.log(fragments);
        this.newMessage([]);
        this.createMessage(this.currentMessage);


        this.requestBuilder.addMessage(this.currentMessage);
        this.chatClient.sendStreamingMessage(this.requestBuilder);
        this.message.value = "";
    }

    changeToStopIcon(){
        const sendButton = document.getElementById('send-button');
        sendButton.innerHTML = `
        <div class="center">
            <svg xmlns="http://www.w3.org/2000/svg" height="16" width="12" viewBox="0 0 384 512">
                <path fill="#ffffff" d="M0 96C0 60.7 28.7 32 64 32H320c35.3 0 64 28.7 64 64V416c0 35.3-28.7 64-64 64H64c-35.3 0-64-28.7-64-64V96z" />
            </svg>
        </div>
        `;
    }

    changeToSendIcon(){
        const sendButton = document.getElementById('send-button');
        sendButton.innerHTML = `
        <div class="center">
                        <svg xmlns="http://www.w3.org/2000/svg" height="16" width="12" viewBox="0 0 384 512">
                            <path fill="#ffffff" d="M214.6 41.4c-12.5-12.5-32.8-12.5-45.3 0l-160 160c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0L160 141.2 160 448c0 17.7 14.3 32 32 32s32-14.3 32-32l0-306.7L329.4 246.6c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3l-160-160z" />
                        </svg>
                    </div>
        `;
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
