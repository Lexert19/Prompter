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

        this.provider = "OPENAI";
        this.url = "https://api.openai.com/v1/chat/completions";
        this.model = "gpt-4o-mini";
        this.type = "";

        this.key = "";
        this.models = [
            {
                name: "o4-mini",
                text: "o4-mini",
                provider: "OPENAI",
                url: "https://api.openai.com/v1/chat/completions",
                type: "vision",
            },
            {
                name: "gpt-4o",
                text: "gpt-4o",
                provider: "OPENAI",
                url: "https://api.openai.com/v1/chat/completions",
                type: "vision",
            },
            {
                name: "claude-3-7-sonnet-20250219",
                text: "Claude Sonnet 3.7",
                provider: "ANTHROPIC",
                url: "https://api.anthropic.com/v1/messages",
                type: "vision",
            },

            {
                name: "deepseek-ai/DeepSeek-V3-0324",
                text: "deepinfra/DeepSeek-V3",
                provider: "DEEPINFRA",
                type: "text",
                url: "https://api.deepinfra.com/v1/openai/chat/completions",
            },
            {
                name: "deepseek-ai/DeepSeek-R1-0528",
                text: "deepseek-ai/DeepSeek-R1-0528",
                provider: "DEEPINFRA",
                type: "text",
                url: "https://api.deepinfra.com/v1/openai/chat/completions",
            },

            {
                name: "deepseek-chat",
                text: "deepseek-v3",
                provider: "DEEPSEEK",
                type: "text",
                url: "https://api.deepseek.com/chat/completions",
            },
            {
                name: "deepseek-reasoner",
                text: "deepseek-r1",
                provider: "DEEPSEEK",
                type: "text",
                url: "https://api.deepseek.com/chat/completions",
            },
            {
                name: "gemini-2.0-flash",
                text: "gemini-2.0-flash",
                provider: "GEMINI",
                type: "vision",
                url: "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
            },
            {
                name: "qwen-qwq-32b",
                text: "qwen-qwq-32b",
                provider: "GROQ",
                type: "text",
                url: "https://api.groq.com/openai/v1/chat/completions",
            },
        ];
        this.keys = "";

        this.load();
        this.loadKeys();

        this.loadProjects();
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
            project: this.project,
            projectSwitch: this.projectSwitch,
            activeHistory: this.activeHistory,
            thinkingEffort: this.thinkingEffort,
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
//            case "maxTokens":
//                window.settings.maxTokens = parseInt(event.target.value);
//                break;
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

    loadProjects() {
        fetch("/api/projects", {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
            credentials: "include",
        })
            .then((response) => {
            if (!response.ok) {
                throw new Error("Failed to fetch projects: " + response.statusText);
            }
            return response.json();
        })
            .then((projects) => {
            const projectSelect = document.getElementById("project");
            projectSelect.innerHTML = "";

            projects.forEach((project) => {
                const option = document.createElement("option");
                option.value = project.id;
                option.textContent = project.name;
                projectSelect.appendChild(option);
            });

            projectSelect.value = this.project;
        })
            .catch((error) => {
            console.error("Error loading projects:", error);
        });
    }

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
            const modelSelect = document.getElementById("modelOptions");
            modelSelect.innerHTML = this.models
                .map(model => `<option value="${model.name}">${model.text}</option>`)
                .join("");
            modelSelect.value = this.model;
        })
            .catch(error => console.error('Error loading models:', error));
    }

    initUI() {
        this.models.forEach((model) => {
            modelOptions.innerHTML += `<option value="${model.name}">${model.text}</option>`;
        });

        document.getElementById("memory").checked = this.memory;
        document.getElementById("chatHistoryInput").checked = this.activeHistory;
        document.getElementById("cache").checked = this.cache;
        document.getElementById("system").value = this.system;
        document.getElementById("systemSwitch").checked = this.systemSwitch;
        document.getElementById("projectSwitch").checked = this.projectSwitch;

        //document.querySelector('input[name="maxTokens"]').value = this.maxTokens;

        document.getElementById("temperature").value = this.temperature * 100;

        const modelSelect = document.getElementById("modelOptions");
        modelSelect.innerHTML = this.models
            .map((model) => `<option value="${model.name}">${model.name}</option>`)
            .join("");
        modelSelect.value = this.model;
    }

    loadKeys() {
        fetch("/account/keys", {
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

window.settings = new Settings();
