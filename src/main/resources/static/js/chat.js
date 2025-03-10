
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
        window.chatHistory.createChatSession().then(sessionId=>{
            this.session = sessionId;
        });
        window.chatHistory.loadHistory();
    }

    setBlockedInput(value){
        this.blockedInput = value;
        if(value){
            document.getElementById('send-icon').style.display = 'none'; 
            document.getElementById('stop-icon').style.display = 'block'; 
        }else{
            document.getElementById('send-icon').style.display = 'block'; 
            document.getElementById('stop-icon').style.display = 'none'; 
        }
    }

   
    appendText(event) {
        if (event.key === "Enter") {
            event.preventDefault();
            this.chat();
        }
    }

  
    

    loadChat(id, name) {
        // hidePages();
        // this.clearMessages();
        // this.currentChat = this.chats[name];
        // this.chatSettings.classList.add("active");
        // this.currentChat.loadChat(id)
        //     .then(messages => {
        //         messages.forEach(message => {
        //             this.createMessage(message);
        //         })
        //     })

        // this.currentChat.models.forEach(model => {
        //     modelOptions.innerHTML += `<option value="${model.name}">${model.text}</option>`;
        // });
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
            <div id="input-${message.id}" class="code-wrap">${escapeHtml(message.getText())}</div>
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
            texts.push(`<i class="fas fa-file-alt" style="margin-right: 5px;"></i> ${doc.length}`);
        });
        this.images.forEach(img =>{
            texts.push(`<i class="fas fa-image" style="margin-right: 5px;"></i> ${img.length}`);
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
            this.chatClient.stopStreaming();
            return;
        }
        this.setBlockedInput(true);

        const fragments = await window.projects.getContext(this.message.value);
        console.log(fragments);
        this.newMessage([]);
        this.createMessage(this.currentMessage);


        this.requestBuilder.addMessage(this.currentMessage);
        this.chatClient.sendStreamingMessage(this.requestBuilder);
        this.message.value = "";
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

   

   
}

window.chat = new Chat();
