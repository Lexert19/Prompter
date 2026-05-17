
class RequestBuilder {
    constructor() {
        this.messages = [];
        this.stream = true;
    }

    getSystem() {
        if (Settings.instance().systemSwitch) {
            return Settings.instance().system;
        }
        return "";
    }

    getFrequencyPenalty() {
        return Settings.instance().frequencyPenalty;
    }

    getPresencePenalty() {
        return Settings.instance().presencePenalty;
    }

    getType() {
        return Settings.instance().type;
    }

    getTopP(){
        return Settings.instance().top_p;
    }

    getProvider() {
        return Settings.instance().provider;
    }

    getModel() {
        return Settings.instance().model;
    }

    getUrl() {
        return Settings.instance().url;
    }

    getMaxTokens() {
        return Settings.instance().maxTokens;
    }

    getTemperature() {
        return Settings.instance().temperature;
    }

    getKey() {
        return Settings.instance().key;
    }

    toRequestJSON() {
        let messagesToInclude;
        if (Settings.instance().memory) {
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
            top_p: this.getTopP(),
            frequency_penalty: this.getFrequencyPenalty(),
            presence_penalty: this.getPresencePenalty(),
            temperature: this.getTemperature(),
            stream: this.stream,
            type: this.getType(),
            key: this.getKey(),
            ...(Settings.instance().thinkingEffort !== "lack" && {
                reasoningEffort: Settings.instance().thinkingEffort
            }),
            system: this.getSystem(),
            useSharedKeys: Settings.instance().useSharedKeys
        });
    }

    mapContent(content) {
        const baseContent = {
            type: content.type,
            text: content.text || "",
            cache: content.cache || false,
        };

        switch (content.type) {
            case "image_id":
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

    calculateContextSize() {
        let total = 0;
        let messagesToInclude;
        if (Settings.instance().memory) {
            messagesToInclude = [...this.messages];
        } else {
            messagesToInclude = [
                ...this.messages.filter((message) => message.cache === true),
                this.messages[this.messages.length - 1],
            ];
        }

        messagesToInclude.forEach(msg => {
            total += msg.text.length;
            total += (msg.images.length * 1000);
        });

        return total;
    }
}
