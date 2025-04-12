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

        this.provider = "OPENAI";
        this.url = "https://api.openai.com/v1/chat/completions";
        this.model = "gpt-4o-mini";
        this.type = "";

        this.key = "";
        this.models = [
            {
                name: "gpt-4o-mini",
                text: "gpt-4o-mini",
                provider: "OPENAI",
                url: "https://api.openai.com/v1/chat/completions",
                type: "vision",
            },
            {
                name: "o3-mini",
                text: "o3-mini",
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
                name: "claude-3-5-haiku-latest",
                text: "Claude Haiku 3.5",
                provider: "ANTHROPIC",
                url: "https://api.anthropic.com/v1/messages",
                type: "text",
            },
            {
                name: "claude-3-7-sonnet-20250219",
                text: "Claude Sonnet 3.7",
                provider: "ANTHROPIC",
                url: "https://api.anthropic.com/v1/messages",
                type: "vision",
            },
            {
                name: "meta-llama/Llama-4-Maverick-17B-128E-Instruct-FP8",
                text: "meta-llama/Llama-4-Maverick-17B-128E-Instruct-FP8",
                provider: "DEEPINFRA",
                type: "text",
                url: "https://api.deepinfra.com/v1/openai/chat/completions",
            },
            {
                name: "deepseek-ai/DeepSeek-V3-0324",
                text: "deepinfra/DeepSeek-V3-new",
                provider: "DEEPINFRA",
                type: "text",
                url: "https://api.deepinfra.com/v1/openai/chat/completions",
            },
            {
                name: "deepseek-ai/DeepSeek-R1",
                text: "deepinfra/DeepSeek-R1",
                provider: "DEEPINFRA",
                type: "text",
                url: "https://api.deepinfra.com/v1/openai/chat/completions",
            },
            {
                name: "deepseek-ai/DeepSeek-R1-Turbo",
                text: "deepinfra/DeepSeek-R1-Turbo",
                provider: "DEEPINFRA",
                type: "text",
                url: "https://api.deepinfra.com/v1/openai/chat/completions",
            },
            {
                name: "Qwen/QwQ-32B",
                text: "Qwen/QwQ-32B",
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
                name: "gemini-2.0-flash-thinking-exp-01-21",
                text: "gemini-2.0-flash-thinking-exp-01-21",
                provider: "GEMINI",
                type: "vision",
                url: "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
            },
            {
                name: "gemini-2.5-pro-exp-03-25",
                text: "gemini-2.5.pro-exp-03-25",
                provider: "GEMINI",
                type: "vision",
                url: "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
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
            case "maxTokens":
                window.settings.maxTokens = parseInt(event.target.value);
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
        this.models.forEach((model) => {
            modelOptions.innerHTML += `<option value="${model.name}">${model.text}</option>`;
        });
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

        document.querySelector('input[name="maxTokens"]').value = this.maxTokens;

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
