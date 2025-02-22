class HtmlParser {
    constructor() {
        this.elements = [];
        this.currentElement = null;
        this.buffer = '';
    }

    parse(textFragment) {
        this.buffer += textFragment;
        
        // Process headers (###)
        if (this.buffer.includes('###')) {
            const headerStart = this.buffer.indexOf('###');
            const headerEnd = this.buffer.indexOf('\n', headerStart);
            
            if (headerEnd !== -1) {
                this.elements.push({
                    type: 'header',
                    text: this.buffer.substring(headerStart + 3, headerEnd).trim()
                });
                this.buffer = this.buffer.substring(headerEnd + 1);
            }
        }

        // Process list elements (\n-)
        if (this.buffer.includes('\n-')) {
            const listStart = this.buffer.indexOf('\n-');
            const listEnd = this.buffer.indexOf('\n', listStart + 2);
            
            if (listEnd !== -1) {
                this.elements.push({
                    type: 'listElement',
                    text: this.buffer.substring(listStart + 2, listEnd).trim()
                });
                this.buffer = this.buffer.substring(listEnd + 1);
            }
        }

        // Process code blocks (```language\n...```)
        if (this.buffer.includes('```')) {
            const codeStart = this.buffer.indexOf('```');
            const languageEnd = this.buffer.indexOf('\n', codeStart);
            const codeEnd = this.buffer.indexOf('```\n', languageEnd);
            
            if (codeEnd !== -1) {
                const language = this.buffer.substring(codeStart + 3, languageEnd);
                this.elements.push({
                    type: 'code',
                    language: language,
                    text: this.buffer.substring(languageEnd + 1, codeEnd).trim()
                });
                this.buffer = this.buffer.substring(codeEnd + 4);
            }
        }

        // Process normal text (\n\n...\n\n)
        if (this.buffer.includes('\n\n')) {
            const textStart = this.buffer.indexOf('\n\n');
            const textEnd = this.buffer.indexOf('\n\n', textStart + 2);
            
            if (textEnd !== -1) {
                this.elements.push({
                    type: 'normalText',
                    text: this.buffer.substring(textStart + 2, textEnd).trim()
                });
                this.buffer = this.buffer.substring(textEnd + 2);
            }
        }

        // Process strong text (**...**)
        if (this.buffer.includes('**')) {
            const strongStart = this.buffer.indexOf('**');
            const strongEnd = this.buffer.indexOf('**', strongStart + 2);
            
            if (strongEnd !== -1) {
                this.elements.push({
                    type: 'strong',
                    text: this.buffer.substring(strongStart + 2, strongEnd).trim()
                });
                this.buffer = this.buffer.substring(strongEnd + 2);
            }
        }
    }

    toHTML() {
        return this.elements.map(element => {
            switch (element.type) {
                case 'header':
                    return `<h1><pre><code>${element.text}</code></pre></h1>`;
                case 'listElement':
                    return `<li><pre><code>${element.text}</code></pre></li>`;
                case 'code':
                    return `<pre><code class="language-${element.language}">${element.text}</code></pre>`;
                case 'normalText':
                    return `<p><pre><code>${element.text}</code></pre></p>`;
                case 'strong':
                    return `<strong><pre><code>${element.text}</code></pre></strong>`;
                default:
                    return '';
            }
        }).join('\n');
    }
}