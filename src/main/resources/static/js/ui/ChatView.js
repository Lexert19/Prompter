class ChatView {
    static _instance = null;

    static instance() {
        if (!ChatView._instance) {
            ChatView._instance = new ChatView();
        }
        return ChatView._instance;
    }

    constructor() {
        if (ChatView._instance) {
            return ChatView._instance;
        }
        this.chatView = document.getElementById("chatMessages");
        this.shouldAutoScroll = true;
        this._bindScroll();
        ChatView._instance = this;
    }

    _bindScroll() {
        if (!this.chatView) return;
        this.chatView.addEventListener('scroll', () => {
            const max = this.chatView.scrollTop - this.chatView.scrollTopMax;
            if (max <= -10) {
                this.shouldAutoScroll = true;
            }else{
                this.shouldAutoScroll = false;
            }
        }, { passive: true });
    }

    scrollToBottom() {
        this.chatView.scrollTop = this.chatView.scrollHeight;
        this.shouldAutoScroll = true;
    }


    appendMessage(message){
        const messageView = new MessageView(message);
        messageView.createHtmlElement(this.chatView);
        if(message.role == "user"){
            setTimeout(() => {
                this.scrollToBottom();
            }, 20);
        }
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