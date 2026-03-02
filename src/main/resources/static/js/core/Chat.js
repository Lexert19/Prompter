class Chat {
    constructor() {
        this.documents = [];
        this.images = [];
        this.chatView = new ChatView();

        this.message = document.getElementById("input");
        this.chatMessages = document.getElementById("chatMessages");
        this.requestBuilder = new RequestBuilder();
        this.chatClient = new ChatClient();
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
        window.chatHistory.updateUrlForChat(id);
        this.requestBuilder = await window.chatHistory.getRequestBuilderForChat(this.session);
        window.chatClient.requestBuilder = this.requestBuilder;
        this.chatView.renderMessages(this.requestBuilder);
    }

    stopStreaming(){
        this.chatClient.stopStreaming();
    }

    async sendMessage(content, role, images, longTexts) {
        const fragments = await window.projects.getContext(this.message.value);
        this.currentMessage = new Message(
            role,
            content,
            images,
            longTexts,
            []
        );

        this.chatView.appendMessage(this.currentMessage)
        this.saveMessage(this.currentMessage);

        this.requestBuilder.addMessage(this.currentMessage);
        this.chatClient.sendStreamingMessage(this.requestBuilder);
        this.message.value = "";
    }

}

document.addEventListener('DOMContentLoaded', function() {
    window.chat = new Chat();
});