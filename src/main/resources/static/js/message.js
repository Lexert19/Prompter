class Message {
    constructor(role = "", text = "", images = [], documents = [], context = []) {
        this.role = role;
        this.content = [];
        this.id = Math.random().toString(36);
        this.cache = false;
        this.documents = documents;
        this.context = context;
        this.text = text;
        this.time = Date.now();
        this.start = Date.now();
        this.end = null;
        this.images = images;
    }

    appendText(text) {
        this.text += text;
    }

    getText() {
        return this.text;
    }

    buildContent() {
        const content = [];

        if (this.text) {
            let textContent = "";
            this.context.forEach(
                (context) => (textContent += `<context>${context}</context>`)
            );
            this.documents.forEach(
                (doc) => (textContent += "<document>" + doc + "</document>")
            );

            content.push({
                type: "text",
                text: textContent + this.text + this.getSystemPrompt(),
                cache: this.cache,
            });
        }

        this.images.forEach(img => {
            const parts = img.split(';');
            const mediaType = parts[0].split(':')[1];
            const data = parts[1].split(',')[1];

            content.push({
                type: "image",
                mediaType: mediaType,
                data: data,
                cache: this.cache
            });
        });

        return content;
    }

    getSystemPrompt(){
        if(window.settings.systemSwitch == false){
            return "";
        }

        return `{${window.settings.system}}`

    }

    getHtmlImages() {
        return this.images.map((img) => {
            const parts = img.split(';');
            const mediaType = parts[0].split(':')[1];
            const data = parts[1].split(',')[1];

            return `<img class="image" src="data:${mediaType};base64,${data}" alt="Inline image">`;
        }).join("");
    }

    getHtmlFiles() {
        let documentsText = "";
        this.documents.forEach((document, index) => {
            documentsText += `<div class="me-2">Dokument: ${document.length}</div>`;
        });
        return `<div class="d-flex">${documentsText}</div>`;
    }
}
