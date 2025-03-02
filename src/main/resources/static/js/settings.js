class Settings{
    constructor(){
        this.memory = false;
        this.cache = false;
        this.maxTokens = 8000;
        this.temperature = 0.0;

        this.provider = "OPENAI";
        this.url = "https://api.openai.com/v1/chat/completions";
        this.model = "gpt-4o-mini";
        this.type = "";

        this.key = "";
        this.models = [
            { name: "gpt-4o-mini", text: "gpt-4o-mini", provider: "OPENAI", url: "https://api.openai.com/v1/chat/completions", type: "vision" },
            { name: "o3-mini", text: "o3-mini", provider: "OPENAI", url: "https://api.openai.com/v1/chat/completions", type: "vision" },

            { name: "gpt-4o", text: "gpt-4o", provider: "OPENAI", url: "https://api.openai.com/v1/chat/completions", type: "vision" },
            { name: "gpt-4-turbo", text: "gpt-4-turbo", provider: "OPENAI", url: "https://api.openai.com/v1/chat/completions", type: "vision" },
            { name: "claude-3-haiku-20240307", text: "Claude Haiku 3", provider: "ANTHROPIC", url: "https://api.anthropic.com/v1/messages", type: "vision" },
            { name: "claude-3-7-sonnet-20250219", text: "Claude Sonnet 3.7", provider: "ANTHROPIC", url: "https://api.anthropic.com/v1/messages", type: "vision" },
            { name: "claude-3-opus-20240229", text: "Claude Opus 3", provider: "ANTHROPIC", url: "https://api.anthropic.com/v1/messages", type: "vision" },
            {
                name: "meta-llama/Llama-3.3-70B-Instruct-Turbo",
                text: "meta-llama/Llama-3.3-70B-Instruct-Turbo",
                provider: "DEEPINFRA",
                type: "text",
                url: "https://api.deepinfra.com/v1/openai/chat/completions"
            },
            {
                name: "deepseek-ai/DeepSeek-V3",
                text: "deepinfra/DeepSeek-V3",
                provider: "DEEPINFRA",
                type: "text",
                url: "https://api.deepinfra.com/v1/openai/chat/completions"
            },
            {
                name: "deepseek-ai/DeepSeek-R1",
                text: "deepinfra/DeepSeek-R1",
                provider: "DEEPINFRA",
                type: "text",
                url: "https://api.deepinfra.com/v1/openai/chat/completions"
            },
            {
                name: "deepseek-chat",
                text: "deepseek-v3",
                provider: "DEEPSEEK",
                type: "text",
                url: "https://api.deepseek.com/chat/completions"
            },
            {
                name: "deepseek-reasoner",
                text: "deepseek-r1",
                provider: "DEEPSEEK",
                type: "text",
                url: "https://api.deepseek.com/chat/completions"
            },
            {
                name: "gemini-2.0-flash-thinking-exp-01-21",
                text: "gemini-2.0-flash-thinking-exp-01-21",
                provider: "GEMINI",
                type: "text",
                url: "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions"
            }
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
            model: this.model
        };
        localStorage.setItem("appSettings", JSON.stringify(settingsToSave));
    }

    load() {
        const savedSettings = JSON.parse(localStorage.getItem("appSettings") || "{}");
        Object.assign(this, savedSettings);
    }


    change(event){
        switch (event.target.name) {
            case "memory":
                window.settings.memory = event.target.checked;
                break;
            case "cache":
                window.settings.cache = event.target.checked;
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
        const selectedModel = window.settings.models.find(model => model.name === event.target.value);
        if (selectedModel) {
            window.settings.model = selectedModel.name;
            window.settings.provider = selectedModel.provider;
            window.settings.url = selectedModel.url;

            window.settings.key = window.settings.keys[selectedModel.provider];
        }
        this.save(); 
    }

    initModels(){
        hidePages();
        this.chatSettings = document.getElementById("chatSettings");
        this.chatSettings.classList.add("active");
        this.models.forEach(model => {
            modelOptions.innerHTML += `<option value="${model.name}">${model.text}</option>`;
        });
    }

    initUI() {
        this.models.forEach(model => {
            modelOptions.innerHTML += `<option value="${model.name}">${model.text}</option>`;
        });

        document.getElementById('memory').checked = this.memory;
        document.getElementById('cache').checked = this.cache;
        
        document.querySelector('input[name="maxTokens"]').value = this.maxTokens;
        
        document.getElementById('temperature').value = this.temperature * 100;
        
        const modelSelect = document.getElementById('modelOptions');
        modelSelect.innerHTML = this.models
            .map(model => `<option value="${model.name}">${model.name}</option>`)
            .join('');
        modelSelect.value = this.model;
    }

    loadKeys() {
        fetch('/account/keys', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch keys: ' + response.statusText);
                }
                return response.json();
            })
            .then(keys => {
                this.keys = keys;
                this.key = this.keys[this.provider];
            })
            .catch(error => {
                console.error('Error loading keys:', error);
            });
    }

}

window.settings = new Settings();