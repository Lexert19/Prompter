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
        this.copyButton = document.getElementById("edit-menu-copy");

        document.addEventListener("click", (event) => {
            if (!this.editMessageHtml.contains(event.target)) {
                this.hideEditMenu();
            }
        });
        this.messageId = "";
        this.addCachedButtonListener();
        this.addRerunButtonListener();
        this.addCopyButtonListener();
    }

    addRerunButtonListener() {
        this.rerunButton.addEventListener("click", () => {
            if (this.messageId) {
                Chat.instance().rerunMessage(this.messageId);
            }
            this.hideEditMenu();
        });
    }

    addCopyButtonListener() {
        if (!this.copyButton) return;
        this.copyButton.addEventListener("click", async () => {
            const message = Chat.instance().requestBuilder.messages.find(
                (msg) => msg.id === this.messageId
            );
            if (!message) {
                this.hideEditMenu();
                return;
            }

            const text = this.stripThinking(message.getText() || "");

            try {
                await navigator.clipboard.writeText(text);
                const originalText = this.copyButton.textContent;
                this.copyButton.textContent = t.t("copied") || "Copied!";
                setTimeout(() => {
                    this.copyButton.textContent = originalText;
                }, 1200);
            } catch (err) {
                console.error("Copy failed:", err);
                try {
                    const ta = document.createElement("textarea");
                    ta.value = text;
                    ta.style.position = "fixed";
                    ta.style.opacity = "0";
                    document.body.appendChild(ta);
                    ta.select();
                    document.execCommand("copy");
                    document.body.removeChild(ta);
                } catch (e) {
                    alert("Failed to copy: " + err.message);
                }
            }
            this.hideEditMenu();
        });
    }

    stripThinking(text) {
        if (!text) return "";
        let out = text.replace(/<think\b[^>]*>[\s\S]*?<\/think>/gi, "");
        out = out.replace(/<think\b[^>]*>[\s\S]*$/gi, "");
        out = out.replace(/<\/?think\b[^>]*>/gi, "");
        return out.replace(/^\s+|\s+$/g, "");
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