class MessageView{
    constructor(message){
        this.editMenu = window.editMessageView;
        this.message = message;
    }

    createHtmlElement(destination, finish = false){
        let duration = "";
        if (this.message.end !== null) {
            const durationMs = this.message.end - this.message.start;
            const seconds = (durationMs / 1000).toFixed(1);
            duration = `${seconds} s`;
        }
        var htmlMessage = `
            <div id="${this.message.id}" class="message ${this.message.role}">
                <div class="assitant-data">
                    <div class="date"></div>
                    <div class="duration" id="duration-${this.message.id}">${duration}</div>
                </div>
                ${this.message.getHtmlImages()}
                ${this.message.getHtmlFiles()}
                <div id="input-${this.message.id}" class="code-wrap">${escapeHtml(this.message.getText())}</div>
                <span id="cached-${this.message.id}">${this.message.cache ? "cached" : ""}<span>
            </div>
            `;

        destination.insertAdjacentHTML('afterbegin', htmlMessage);
        const messageElement = document.getElementById(this.message.id);
        messageElement.addEventListener('contextmenu', (event) => {
            event.preventDefault();
            this.editMenu.showEditMenu(event, this.message.id);
        });

        if(this.message.role == "assistant" && finish == false)
            this.startDurationCounter();
        return document.getElementById("input-" + this.message.id);
    }

    startDurationCounter(){
        this.updateDurationCounter();

        const intervalId = setInterval(() => {
            if (this.message.end !== null) {
                clearInterval(intervalId);
                return;
            }
            this.updateDurationCounter();
        }, 100);
    }


    updateDurationCounter(){
        const durationElement = document.getElementById(`duration-${this.message.id}`);
        if(durationElement){
            const currentTime = Date.now();
            const elapsedTimeMs = currentTime - this.message.start;

            const elapsedTimeSec = (elapsedTimeMs / 1000).toFixed(1);

            durationElement.textContent = `${elapsedTimeSec}s`;
        }
    }
}