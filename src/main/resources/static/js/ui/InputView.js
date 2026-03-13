class InputView{
    constructor(){
        this.chatInput = document.getElementById("input");
        this.documentsHtml = document.getElementById("documents");
        this.images = [];
        this.longTexts = [];
        this.addPasteListener();
        this.isBlocked = false;
    }

    removeImage(index) {
        this.images.splice(index, 1);
        this.updateView();
    }

    removeLongText(index) {
        this.longTexts.splice(index, 1);
        this.updateView();
    }


    setIsBlocked(state){
        this.isBlocked = state;
        this.updateView();
    }

    updateDocumentsView() {
        const texts = [];
        this.longTexts.forEach((text, index) => {
            texts.push(`<span id="doc-${index}"><i class="fas fa-file-alt" style="margin-right:5px;"></i> ${text.length} <i class="fas fa-times" style="cursor: pointer;" onclick="window.inputView.removeLongText(${index})"></i></span>`);
        });
        this.images.forEach((img, index) => {
            texts.push(`<span id="img-${index}"><i class="fas fa-image" style="margin-right:5px;"></i> ${img.length} <i class="fas fa-times" style="cursor: pointer;" onclick="window.inputView.removeImage(${index})"></i></span>`);
        });
        this.documentsHtml.innerHTML = texts.join(' ');
    }



    appendText(event) {
        if (event.key === "Enter") {
            event.preventDefault();
            this.makeChat();
        }
    }

    makeChat() {
        const message = this.chatInput.value;

        if (this.isBlocked) {
            window.chat.stopStreaming();
            this.isBlocked = false;
            this.updateView();
            return;
        }

        if (!message.trim()) {
            return;
        }

        if (!window.settings.key && !window.settings.useSharedKeys) {
            window.modal.open(
                t.t("error"),
                '<p>'+t.t("noApiKey")+'</p>'
            );
            return;
        }

        this.chatInput.value = "";
        this.isBlocked = true;
        window.chat.sendMessage(message, "user", this.images, this.longTexts);
        this.images = [];
        this.longTexts = [];
        this.updateView();
    }

    updateView(){
        if(this.isBlocked){
            document.getElementById('send-icon').classList.add("d-none");
            document.getElementById('stop-icon').classList.remove("d-none");
        }else{
            document.getElementById('send-icon').classList.remove("d-none");
            document.getElementById('stop-icon').classList.add("d-none");
        }
        this.updateDocumentsView();
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
                    reader.onload = async function (e) {
                        const base64 = e.target.result;
                        try {
                            const fileId = await window.chatHistory.uploadImageBase64(base64);
                            window.inputView.images.push(fileId);
                        } catch (error) {
                            console.error("Error sending image:", error);
                        }
                        window.inputView.updateView();
                    };
                    reader.readAsDataURL(file);
                    break;
                }
            }

            if (!hasImage) {
                if (pastedText.length > 2000) {
                    event.preventDefault();
                    window.inputView.longTexts.push(pastedText);
                    window.inputView.updateView();
                } else {
                }
            }

        });
    }

}

window.inputView = new InputView();