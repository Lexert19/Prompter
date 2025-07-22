class InputView{
    constructor(){
        this.chatInput = document.getElementById("input");
        this.documentsHtml = document.getElementById("documents");

        this.addPasteListener();
    }

    updateDocumentsView() {
        const docElement = this.documentsHtml;
        let content = '';
        const texts = [];
        window.data.documents.forEach((doc, index) => {
            texts.push(`<span id="doc-${index}"><i class="fas fa-file-alt" style="margin-right:5px;"></i> ${doc.length} <i class="fas fa-times" style="cursor: pointer;" onclick="window.data.removeDocument(${index})"></i></span>`);
        });
        window.data.images.forEach((img, index) => {
            texts.push(`<span id="img-${index}"><i class="fas fa-image" style="margin-right:5px;"></i> ${img.length} <i class="fas fa-times" style="cursor: pointer;" onclick="window.data.removeImage(${index})"></i></span>`);
        });
        if (texts.length) content += ' ' + texts.join(' ');
        docElement.innerHTML = content;
    }


    appendText(event) {
        if (event.key === "Enter") {
            event.preventDefault();
            this.makeChat();
        }
    }

    makeChat(){
        const sended = window.chatApi.chat(
            this.chatInput.value,
            "user"
        );
        if(sended){
            this.chatInput.value = "";
        }
    }

    updateView(){
        if(window.data.blockedInput){
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

                    reader.onload = function (event) {
                        window.data.appendImage(event.target.result);
                    };

                    reader.readAsDataURL(file);
                    hasImage = true;
                    break;
                }
            }

            if (!hasImage) {
                if (pastedText.length > 2000) {
                    event.preventDefault();
                    window.data.appendDocument(pastedText);
                } else {
                }
            }

        });
    }

}

window.inputView = new InputView();