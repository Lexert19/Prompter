
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

    getType() {
        return Settings.instance().type;
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

    getTemperature() {
        return Settings.instance().temperature;
    }

    getKey() {
        return Settings.instance().key;
    }

    getProviderStrategy() { return Settings.instance().providerStrategy || this.getProvider(); }

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

        const base = {
            model: this.getModel(),
            url: this.getUrl(),
            provider: this.getProvider(),
            providerStrategy: this.getProviderStrategy(),
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
            useSharedKeys: Settings.instance().useSharedKeys,
            includeAdvancedParams: Settings.instance().includeAdvancedParams
        }

        if (this.getProvider() === "OPENROUTER") {
            const settings = Settings.instance();
            const orderRaw = settings.openRouterProviderOrder;
            if (orderRaw && orderRaw.trim()) {
                const orderArray = orderRaw.split(',').map(s => s.trim()).filter(Boolean);
                if (orderArray.length > 0) {
                    base.providerConfig = {
                        order: orderArray,
                        allow_fallbacks: settings.openRouterAllowFallbacks !== false
                    };
                }
            }
        }

        return JSON.stringify(base);
    }

    mapContent(content) {
        const baseContent = {
            type: content.type,
            text: content.text || "",
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

    _getItemLength(item) {
        if (item == null) return 0;
        if (typeof item === "string") return item.length;
        if (typeof item.getTextContentLength === 'function') return item.getTextContentLength();
        if (typeof item.content === 'string') return item.content.length;
        if (typeof item.text === 'string') return item.text.length;
        if (typeof item.textContent === 'string') return item.textContent.length;
        if (typeof item.fragment === 'string') return item.fragment.length;
        return 0;
    }

    _getDocumentLength(doc) {
        return this._getItemLength(doc);
    }

    _getMessageTextLength(m) {
        if (!m) return 0;
        let len = m.text?.length || 0;

        if (m.documents) {
            for (const doc of m.documents) {
                len += this._getItemLength(doc);
            }
        }
        if (m.context) {
            for (const ctx of m.context) {
                len += this._getItemLength(ctx);
            }
        }
        return len;
    }

    calculateContextSize() {
        let total = this.getSystem()?.length || 0;

        this.messages.forEach(msg => {
            total += this._getMessageTextLength(msg);
            total += (msg.images?.length || 0) * 1000;
        });

        return total;
    }

    getStats() {
        const messages = this.messages || [];

        const total = messages.length;
        const user = messages.filter(m => m.role === 'user').length;
        const assistant = messages.filter(m => m.role === 'assistant').length;

        const textLength = messages.reduce(
            (sum, m) => sum + this._getMessageTextLength(m),
            0
        );

        const images = messages.reduce(
            (sum, m) => sum + (m.images ? m.images.length : 0),
            0
        );

        const documents = messages.reduce(
            (sum, m) => sum + (m.documents ? m.documents.length : 0),
            0
        );

        const contextSize = this.calculateContextSize
            ? this.calculateContextSize()
            : textLength + images * 1000;

        return {
            total,
            user,
            assistant,
            textLength,
            images,
            documents,
            contextSize,
            estimatedTokens: Math.round(textLength / 3.5),
            estimatedContextTokens: Math.round(contextSize / 3.5)
        };
    }
}
