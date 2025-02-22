class HtmlParser {
    constructor() {
        this.elements = [];
        this.currentLine = '';
        this.isInCodeBlock = false;
        this.isInThinkingBlock = false;
        this.codeContent = '';
        this.thinkingContent = '';
    }

    parse(textFragment) {
        // Split the fragment into lines and process each one
        const lines = textFragment.split('\n');
        
        for (const line of lines) {
            if (this.isInCodeBlock) {
                if (line.trim() === '```') {
                    this.isInCodeBlock = false;
                    this.elements.push({
                        type: 'code',
                        text: this.codeContent.trim()
                    });
                    this.codeContent = '';
                } else {
                    this.codeContent += line + '\n';
                }
                continue;
            }

            if (this.isInThinkingBlock) {
                if (line.trim() === '</think>') {
                    this.isInThinkingBlock = false;
                    this.elements.push({
                        type: 'thinking',
                        text: this.thinkingContent.trim()
                    });
                    this.thinkingContent = '';
                } else {
                    this.thinkingContent += line + '\n';
                }
                continue;
            }

            // Check for code block start
            if (line.startsWith('```')) {
                this.isInCodeBlock = true;
                continue;
            }

            // Check for thinking block start
            if (line.trim() === '<think>') {
                this.isInThinkingBlock = true;
                continue;
            }

            // Check for header
            if (line.startsWith('###')) {
                this.elements.push({
                    type: 'header',
                    text: line.substring(3).trim()
                });
                continue;
            }

            // Check for ordered list element
            const olMatch = line.match(/^(\d+)\.\s+\*\*(.*?)\*\*:\s*(.*)$/);
            if (olMatch) {
                this.elements.push({
                    type: 'olElement',
                    header: olMatch[2].trim(),
                    text: olMatch[3].trim()
                });
                continue;
            }

            // Check for unordered list element
            if (line.startsWith('-')) {
                this.elements.push({
                    type: 'ulElement',
                    text: line.substring(1).trim()
                });
                continue;
            }

            // Check for strong text
            if (line.startsWith('**') && line.endsWith('**')) {
                this.elements.push({
                    type: 'strong',
                    text: line.slice(2, -2).trim()
                });
                continue;
            }

            // Normal text
            if (line.trim()) {
                this.elements.push({
                    type: 'normalText',
                    text: line.trim()
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
                    return `<div class="ol-item"><strong>${element.header}</strong>: ${element.text}</div>`;
                case 'code':
                    return `<pre><code>${element.text}</code></pre>`;
                case 'normalText':
                    return `<p>${element.text}</p>`;
                case 'ulElement':
                    return `<ul><li>${element.text}</li></ul>`;
                case 'thinking':
                    return `<div class="thinking">${element.text}</div>`;
                default:
                    return '';
            }
        }).join('\n');
    }
}