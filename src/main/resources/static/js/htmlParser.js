class HtmlParser {
    constructor() {
        this.elements = [];
        this.currentLine = '';
        this.isInCodeBlock = false;
        this.currentCodeBlock = null;
    }

    parse(textFragment) {
        // Add fragment to current line
        this.currentLine += textFragment;

        // Process complete lines
        while (this.currentLine.includes('\n')) {
            const lineEndIndex = this.currentLine.indexOf('\n');
            const completeLine = this.currentLine.substring(0, lineEndIndex).trim();
            this.currentLine = this.currentLine.substring(lineEndIndex + 1);

            // Handle code blocks
            if (this.isInCodeBlock) {
                if (completeLine === '```') {
                    this.isInCodeBlock = false;
                    this.elements.push(this.currentCodeBlock);
                    this.currentCodeBlock = null;
                } else {
                    this.currentCodeBlock.text += completeLine + '\n';
                }
                continue;
            }

            // Check for new code block
            if (completeLine.startsWith('```')) {
                this.isInCodeBlock = true;
                this.currentCodeBlock = {
                    type: 'code',
                    language: completeLine.substring(3),
                    text: ''
                };
                continue;
            }

            // Parse other elements
            if (completeLine.startsWith('###')) {
                this.elements.push({
                    type: 'header',
                    text: completeLine.substring(3).trim()
                });
            } else if (completeLine.match(/^\d+\.\s+\*\*/)) {
                const matches = completeLine.match(/^(\d+)\.\s+\*\*(.+?)\*\*:\s*(.+)$/);
                if (matches) {
                    this.elements.push({
                        type: 'olElement',
                        header: matches[2],
                        text: matches[3]
                    });
                }
            } else if (completeLine.startsWith('-')) {
                this.elements.push({
                    type: 'ulElement',
                    text: completeLine.substring(1).trim()
                });
            } else if (completeLine.startsWith('**')) {
                this.elements.push({
                    type: 'strong',
                    text: completeLine.replace(/\*\*/g, '').trim()
                });
            } else if (completeLine) {
                this.elements.push({
                    type: 'normalText',
                    text: completeLine
                });
            }
        }
    }

    toHTML() {
        return this.elements.map(element => {
            switch (element.type) {
                case 'header':
                    return `<h3>${element.text}</h3>`;
                case 'strong':
                    return `<strong>${element.text}</strong>`;
                case 'olElement':
                    return `<li><strong>${element.header}</strong>: ${element.text}</li>`;
                case 'code':
                    return `<pre><code class="language-${element.language}">${element.text}</code></pre>`;
                case 'ulElement':
                    return `<li>${element.text}</li>`;
                case 'normalText':
                    return `<p>${element.text}</p>`;
                default:
                    return '';
            }
        }).join('\n');
    }
}