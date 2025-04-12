
class RequestBuilder {
    constructor() {
        this.messages = [];
        this.stream = true;
    }

    getSystem() {
        if (document.getElementById("systemSwitch").checked) {
            return window.settings.system;
        }
        return "";
    }

    getType() {
        return window.settings.type;
    }

    getProvider() {
        return window.settings.provider;
    }

    getModel() {
        return window.settings.model;
    }

    getUrl() {
        return window.settings.url;
    }

    getMaxTokens() {
        return window.settings.maxTokens;
    }

    getTemperature() {
        return window.settings.temperature;
    }

    getKey() {
        return window.settings.key;
    }

    toRequestJSON() {
        let messagesToInclude;
        if (window.settings.memory) {
            messagesToInclude = [...this.messages];
        } else {
            messagesToInclude = [
                ...this.messages.filter((message) => message.cache === true),
                this.messages[this.messages.length - 1],
            ];
        }

        return JSON.stringify({
            model: this.getModel(),
            url: this.getUrl(),
            provider: this.getProvider(),
            messages: messagesToInclude.map((message) => ({
                role: message.role,
                content: message.buildContent()
            })),
            maxTokens: this.getMaxTokens(),
            temperature: this.getTemperature(),
            stream: this.stream,
            type: this.getType(),
            key: this.getKey(),
            system: this.getSystem(),
        });
    }

    mapContent(content) {
        const baseContent = {
            type: content.type,
            text: content.text || "",
            cache: content.cache || false,
        };

        switch (content.type) {
            case "image_url":
                return {
                    ...baseContent,
                    url: content.url,
                    mediaType: content.mediaType,
                };

            case "image":
                return {
                    ...baseContent,
                    mediaType: content.mediaType,
                    data: content.data,
                };

            default:
                return baseContent;
        }
    }

    addMessage(message) {
        this.messages.push(message);
        return this;
    }
}
