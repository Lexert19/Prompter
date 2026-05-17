class HtmlParser {
    constructor() {
        this.elements = [];
        this.bufferedText = "";
        this.isCodeBlock = false;
        this.isThinkingBlock = false;
        this.isNormalBlock = false;
        this.blocks = [];
        this.previousBlocks = [];
        this.lines = [];
        this.blockElements = new Map();
        this.randomId = Math.floor(Math.random() * 1000000);
        this.nextBlockId = 0;
        this.processedLength = 0;
        this.incompleteLine = '';
    }

    setRootElement(rootElement){
        this.rootElement = rootElement;
    }

    clear(){
        this.lines = [];
        this.bufferedText = "";
        this.blocks = [];
        this.blockElements.clear();
        this.previousBlocks = [];
        this.randomId = Math.floor(Math.random() * 1000000);
        this.nextBlockId = 0;
        this.processedLength = 0;
        this.incompleteLine = '';
        this.lastProcessedLine = 0;
        this.isCodeBlock = false;
        this.isThinkingBlock = false;
    }

    parse(textFragment) {
        this.bufferedText += textFragment;

        const newData = this.bufferedText.slice(this.processedLength);
        this.processedLength = this.bufferedText.length;

        this.incompleteLine += newData;
        const lines = this.incompleteLine.split('\n');

        this.incompleteLine = lines.pop() || '';

        for (const line of lines) {
            const previousLine = this.lines[this.lines.length - 1] || {text: "", mode: ""};
            this.lines.push({
                text: line,
                mode: this.getLineMode(previousLine, line)
            });
        }

        this.updateBlocks();
        this.renderChanges();
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
        const codeDelim = /^ *```/;

        if(line.startsWith('<think>')){ this.isThinkingBlock = true; return "START_THINKING"; }
        if(line.startsWith('</think>')){ this.isThinkingBlock = false; return "STOP_THINKING"; }

        if(codeDelim.test(line)){
            if(this.isCodeBlock){ this.isCodeBlock = false; return "STOP_CODE"; }
            this.isCodeBlock = true; return "START_CODE";
        }

        if(this.isThinkingBlock) return "THINKING";
        if(this.isCodeBlock) return "CODE";

        if(line.startsWith("###") || line.startsWith("##")) return "HEADER3";
        if(line.startsWith("---")) return "LINE";
        if(line.startsWith("|")) return "ROW";

        if(line.startsWith("        *") || line.startsWith("        -")) return "LISTITEM3";
        if(line.startsWith("    *") || line.startsWith("    -")) return "LISTITEM2";
        if(line.startsWith("     -")) return "LISTITEM3DEEPSEEK";
        if(line.startsWith("   -")) return "LISTITEM2DEEPSEEK";
        if(line.startsWith("* ") || line.startsWith("- ")) return "LISTITEM"

        return "NORMAL";
    }

//    getLineMode(previousLine, line){
//        const codeBlockDelimiterRegex = /^ *```/;
//        if(line.startsWith('<think>')){
//            return "START_THINKING";
//        }else if (line.startsWith('</think>')) {
//            return "STOP_THINKING";
//        }else if(codeBlockDelimiterRegex.test(line) && previousLine.mode != "CODE"){
//            return "START_CODE";
//        }else if(codeBlockDelimiterRegex.test(line) && previousLine.mode == "CODE"){
//            return "STOP_CODE";
//        }else if(previousLine.mode == "START_THINKING" || previousLine.mode == "THINKING"){
//            return "THINKING";
//        }else if(previousLine.mode == "START_CODE" || previousLine.mode == "CODE"){
//            return "CODE";
//        }else if(line.startsWith("###") || line.startsWith("##")){
//            return "HEADER3"
//        }else if(line.startsWith("**")){
//            //return "HEADER3"
//        }else if(line.startsWith("* ") || line.startsWith("- ")){
//            return "LISTITEM"
//        }else if(line.startsWith("    *") ){
//            return "LISTITEM2";
//        }else if(line.startsWith("        *")){
//            return "LISTITEM3";
//        }else if(line.startsWith("---")){
//            return "LINE";
//        }else if(line.startsWith("   -")){
//            return "LISTITEM2DEEPSEEK";
//        }else if(line.startsWith("     -")){
//            return "LISTITEM3DEEPSEEK";
//        }else if(line.startsWith("|")){
//            return "ROW";
//        }
//
//        return "NORMAL";
//    }

    updateBlocks(){
        const startIdx = this.lastProcessedLine || 0;
        for(let i = startIdx; i < this.lines.length; i++){
            const line = this.lines[i];
            let currentBlock = this.blocks[this.blocks.length - 1];
            switch(line.mode){
                case 'START_THINKING':
                    currentBlock = { id: this.nextBlockId++, type: 'THINKING', content: '', closed: false };
                    this.blocks.push(currentBlock);
                    break;
                case 'STOP_THINKING':
                    if(currentBlock && currentBlock.type === 'THINKING') currentBlock.closed = true;
                    break;
                case 'START_CODE':
                    this.blocks.push({
                        id: this.nextBlockId++,
                        type: 'CODE',
                        content: '',
                        language: line.text.replace(/```/, '').trim() || null,
                        closed: false
                    });
                    break;
                case 'STOP_CODE':
                    if(currentBlock && currentBlock.type === 'CODE') currentBlock.closed = true;
                    break;
                case 'CODE':
                case 'THINKING':
                    if(currentBlock) currentBlock.content += line.text + "\n";
                    break;
                case 'NORMAL':
                    if(line.text){
                        this.blocks.push({ id: this.nextBlockId++, type: 'NORMAL', content: line.text, closed: true });
                    }
                    break;
                case 'ROW':
                case 'LISTITEM':
                case 'LISTITEM2':
                case 'LISTITEM2DEEPSEEK':
                case 'LISTITEM3':
                case 'LISTITEM3DEEPSEEK':
                case 'LINE':
                case 'HEADER2':
                case 'HEADER3':
                    this.blocks.push({ id: this.nextBlockId++, type: line.mode, content: line.text, closed: true });
                    break;

            }
        }
        this.lastProcessedLine = this.lines.length;
    }

    renderStrongs(content){
        const withoutHtmlContent = this.escapeHtml(content);
        let formattedContent = withoutHtmlContent.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
        formattedContent = formattedContent.replace(/\*(.*?)\*/g, '<em>$1</em>');
        return formattedContent;
    }

    renderBlock(block){
        return `<div id="block-${block.id}">${this.renderBlockToHtmlString(block)}</div>`;
    }

    renderBlockToHtmlString(block) {
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
                return `<div class="thinking"><button class="clear-button p-0 bg-transparent" onclick="collapseThinkingContent(${this.randomId})"><h4>Thinking:</h4></button><p class="thinking-content show" id="thinkingContent-${this.randomId}">${this.escapeHtml(block.content)}</p></div>\n`;
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
                return `<div class="line"></div>`;
            case 'ROW':
                let rowContent = block.content.trim();
                if (rowContent.startsWith('|') && rowContent.endsWith('|')) {
                    rowContent = rowContent.substring(1, rowContent.length - 1);
                }

                let cells = rowContent.split('|').map(cell => cell.trim());
                cells = cells.filter(cell => !cell.includes('---'));

                const cellHtml = cells.map(cell => `<div class="border-blue p-1 w-100 text-break">${this.renderStrongs(cell)}</div>`).join('');

                return `<div class="d-flex">${cellHtml}</div>`;
            default:
                return '';
        }
    }

    _removeStaleBlocks(newBlockMap) {
        for (const [id, element] of this.blockElements.entries()) {
            if (!newBlockMap.has(id)) {
                element.remove();
                this.blockElements.delete(id);
            }
        }
    }

    _highlightCodeBlock(parentElement) {
        const codeElement = parentElement.querySelector('pre code');
        if (codeElement) {
            delete codeElement.dataset.highlighted;
            hljs.highlightElement(codeElement);
        }
    }

    _updateExistingBlock(existingElement, newBlock, oldBlock) {
        if (!oldBlock || oldBlock.content !== newBlock.content || oldBlock.type !== newBlock.type) {
            existingElement.innerHTML = this.renderBlockToHtmlString(newBlock);
            if (newBlock.type === 'CODE') {
                this._highlightCodeBlock(existingElement);
            }
        }
    }

    _createNewBlockElement(newBlock) {
        const newElement = document.createElement('div');
        newElement.id = `block-${newBlock.id}`;
        newElement.innerHTML = this.renderBlockToHtmlString(newBlock);
        return newElement;
    }

    _handleNewBlock(newBlock, currentDomNode) {
        const newElement = this._createNewBlockElement(newBlock);
        this.rootElement.insertBefore(newElement, currentDomNode);
        this.blockElements.set(newBlock.id, newElement);

        if (newBlock.type === 'CODE') {
            this._highlightCodeBlock(newElement);
        }
        return newElement.nextSibling;
    }

    _handleExistingBlock(existingElement, newBlock, oldBlock, currentDomNode) {
        this._updateExistingBlock(existingElement, newBlock, oldBlock);
        if (currentDomNode !== existingElement) {
            this.rootElement.insertBefore(existingElement, currentDomNode);
        }
        return existingElement.nextSibling;
    }

    renderChanges() {
        if (!this.rootElement) return;
        const newMap = new Map(this.blocks.map(b => [b.id, b]));
        this._removeStaleBlocks(newMap);

        let node = this.rootElement.firstChild;
        for (let i = 0; i < this.blocks.length; i++) {
            const block = this.blocks[i];
            const old = this.previousBlocks.find(b => b.id === block.id);
            let el = this.blockElements.get(block.id);

            if (el && block.closed && old && old.content === block.content) {
                node = el.nextSibling;
                continue;
            }

            if (el) {
                this._updateExistingBlock(el, block, old);
                if (node!== el) this.rootElement.insertBefore(el, node);
                node = el.nextSibling;
            } else {
                el = this._createNewBlockElement(block);
                this.rootElement.insertBefore(el, node);
                this.blockElements.set(block.id, el);
                if (block.type === 'CODE' && block.closed) this._highlightCodeBlock(el);
            }
        }
        this.previousBlocks = this.blocks.map(b => ({...b}));
    }

    restoreHighlights(){
        if (!this.rootElement) return;
        this.rootElement.querySelectorAll('pre code').forEach(code => {
            delete code.dataset.highlighted;
            hljs.highlightElement(code);
        });
    }

    escapeHtml(text) {
        return text
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }


    parseToHtml(fullText) {
        this.clear();
        this.bufferedText = fullText;
        this.readLines();
        this.updateBlocks();

        const htmlResult = this.blocks.map(block => {
            return this.renderBlockToHtmlString(block);
        }).join('');

        return htmlResult;
    }
}


