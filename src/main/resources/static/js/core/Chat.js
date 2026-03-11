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

//    async sendMessage(content, role, images, longTexts) {
//        const fragments = await window.projects.getContext(this.message.value);
//        this.currentMessage = new Message(
//            role,
//            content,
//            images,
//            longTexts,
//            []
//        );
//
//        this.chatView.appendMessage(this.currentMessage)
//        this.saveMessage(this.currentMessage);
//
//        this.requestBuilder.addMessage(this.currentMessage);
//        this.chatClient.sendStreamingMessage(this.requestBuilder);
//        this.message.value = "";
//    }

    async sendMessage(content, role, images, longTexts) {
        const fragments = await window.projects.getContext(content);
        const userMessage = new Message(role, content, images, longTexts, []);

        this.chatView.appendMessage(userMessage);
        this.saveMessage(userMessage);
        this.requestBuilder.addMessage(userMessage);

        await this._sendRequest();
        this.message.value = "";
    }

    async _sendRequest() {
        this.chatClient.sendStreamingMessage(this.requestBuilder);
    }

    async resendUserMessage(userMessage) {
        await this._sendRequest();
    }


    async rerunMessage(messageId) {
        const messages = this.requestBuilder.messages;
        const index = messages.findIndex(m => m.id === messageId);
        if (index === -1) return;

        const targetMessage = messages[index];

        if (targetMessage.role === 'assistant') {
            let userIndex = index - 1;
            while (userIndex >= 0 && messages[userIndex].role !== 'user') {
                userIndex--;
            }
            if (userIndex < 0) return;

            const userMessage = messages[userIndex];
            this.requestBuilder.messages = messages.slice(0, userIndex + 1);
            this.chatView.clearMessages();
            this.chatView.renderMessages(this.requestBuilder);
            await this._sendRequest();
        }
    }

}

document.addEventListener('DOMContentLoaded', function() {
    window.chat = new Chat();
});