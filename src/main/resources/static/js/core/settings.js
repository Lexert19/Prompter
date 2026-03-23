class Settings {
    constructor() {
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
                window.settings.memory = event.target.checked;
                break;
            case "cache":
                window.settings.cache = event.target.checked;
                break;
            case "systemSwitch":
                window.settings.systemSwitch = event.target.checked;
                break;
            case "chatHistoryInput":
                window.settings.activeHistory = event.target.checked;
                break;
            case "projectSwitch":
                window.settings.projectSwitch = event.target.checked;
                break;
            case "system":
                window.settings.system = event.target.value;
                break;
            case "project":
                window.settings.project = event.target.value;
                break;
            case "temperature":
                window.settings.temperature = event.target.value / 100;
                break;
            case "top_p":
                this.top_p = parseFloat(event.target.value);
                break;
            case "useSharedKeys":
                window.settings.useSharedKeys = event.target.checked;
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
        const selectedModel = window.settings.models.find(
            (model) => model.name === event.target.value
        );
        if (selectedModel) {
            window.settings.model = selectedModel.name;
            window.settings.provider = selectedModel.provider;
            window.settings.url = selectedModel.url;
            window.settings.type = selectedModel.type;

            window.settings.key = window.settings.keys[selectedModel.provider];
        }
        this.save();
    }

//    loadProjects() {
//        fetch("/api/projects", {
//            method: "GET",
//            headers: {
//                "Content-Type": "application/json",
//            },
//            credentials: "include",
//        })
//            .then((response) => {
//            if (!response.ok) {
//                throw new Error("Failed to fetch projects: " + response.statusText);
//            }
//            return response.json();
//        })
//            .then((projects) => {
//            const projectSelect = document.getElementById("project");
//            projectSelect.innerHTML = "";
//
//            projects.forEach((project) => {
//                const option = document.createElement("option");
//                option.value = project.id;
//                option.textContent = project.name;
//                projectSelect.appendChild(option);
//            });
//
//        })
//            .catch((error) => {
//            console.error("Error loading projects:", error);
//        });
//    }

    initModels() {
        hidePages();
        this.chatSettings = document.getElementById("chatSettings");
        this.chatSettings.classList.add("active");
        fetch('/api/models/all-models', {
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
//        this.models.forEach((model) => {
//            modelOptions.innerHTML += `<option value="${model.name}">${model.text}</option>`;
//        });

        //window.systemPromptSelector.render();

        document.getElementById("memory").checked = this.memory;
        document.getElementById("chatHistoryInput").checked = this.activeHistory;
        document.getElementById("cache").checked = this.cache;
        document.getElementById("temperature").value = this.temperature * 100;
        document.getElementById("useSharedKeys").checked = this.useSharedKeys;
    }

    loadKeys() {
        fetch("/api/account/keys", {
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
            this.keys = keys;
            this.key = this.keys[this.provider];
        })
            .catch((error) => {
            console.error("Error loading keys:", error);
        });
    }
}

document.addEventListener('DOMContentLoaded', function() {
    window.settings = new Settings();
});
