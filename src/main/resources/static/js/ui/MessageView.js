class MessageView{
    constructor(message){
        this.editMenu = window.editMessageView;
        this.message = message;
    }

    createHtmlElement(destination, finished = false){
        let duration = "";
        let tpsDisplay = "";
        if (this.message.end !== null) {
            const durationMs = this.message.end - this.message.start;
            const seconds = (durationMs / 1000).toFixed(1);
            duration = `${seconds} s`;
        }
        if (this.message.role === "assistant") {
            tpsDisplay = ` | ${this.message.getTps()} tps`;
        }

        let htmlContent = "";
        if(this.message.role == "assistant"){
            htmlContent = window.chatClient.parser.parseToHtml(this.message.getText());
        }else{
            htmlContent = escapeHtml(this.message.getText());
        }
        var htmlMessage = `
            <div id="${this.message.id}" class="message ${this.message.role}">
                <div class="assitant-data d-flex align-items-center">
                    <div class="date"></div>
                    <div class="duration" id="duration-${this.message.id}">${duration}</div>
                    <div class="duration ms-1" id="tps-${this.message.id}">${tpsDisplay}</div>
                    <div class="loading-dots" id="loading-${this.message.id}" style="display: ${finished ? 'none' : 'inline-flex'};">
                        <span></span><span></span><span></span>
                    </div>
                </div>
                ${this.message.getHtmlImages()}
                ${this.message.getHtmlFiles()}
                <div id="input-${this.message.id}" class="code-wrap">${htmlContent}</div>
                <span id="cached-${this.message.id}">${this.message.cache ? "cached" : ""}<span>
            </div>
            `;

        destination.insertAdjacentHTML('afterbegin', htmlMessage);
        const messageElement = document.getElementById(this.message.id);
        messageElement.addEventListener('contextmenu', (event) => {
            event.preventDefault();
            this.editMenu.showEditMenu(event, this.message.id);
        });

        if(this.message.role == "assistant" && finished == false)
            this.startDurationCounter();
        return document.getElementById("input-" + this.message.id);
    }

    startDurationCounter(){
        this.updateDurationCounter();
        this.updateTpsCounter();

        const intervalId = setInterval(() => {
            if (this.message.end !== null) {
                clearInterval(intervalId);
                const loadingEl = document.getElementById(`loading-${this.message.id}`);
                if (loadingEl) loadingEl.style.display = 'none';
                return;
            }
            this.updateDurationCounter();
            this.updateTpsCounter();
        }, 100);
    }

    updateDurationCounter(){
        const durationElement = document.getElementById(`duration-${this.message.id}`);
        const currentTime = Date.now();
        const elapsedTimeMs = currentTime - this.message.start;
        const elapsedTimeSec = (elapsedTimeMs / 1000).toFixed(1);
        durationElement.textContent = `${elapsedTimeSec}s`;
    }

    updateTpsCounter() {
        const tpsElement = document.getElementById(`tps-${this.message.id}`);
        const tps = this.message.getTps();
        tpsElement.textContent = ` | ${tps} tps`;
    }

    finish(){
        this.updateTpsCounter();
    }
}