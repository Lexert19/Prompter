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
        this.requestBuilder = new RequestBuilder();
        this.chatClient = new ChatApi();
        this.blockedInput = false;

        const path = window.location.pathname;
        const parts = path.split('/').filter(Boolean);
        this.session = parts[0] === "chat" && parts[1] ? parts[1] : "";
        //this.addPasteListener();
        if(this.session != ""){
            this.loadChat(this.session);
        }

        window.chatHistory.loadHistory();
    }

    async saveMessage(content){
        if(!window.settings.activeHistory)
            return;
        if(this.session == ""){
            const id =  await window.chatHistory.createChatSession(content);
            this.session = id;
        }
        await window.chatHistory.saveMessage(this.session, content);
    }

    async loadChat(id) {
        this.session = id;

        try {
            this.requestBuilder = await window.chatHistory.getRequestBuilderForChat(this.session);
            this.clearMessages();
            this.requestBuilder.messages.forEach(message =>{
                const messageView = new MessageView(message);
                messageView.createHtmlElement(this.chatMessages, true);
            });

        } catch (error) {
            console.error("Error loading chat history", error);
        } finally {
            //this.setBlockedInput(false);
            //this.currentMessage.end = Date.now();
        }
    }


    async chat() {
        if (this.blockedInput == true){
            this.chatClient.stopStreaming();
            return;
        }
        //this.setBlockedInput(true);

        const fragments = await window.projects.getContext(this.message.value);
        console.log(fragments);
        this.newMessage([]);
        this.updateDocumentsDisplay(); 
        this.saveMessage(this.currentMessage);
        const messageView = new MessageView(this.currentMessage);
        messageView.createHtmlElement(this.chatMessages);
        //this.createMessage(this.currentMessage);


        this.requestBuilder.addMessage(this.currentMessage);
        this.chatClient.sendStreamingMessage(this.requestBuilder);
        this.message.value = "";
    }

    addMessageView(message){
        const messageView = new MessageView(message);
        messageView.createHtmlElement(this.chatMessages);
    }
    

    newMessage(fragments = [], ) {
        this.currentMessage = new Message(
            "user",
            this.message.value,
            this.images,
            this.documents,
            fragments
        );
        this.images = [];
        this.documents = [];
    }

    clearMessages() {
        document.getElementById("chatMessages").innerHTML = "";
    }

   

   
}

document.addEventListener('DOMContentLoaded', function() {
    window.chat = new Chat();
});