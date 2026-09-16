class Message {
    constructor(role = "", text = "", images = [], documents = [], context = []) {
        this.role = role;
        this.content = [];
        this.id = Math.random().toString(36);
        this.cache = false;
        this.documents = documents;
        this.context = context;
        this.text = text + this.getSystemPrompt(role);
        this.time = Date.now();
        this.start = Date.now();
        this.end = null;
        this.images = images;
    }

    getTps(){
        const now = Date.now();
        const endTime = this.end !== null ? this.end : now;

        const durationSec = (endTime - this.start) / 1000;
        if (durationSec <= 0) return 0;

        const estimatedTokens = this.text.length / 4;

        return parseFloat((estimatedTokens / durationSec).toFixed(2));
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
                text: textContent + this.text,
                cache: this.cache,
            });
        }

        this.images.forEach(img => {
            const fileId = typeof img === 'object' ? img.id : img;
            content.push({
                type: "image",
                fileId: Number(fileId),
                cache: this.cache
            });
        });

        return content;
    }

    getSystemPrompt(role){
        if(Settings.instance().systemSwitch == false || role == "assistant"){
            return "";
        }

        return `{${Settings.instance().system}}`

    }

    getHtmlImages() {
        return this.images.map((img) => {
            const id = typeof img === 'object' ? img.id : img;
            return `<div class="image-container" style="display:inline-block;margin:5px;">
            <a href="/api/files/${id}" target="_blank" class="image-link">
                <img class="image-thumb" data-file-id="${id}" src="/api/files/${id}" alt="Image" style="max-width:200px;max-height:200px;border-radius:8px;object-fit:cover;cursor:pointer;" loading="lazy">
            </a>
        </div>`;
        }).join("");
    }

    getHtmlFiles() {
        let documentsText = "";
        this.documents.forEach((document, index) => {
            documentsText += `<div class="me-2">${t.t("document")} ${document.length}</div>`;
        });
        return `<div class="d-flex">${documentsText}</div>`;
    }
}
