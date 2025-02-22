class HtmlParser{
    constructor(){
        this.elements = [];
        this.currentLineBuffer = "";
        this.isCodeBlock = false;
        //this.currentCodeBlock = { type: 'code', language: '', text: '' };
        this.isThinkingBlock = false;
        this.isNormalBlock = false;
        this.currentThinkingBlock = { type: 'thinking', text: '' };
    }


    parse(textFragment){
        if (textFragment.startsWith("```")) {
            if(this.isCodeBlock){
                this.isCodeBlock = false;
            }else{
                const codeBlock = { type: 'code', language: false, text: '' };
                if( !textFragment.includes("\n")){
                    codeBlock.language = true;
                }
                this.elements.push(codeBlock);
                this.isCodeBlock = true;
                this.isNormalBlock = false
            }
            return;
        }

        if (textFragment.startsWith("<think>")) {
            this.currentThinkingBlock = { type: 'thinking', text: '' };
            this.elements.push(this.currentThinkingBlock);
            this.isThinkingBlock = true;
            this.isNormalBlock = false
            return;
        }

        if(textFragment.startsWith("</think>")){
            this.isThinkingBlock = false;
            return;
        }

        if(!this.isCodeBlock && !this.isThinkingBlock && !this.isNormalBlock){
            const normalBlock = {type: "normalText", text: ""}
            this.elements.push(normalBlock);
            this.isNormalBlock = true;
        }

        //readers
        if (this.isThinkingBlock) {
            this.readThinkElement(textFragment);
            return;
        }
        if (this.isCodeBlock) {
            this.readCodeBlock(textFragment);
            return;
        }

        if(this.isNormalBlock){
            this.readNormalBlock(textFragment);
            return;
        }
       



        // for (const line of lines) {

          

           


        //     if (line.startsWith("```")) {
        //         const language = line.substring(3).trim();
        //         this.isCodeBlock = true;
        //         this.currentCodeBlock.language = language;
        //         continue;
        //     }

           


        //     if (line.startsWith("###")) {
        //         this.elements.push({ type: 'header', text: line.substring(3).trim() });
        //     } else if (/^\d+\.\s\*\*(.+?)\*\*\:\s(.*)$/.test(line)) {
        //         this.readOlElement(line);
        //     } else if (line.startsWith("-")) {
        //         this.elements.push({ type: 'ulElement', text: line.substring(1).trim() });
        //     }
        //     else if (line.trim() !== "") { 
        //         this.elements.push({ type: 'normalText', text: line });
        //     }
        // }
    }

    readNormalBlock(textFragment){
        this.elements[this.elements.length-1].text += textFragment;
    }

    readCodeBlock(textFragment){
        if(textFragment.startsWith("```")){
            this.isCodeBlock = false;
        }else{
            this.elements[this.elements.length-1].text += textFragment;
        }
    }

    readThinkElement(textFragment){
        if (textFragment.startsWith("</think>")) {
            this.isThinkingBlock = false;
        } else {
            this.elements[this.elements.length-1].text += textFragment;
        }
    }

    // readOlElement(line){
    //     const match = line.match(/^(\d+)\.\s\*\*(.+?)\*\*\:\s(.*)$/);
    //     if (match) {
    //         const header = match[2].trim();
    //         const text = match[3].trim();
    //         this.elements.push({ type: 'olElement', header: header, text: text });
    //     } else {
    //         this.elements.push({ type: 'normalText', text: line });
    //     }
    // }

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
        console.log(html);
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