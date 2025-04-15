class HtmlParser {
    constructor() {
        this.elements = [];
        this.bufferedText = "";
        this.isCodeBlock = false;
        this.isThinkingBlock = false;
        this.isNormalBlock = false;
        this.lines = [];
    }

    clear(){
        this.lines = [];
        this.bufferedText = "";

    }

    parse(textFragment) {
        this.bufferedText += textFragment;
        this.readLines();
    }

    readLines(){
        let lines = this.bufferedText.split('\n');
        for (let i = 0; i < lines.length; i++) {
            let line = lines[i];

            let previousLine = null;
            if(i == 0)
                previousLine = {text: "", mode: ""};
            else
                previousLine = this.lines[i-1];
            this.lines[i] = {
                text: line,
                mode: this.getLineMode(previousLine, line)
            };

        }
    }

    getLineMode(previousLine, line){
        if(line.startsWith('<think>')){
            return "START_THINKING";
        }else if (line.startsWith('</think>')) {
            return "STOP_THINKING";
        }else if(line.startsWith('```') && previousLine.mode != "CODE"){
            return "START_CODE";
        }else if(line.startsWith('```') && previousLine.mode == "CODE"){
            return "STOP_CODE";
        }else if(previousLine.mode == "START_THINKING" || previousLine.mode == "THINKING"){
            return "THINKING";
        }else if(previousLine.mode == "START_CODE" || previousLine.mode == "CODE"){
            return "CODE";
        }else if(line.startsWith("###") || line.startsWith("##")){
            return "HEADER3"
        }else if(line.startsWith("**")){
            //return "HEADER3"
        }

        return "NORMAL";
    }

    toHTML() {
        let html = '';
        let currentMode = 'NORMAL';
        let currentBlock = { type: 'NORMAL', content: '' };

        for (const line of this.lines) {
            switch (line.mode) {
                case 'START_THINKING':
                    if (currentBlock.content.trim()) {
                        html += this.renderBlock(currentBlock);
                    }
                    currentMode = 'THINKING';
                    currentBlock = { type: 'THINKING', content: '' };
                    break;

                case 'STOP_THINKING':
                    if (currentBlock.content.trim()) {
                        html += this.renderBlock(currentBlock);
                    }
                    currentMode = 'NORMAL';
                    currentBlock = { type: 'NORMAL', content: '' };
                    break;

                case 'START_CODE':
                    if (currentBlock.content.trim()) {
                        html += this.renderBlock(currentBlock);
                    }
                    currentMode = 'CODE';
                    currentBlock = {
                        type: 'CODE',
                        content: '',
                        language: line.text.replace(/```/, '').trim() || null
                    };
                    break;

                case 'STOP_CODE':
                    if (currentBlock.content.trim()) {
                        html += this.renderBlock(currentBlock);
                    }
                    currentMode = 'NORMAL';
                    currentBlock = { type: 'NORMAL', content: '' };
                    break;

                case 'THINKING':
                case 'CODE':
                case 'NORMAL':
                    if (line.text) {
                        if (currentBlock.content) {
                            currentBlock.content += '\n' + line.text;
                        } else {
                            currentBlock.content = line.text;
                        }
                    }
                    break;
                case 'HEADER2':
                case 'HEADER3':
                    if (currentBlock.content.trim()) {
                        html += this.renderBlock(currentBlock);
                        currentBlock = { type: line.mode, content: line.text };
                    } else {
                        currentBlock = { type: line.mode, content: line.text };
                    }
                    html += this.renderBlock(currentBlock);
                    currentBlock = { type: 'NORMAL', content: '' };
                    break;
            }
        }

        if (currentBlock.content.trim()) {
            html += this.renderBlock(currentBlock);
        }

        return html;
    }

    renderBlock(block) {
        switch (block.type) {
            case 'NORMAL':
                let withoutHtmlContent = this.escapeHtml(block.content);
                let content = withoutHtmlContent.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
                return `<p>${content}</p>\n`;

            case 'THINKING':
                return `<div class="thinking"><h4>Thinking:</h4><p>${this.escapeHtml(block.content)}</p></div>\n`;

            case 'CODE':
                const langClass = block.language ? ` class="language-${block.language}"` : '';
                return `<div class="code-block"><pre><code${langClass}>${this.escapeHtml(block.content)}</code></pre></div>\n`;
            case 'HEADER2':
                return `<h2>${this.escapeHtml(block.content.substring(3).trim())}</h2>\n`;
            case 'HEADER3':
                let withoutHtmlContent = this.escapeHtml(block.content.substring(3).trim());
                let content = withoutHtmlContent.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
                //const header2Content = block.content.substring(2).replace(/\*\*/g, '').trim();
                return `<h3>${content}</h3>\n`;

            default:
                return '';
        }
    }

    escapeHtml(text) {
        return text
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
}


