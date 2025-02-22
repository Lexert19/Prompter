class HtmlParser{
    constructor(){
        this.elements = [];
        this.currentLineBuffer = "";
        this.isCodeBlock = false;
        this.currentCodeBlock = { type: 'code', language: '', text: '' };
        this.isThinkingBlock = false;
        this.currentThinkingBlock = { type: 'thinking', text: '' };
    }


    parse(textFragment){
        this.currentLineBuffer += textFragment;
        let lines = this.currentLineBuffer.split('\n');
        this.currentLineBuffer = lines.pop(); // Keep the last incomplete line in buffer

        for (const line of lines) {
            if (this.isCodeBlock) {
                if (line.trim() === "```") {
                    this.isCodeBlock = false;
                    this.elements.push(this.currentCodeBlock);
                    this.currentCodeBlock = { type: 'code', language: '', text: '' };
                } else {
                    this.currentCodeBlock.text += line + '\n';
                }
                continue; // Skip normal line processing when in code block
            }

            if (this.isThinkingBlock) {
                if (line.trim() === "</think>") {
                    this.isThinkingBlock = false;
                    this.elements.push(this.currentThinkingBlock);
                    this.currentThinkingBlock = { type: 'thinking', text: '' };
                } else {
                    this.currentThinkingBlock.text += line + '\n';
                }
                continue; // Skip normal line processing when in thinking block
            }


            if (line.startsWith("```")) {
                const language = line.substring(3).trim();
                this.isCodeBlock = true;
                this.currentCodeBlock.language = language;
                continue;
            }

            if (line.startsWith("<think>")) {
                this.isThinkingBlock = true;
                continue;
            }


            if (line.startsWith("###")) {
                this.elements.push({ type: 'header', text: line.substring(3).trim() });
            } else if (/^\d+\.\s\*\*(.+?)\*\*\:\s(.*)$/.test(line)) {
                const match = line.match(/^(\d+)\.\s\*\*(.+?)\*\*\:\s(.*)$/);
                if (match) {
                    const header = match[2].trim();
                    const text = match[3].trim();
                    this.elements.push({ type: 'olElement', header: header, text: text });
                } else {
                    this.elements.push({ type: 'normalText', text: line });
                }

            } else if (line.startsWith("-")) {
                this.elements.push({ type: 'ulElement', text: line.substring(1).trim() });
            }
            else if (line.trim() !== "") { //ignore empty lines
                this.elements.push({ type: 'normalText', text: line });
            }
        }
    }

    toHTML(){
        let html = "";
        for (const element of this.elements) {
            switch (element.type) {
                case 'normalText':
                    html += `<p><pre><code>${this.escapeHtml(element.text)}</code></pre></p>\n`;
                    break;
                case 'code':
                    const languageClass = element.language ? `language-${element.language}` : '';
                    html += `<pre><code class="${languageClass}">${this.escapeHtml(element.text)}</code></pre>\n`;
                    break;
                case 'header':
                    html += `<h3><pre><code>${this.escapeHtml(element.text)}</code></pre></h3>\n`;
                    break;
                case 'olElement':
                    html += `<ol><li><strong><pre><code>${this.escapeHtml(element.header)}</code></pre></strong>: <pre><code>${this.escapeHtml(element.text)}</code></pre></li></ol>\n`;
                    break;
                case 'ulElement':
                    html += `<ul><li><pre><code>${this.escapeHtml(element.text)}</code></pre></li></ul>\n`;
                    break;
                case 'thinking':
                    html += `<div class="thinking"><h4>Thinking:</h4><pre><code>${this.escapeHtml(element.text)}</code></pre></div>\n`;
                    break;
            }
        }
        return html;
    }

    escapeHtml(text) {
        return text.replace(/&/g, '&amp;')
                   .replace(/</g, '&lt;')
                   .replace(/>/g, '&gt;')
                   .replace(/"/g, '&quot;')
                   .replace(/'/g, '&#039;');
    }
}