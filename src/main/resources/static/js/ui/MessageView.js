class MessageView{
    constructor(message){
        this.message = message;
    }

    createHtmlElement(destination, finished = false){
        let duration = "";
        let tpsDisplay = "";

        if (this.message.role === "assistant" && this.message.end !== null) {
            const durationMs = this.message.end - this.message.start;
            const seconds = (durationMs / 1000).toFixed(1);
            duration = `${seconds} s`;
        }
        if (this.message.role === "assistant") {
            tpsDisplay = ` | ${this.message.getTps()} tps`;
        }

        let htmlContent = "";
        if(this.message.role == "assistant"){
            htmlContent = ChatClient.instance().parser.parseToHtml(this.message.getText());
        }else{
            htmlContent = escapeHtml(this.message.getText());
        }

        var htmlMessage = `
            <div id="${this.message.id}" class="message ${this.message.role}">
                <div class="assitant-data d-flex align-items-center">
                    <div class="date"></div>
                    <div class="duration" id="duration-${this.message.id}">${duration}</div>
                    <div class="duration ms-1" id="tps-${this.message.id}">${tpsDisplay}</div>
                    <!-- Kontener na przycisk z toolami -->
                    <div id="tool-counter-container-${this.message.id}">${this.renderToolCounter()}</div>
                    <div class="loading-dots" id="loading-${this.message.id}" style="display: ${finished ? 'none' : 'inline-flex'};">
                        <span></span><span></span><span></span>
                    </div>
                </div>
                ${this.message.getHtmlImages()}
                ${this.message.getHtmlFiles()}
                <div id="input-${this.message.id}" class="code-wrap">${htmlContent}</div>
            </div>
            `;

        destination.insertAdjacentHTML('beforeend', htmlMessage);
        const messageElement = document.getElementById(this.message.id);

        if (finished && this.message.role === 'assistant' && window.hljs) {
            messageElement.querySelectorAll('pre code').forEach(block => {
                hljs.highlightElement(block);
            });
        }

        messageElement.addEventListener('contextmenu', (event) => {
            event.preventDefault();
            EditMessageView.instance().showEditMenu(event, this.message.id);
        });

        if(this.message.role == "assistant" && finished == false)
            this.startDurationCounter();
        return document.getElementById("input-" + this.message.id);
    }

    renderToolCounter() {
        const calls   = this.message.toolCalls   || [];
        const results = this.message.toolResults || [];
        if (!calls.length && !results.length) return '';

        const count = Math.max(calls.length, results.length);
        const hasError = results.some(r => r.error);

        return `
            <button class="tool-counter ms-2 rounded-1 ${hasError ? 'tool-counter-error' : ''}"
                    onclick="MessageView.showToolModal('${this.message.id}')"
                    title="${t.t('tools.viewToolCalls') || 'View tool calls'}">
                <i class="fas fa-wrench"></i> ${count}
            </button>
        `;
    }

    renderToolResultContent(text) {
        try {
            const obj = JSON.parse(text);
            if (obj?.downloadUrl && obj?.fileName) {
                const size = obj.sizeBytes
                    ? ` (${(obj.sizeBytes / 1024).toFixed(1)} KB)`
                    : '';
                const counts = obj.keptRounds != null
                    ? `<div class="text-secondary small mt-1">
                     kept ${obj.keptRounds} of ${obj.keptRounds + obj.removedRounds} rounds
                   </div>` : '';
                return `
                <a class="btn-download-export d-inline-flex align-items-center gap-2"
                   href="${escapeHtml(obj.downloadUrl)}" download>
                    <i class="fas fa-file-arrow-down"></i>
                    <span>${escapeHtml(obj.fileName)}</span>
                    <small class="text-secondary">${size}</small>
                </a>
                ${counts}
            `;
            }
        } catch (_) {}
        return `<pre class="tool-output">${escapeHtml(text)}</pre>`;
    }

    static showToolModal(messageId) {
        let msg = null;
        const chat = Chat.instance();
        if (chat && chat.requestBuilder && chat.requestBuilder.messages) {
            msg = chat.requestBuilder.messages.find(m => m.id === messageId);
        }
        if (!msg) return;

        const view = new MessageView(msg);
        const html = view.renderToolBlocksDetail();

        Modal.instance().open(
            t.t('tools.toolCallsDetails') || 'Tool Calls Details',
            html,
            null,
            ['modal-wide']
        );
    }

    renderToolBlocksDetail() {
        const calls   = this.message.toolCalls   || [];
        const results = this.message.toolResults || [];
        if (!calls.length && !results.length) return '<p class="text-secondary">No tool calls</p>';

        const byId = new Map();
        for (const c of calls)   byId.set(c.id, { call: c, result: null, error: null });
        for (const r of results) {
            const e = byId.get(r.id) || { call: null };
            if (r.error) e.error = r.error;
            else e.result = r.content;
            byId.set(r.id, e);
        }

        let html = '<div class="d-flex flex-column gap-2">';
        for (const { call, result, error } of byId.values()) {
            const name = call?.name || results.find(r => r.id === call?.id)?.name || 'tool';
            const status = error
                ? '<span class="tool-status error">error</span>'
                : result != null
                    ? '<span class="tool-status ok">done</span>'
                    : '<span class="tool-status pending">running…</span>';

            html += `
            <div class="tool-block ${error ? 'tool-error' : (result != null ? 'tool-result' : 'tool-call')}">
                <div class="tool-header">
                    <i class="fas fa-wrench"></i>
                    <strong>${escapeHtml(name)}</strong>${status}
                </div>
                ${call?.arguments
                ? `<pre class="tool-args">${escapeHtml(call.arguments)}</pre>` : ''}
                ${result != null ? this.renderToolResultContent(result) : ''}
                ${error
                ? `<pre class="tool-output">${escapeHtml(error)}</pre>` : ''}
            </div>`;
        }
        html += '</div>';
        return html;
    }

    startDurationCounter(){
        this.updateDurationCounter();
        this.updateTpsCounter();

        const intervalId = setInterval(() => {
            this.updateTpsCounter();
            if (this.message.end !== null) {
                clearInterval(intervalId);
                const loadingEl = document.getElementById(`loading-${this.message.id}`);
                if (loadingEl) loadingEl.style.display = 'none';
                return;
            }
            this.updateDurationCounter();
        }, 100);
    }

    updateDurationCounter(){
        const durationElement = document.getElementById(`duration-${this.message.id}`);
        if (!durationElement) return;
        const currentTime = Date.now();
        const elapsedTimeMs = currentTime - this.message.start;
        const elapsedTimeSec = (elapsedTimeMs / 1000).toFixed(1);
        durationElement.textContent = `${elapsedTimeSec}s`;
    }

    updateTpsCounter() {
        const tpsElement = document.getElementById(`tps-${this.message.id}`);
        if (!tpsElement) return;

        const s = this.message.stats;
        if (s && typeof s.tps === 'number') {
            const prefix = s.inThinking ? 'thinking: ' : '';
            tpsElement.textContent = ` | ${prefix}${s.tps} tps`;
        } else {
            tpsElement.textContent = ` | ${this.message.getTps()} tps`;
        }
    }
}