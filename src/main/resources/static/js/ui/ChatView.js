class ChatView {
    constructor() {
        this.chatView = document.getElementById("chatMessages");

    }


    appendMessage(message){
        const messageView = new MessageView(message);
        messageView.createHtmlElement(this.chatView);


    }


    renderMessages(requestBuilder){
        this.clearMessages();
        requestBuilder.messages.forEach(message =>{
            const messageView = new MessageView(message);
            messageView.createHtmlElement(this.chatView, true);
        });

        if (window.hljs) {
            document.querySelectorAll('#chatMessages pre code').forEach(block => {
                delete block.dataset.highlighted;
                hljs.highlightElement(block);
            });
        }
    }

    clearMessages() {
        document.getElementById("chatMessages").innerHTML = "";
    }
}