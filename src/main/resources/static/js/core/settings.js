class Settings {
    static _instance = null;

    static instance() {
        if (!Settings._instance) {
            Settings._instance = new Settings();
        }
        return Settings._instance;
    }

    constructor() {
        if (Settings._instance) {
            return Settings._instance;
        }
        Settings._instance = this;

        this.memory = false;
        this.cache = false;
        this.maxTokens = 8000;
        this.temperature = 0.0;
        this.system = "";
        this.systemSwitch = false;
        this.project = "";
        this.projectsSwitch = false;
        this.activeHistory = false;
        this.thinkingEffort = "normal"
        this.useSharedKeys = false;
        this.top_p = 0.95;
        this.frequencyPenalty = 0.0;
        this.presencePenalty = 0.0;

        this.provider = "OPENAI";
        this.url = "https://api.openai.com/v1/chat/completions";
        this.model = "gpt-4o-mini";
        this.type = "";

        this.key = "";
        this.models = [

        ];
        this.keys = "";

        this.load();
        this.loadKeys();

        this.initModels();
        this.initUI();
    }

    save() {
        const settingsToSave = {
            memory: this.memory,
            cache: this.cache,
            maxTokens: this.maxTokens,
            temperature: this.temperature,
            provider: this.provider,
            url: this.url,
            model: this.model,
            type: this.type,
            system: this.system,
            systemSwitch: this.systemSwitch,
            top_p: this.top_p,
            project: this.project,
            projectSwitch: this.projectSwitch,
            activeHistory: this.activeHistory,
            thinkingEffort: this.thinkingEffort,
            useSharedKeys: this.useSharedKeys,
            frequencyPenalty: this.frequencyPenalty,
            presencePenalty: this.presencePenalty,
        };
        localStorage.setItem("appSettings", JSON.stringify(settingsToSave));
    }

    load() {
        const savedSettings = JSON.parse(
            localStorage.getItem("appSettings") || "{}"
        );
        Object.assign(this, savedSettings);
    }

    change(event) {
        switch (event.target.name) {
            case "memory":
                Settings.instance().memory = event.target.checked;
                break;
            case "cache":
                Settings.instance().cache = event.target.checked;
                break;
            case "systemSwitch":
                Settings.instance().systemSwitch = event.target.checked;
                break;
            case "chatHistoryInput":
                Settings.instance().activeHistory = event.target.checked;
                break;
            case "projectSwitch":
                Settings.instance().projectSwitch = event.target.checked;
                break;
            case "system":
                Settings.instance().system = event.target.value;
                break;
            case "project":
                Settings.instance().project = event.target.value;
                break;
            case "temperature":
                Settings.instance().temperature = event.target.value / 100;
                break;
            case "top_p":
                this.top_p = parseFloat(event.target.value);
                break;
            case "useSharedKeys":
                Settings.instance().useSharedKeys = event.target.checked;
                break;
            case "frequencyPenalty":
                this.frequencyPenalty = parseFloat(event.target.value);
                break;
            case "presencePenalty":
                this.presencePenalty = parseFloat(event.target.value);
                break;
        }

        this.save();
    }

    changeModel(event) {
        const selectedModel = Settings.instance().models.find(
            (model) => model.name === event.target.value
        );
        if (selectedModel) {
            Settings.instance().model = selectedModel.name;
            Settings.instance().provider = selectedModel.provider;
            Settings.instance().url = selectedModel.url;
            Settings.instance().type = selectedModel.type;

            Settings.instance().key = Settings.instance().keys[selectedModel.provider];
        }
        this.save();
    }



    initModels() {
        hidePages();
        this.chatSettings = document.getElementById("chatSettings");
        this.chatSettings.classList.add("active");
        fetchWithAuth('/api/models/all-models', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        })
            .then(response => response.json())
            .then(models => {
            this.models = [...this.models, ...models.map(model => ({
                name: model.name,
                text: model.text || model.name,
                provider: model.provider,
                url: model.url,
                type: model.type
            }))];
            const uniqueModels = [...new Map(this.models.map(model => [model.name, model])).values()];
            this.models = uniqueModels;
            this.models.sort((a, b) => a.text.localeCompare(b.text));
        })
            .catch(error => console.error('Error loading models:', error));
    }

    initUI() {
        document.getElementById("memory").checked = this.memory;
        document.getElementById("chatHistoryInput").checked = this.activeHistory;
        document.getElementById("cache").checked = this.cache;
        document.getElementById("temperature").value = this.temperature * 100;
        document.getElementById("top_p").value = this.top_p;
        document.getElementById("frequencyPenalty").value = this.frequencyPenalty;
        document.getElementById("presencePenalty").value = this.presencePenalty;
        document.getElementById("useSharedKeys").checked = this.useSharedKeys;
    }

    loadKeys() {
        fetchWithAuth("/api/account/keys", {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
            credentials: "include",
        })
            .then((response) => {
            if (!response.ok) {
                throw new Error("Failed to fetch keys: " + response.statusText);
            }
            return response.json();
        })
            .then((keys) => {
            this.keys = Object.fromEntries(
                Object.entries(keys).map(([providerName, apiToken]) => [providerName.toUpperCase(), apiToken])
            );

            this.key = this.provider ? (this.keys[this.provider.toUpperCase()] || "") : "";
        })
            .catch((error) => {
            console.error("Error loading keys:", error);
        });
    }
}

document.addEventListener('DOMContentLoaded', function() {
    Settings.instance();
});
