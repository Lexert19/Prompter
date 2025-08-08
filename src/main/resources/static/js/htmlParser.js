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
        const codeBlockDelimiterRegex = /^ *```/;
        if(line.startsWith('<think>')){
            return "START_THINKING";
        }else if (line.startsWith('</think>')) {
            return "STOP_THINKING";
        }else if(codeBlockDelimiterRegex.test(line) && previousLine.mode != "CODE"){
            return "START_CODE";
        }else if(codeBlockDelimiterRegex.test(line) && previousLine.mode == "CODE"){
            return "STOP_CODE";
        }else if(previousLine.mode == "START_THINKING" || previousLine.mode == "THINKING"){
            return "THINKING";
        }else if(previousLine.mode == "START_CODE" || previousLine.mode == "CODE"){
            return "CODE";
        }else if(line.startsWith("###") || line.startsWith("##")){
            return "HEADER3"
        }else if(line.startsWith("**")){
            //return "HEADER3"
        }else if(line.startsWith("* ") || line.startsWith("- ")){
            return "LISTITEM"
        }else if(line.startsWith("    *") ){
            return "LISTITEM2";
        }else if(line.startsWith("        *")){
            return "LISTITEM3";
        }else if(line.startsWith("---")){
            return "LINE";
        }else if(line.startsWith("   -")){
            return "LISTITEM2DEEPSEEK";
        }else if(line.startsWith("     -")){
            return "LISTITEM3DEEPSEEK";
        }

        return "NORMAL";
    }

    toHTML(){
        this.blocks = [];
        let currentBlock = { type: 'NORMAL', content: '' };

        for(const line of this.lines){
            switch(line.mode){
                case 'START_THINKING':
                    currentBlock = { type: 'THINKING', content: '' };
                    this.blocks.push(currentBlock);
                    break;
                case 'STOP_THINKING':
                    break;
                case 'START_CODE':
                    currentBlock = {
                        type: 'CODE',
                        content: '',
                        language: line.text.replace(/```/, '').trim() || null
                    };
                    this.blocks.push(currentBlock);
                    break;
                case 'STOP_CODE':
                    hljs.highlightAll();
                    break;
                case 'CODE':
                case 'THINKING':
                    this.blocks[this.blocks.length-1].content += line.text + "\n";
                    break;
                case 'NORMAL':
                    if(line.text){
                        currentBlock = { type: line.mode, content: line.text };
                        this.blocks.push(currentBlock);
                    }
                break;
                case 'LISTITEM':
                case 'LISTITEM2':
                case 'LISTITEM2DEEPSEEK':
                case 'LISTITEM3':
                case 'LISTITEM3DEEPSEEK':
                case 'LINE':
                case 'HEADER2':
                case 'HEADER3':
                    currentBlock = { type: line.mode, content: line.text };
                    this.blocks.push(currentBlock);
                    break;

            }
        }
        let html = "";
        for(const block of this.blocks){
            html += this.renderBlock(block);
        }
        return html;
    }

//    toBlocks() {
//        let html = '';
//        let currentMode = 'NORMAL';
//        let currentBlock = { type: 'NORMAL', content: '' };
//
//        for (const line of this.lines) {
//            currentBlock = { type: 'NORMAL', content: '' };
//            switch (line.mode) {
//                case 'START_THINKING':
//                    currentBlock = { type: 'THINKING', content: '' };
//                    break;
//
//                case 'STOP_THINKING':
//                    currentBlock = { type: 'NORMAL', content: '' };
//                    break;
//
//                case 'START_CODE':
//                    currentBlock = {
//                        type: 'CODE',
//                        content: '',
//                        language: line.text.replace(/```/, '').trim() || null
//                    };
//                    break;
//
//                case 'STOP_CODE':
//                    currentBlock = { type: 'NORMAL', content: '' };
//                    break;
//
//                case 'CODE':
//                case 'THINKING':
//                    currentBlock = { type: line.mode, content: "\n" + line.text };
//                    break;
//                case 'NORMAL':
//                    if(line.text){
//                        currentBlock = { type: line.mode, content: "\n" + line.text };
//                    }
//                    break;
//                case 'LISTITEM':
//                    currentBlock = { type: 'LISTITEM', content: line.text };
//                    break;
//                case 'LISTITEM2':
//                    currentBlock = { type: line.mode, content: line.text };
//                    break;
//                case 'LISTITEM2DEEPSEEK':
//                    currentBlock = { type: line.mode, content: line.text };
//                    break;
//                case 'LISTITEM3':
//                    currentBlock = { type: line.mode, content: line.text };
//                    break;
//                case 'LISTITEM3DEEPSEEK':
//                    currentBlock = { type: line.mode, content: line.text };
//                    break;
//                case 'LINE':
//                    currentBlock = { type: line.mode, content: line.text };
//                    break;
//                case 'HEADER2':
//                case 'HEADER3':
//                    currentBlock = { type: line.mode, content: line.text };
//                    break;
//
//            }
//
//            html += this.renderBlock(currentBlock);
//        }
//
//        return html;
//    }

    renderStrongs(content){
        const withoutHtmlContent = this.escapeHtml(content);
        let formattedContent = withoutHtmlContent.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
        formattedContent = formattedContent.replace(/\*(.*?)\*/g, '<em>$1</em>');
        return formattedContent;
    }



    renderBlock(block) {
        let content = "";
        let withoutHtmlContent = "";
        switch (block.type) {
            case 'NORMAL':
                withoutHtmlContent = this.escapeHtml(block.content);
                content = this.renderStrongs(block.content)
                return `<p>${content}</p>`;
            case 'RAW':
                withoutHtmlContent = this.escapeHtml(block.content);
                return `${withoutHtmlContent}`;
            case 'THINKING':
                const randomId = Math.floor(Math.random() * 1000000);
                return `<div class="thinking"><button class="clear-button p-0 bg-transparent" onclick="collapseThinkingContent(${randomId})"><h4>Thinking:</h4></button><p class="thinking-content" id="thinkingContent-${randomId}">${this.escapeHtml(block.content)}</p></div>\n`;
            case 'CODE':
                const langClass = block.language ? ` class="language-${block.language}"` : '';
                return `<div class="code-block"><pre><code${langClass}>${this.escapeHtml(block.content)}</code></pre></div>\n`;
            case 'HEADER2':
                return `<h2>${this.escapeHtml(block.content.substring(3).trim())}</h2>\n`;
            case 'HEADER3':
                content = this.renderStrongs(block.content.substring(3).trim())
                return `<h3>${content}</h3>`;
            case 'LISTITEM':
                content = this.renderStrongs(block.content.substring(2).trim())
                return `<li>${content}</li>`;
            case 'LISTITEM2':
                content = this.renderStrongs(block.content.substring(5).trim())
                return `<li class="ml-1">${content}</li>`;
            case 'LISTITEM2DEEPSEEK':
                content = this.renderStrongs(block.content.substring(4).trim())
                return `<li class="ml-1">${content}</li>`;
            case 'LISTITEM3':
                content = this.renderStrongs(block.content.substring(9).trim())
                return `<li class="ml-2">${content}</li>`;
            case 'LISTITEM3DEEPSEEK':
                content = this.renderStrongs(block.content.substring(6).trim())
                return `<li class="ml-2">${content}</li>`;
            case 'LINE':
                return `<div class="line">${content}</div>`;
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


