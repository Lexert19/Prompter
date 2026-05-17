class EditMessageView {
    static _instance = null;

    static instance() {
        if (!EditMessageView._instance) {
            EditMessageView._instance = new EditMessageView();
        }
        return EditMessageView._instance;
    }

    constructor() {
        if (EditMessageView._instance) {
            return EditMessageView._instance;
        }
        EditMessageView._instance = this;

        this.editMessageHtml = document.getElementById("edit-menu");
        this.cachedButton = document.getElementById("edit-menu-cached");
        this.rerunButton = document.getElementById("edit-menu-rerun");

        document.addEventListener("click", (event) => {
            if (!this.editMessageHtml.contains(event.target)) {
                this.hideEditMenu();
            }
        });
        this.messageId = "";
        this.addCachedButtonListener();
        this.addRerunButtonListener();
    }

    addRerunButtonListener() {
        this.rerunButton.addEventListener("click", () => {
            if (this.messageId) {
                Chat.instance().rerunMessage(this.messageId);
            }
            this.hideEditMenu();
        });
    }

    showEditMenu(event, messageId) {
        this.editMessageHtml.style.position = "fixed";
        this.editMessageHtml.style.left = `${event.clientX}px`;
        this.editMessageHtml.style.top = `${event.clientY}px`;
        this.editMessageHtml.style.display = "block";
        this.messageId = messageId;
    }

    hideEditMenu() {
        this.editMessageHtml.style.display = "none";
    }

    addCachedButtonListener() {
        this.cachedButton.addEventListener("click", () => {
            const message = Chat.instance().requestBuilder.messages.find(
                (msg) => msg.id === this.messageId
            );
            if (message) {
                message.cache = !message.cache;
                document.getElementById(
                    `cached-${this.messageId}`
                ).textContent = message.cache ? "cached" : ""
            }
            this.hideEditMenu();
        });
    }
}

EditMessageView.instance();