class InputView{
    static _instance = null;

    static instance() {
        if (!InputView._instance) {
            InputView._instance = new InputView();
        }
        return InputView._instance;
    }

    constructor(){
        if (InputView._instance) {
            return InputView._instance;
        }
        InputView._instance = this;

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
            texts.push(`<span id="doc-${index}"><i class="fas fa-file-alt" style="margin-right:5px;"></i> ${text.length} <i class="fas fa-times" style="cursor: pointer;" onclick="InputView.instance().removeLongText(${index})"></i></span>`);
        });
        this.images.forEach((img, index) => {
            texts.push(`<span id="img-${index}"><i class="fas fa-image" style="margin-right:5px;"></i> ${img.length} <i class="fas fa-times" style="cursor: pointer;" onclick="InputView.instance().removeImage(${index})"></i></span>`);
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
            Chat.instance().stopStreaming();
            this.isBlocked = false;
            this.updateView();
            return;
        }

        if (!message.trim()) {
            return;
        }

        if (!Settings.instance().key && !Settings.instance().useSharedKeys) {
            Modal.instance().open(
                t.t("error"),
                '<p>'+t.t("noApiKey")+'</p>'
            );
            return;
        }

        this.chatInput.value = "";
        this.isBlocked = true;
        Chat.instance().sendMessage(message, "user", this.images, this.longTexts);
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

        const contextSize = Chat.instance().requestBuilder.calculateContextSize();
        const contextDisplay = document.getElementById('context-counter');
        if (contextDisplay) {
            //todo
            //contextDisplay.textContent = `Kontekst: ${contextSize.toLocaleString()} znaków`;
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
                            const fileId = await History.instance().uploadImageBase64(base64);
                            InputView.instance().images.push(fileId);
                        } catch (error) {
                            console.error("Error sending image:", error);
                        }
                        InputView.instance().updateView();
                    };
                    reader.readAsDataURL(file);
                    break;
                }
            }

            if (!hasImage) {
                if (pastedText.length > 2000) {
                    event.preventDefault();
                    InputView.instance().longTexts.push(pastedText);
                    InputView.instance().updateView();
                } else {
                }
            }

        });
    }

}

InputView.instance();