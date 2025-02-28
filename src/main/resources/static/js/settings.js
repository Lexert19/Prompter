class Settings{
    constructor(){
        this.memory = false;
        this.cache = false;
        this.maxTokens = 8000;
        this.temperature = 0.0;

        this.provider = "OPENAI";
        this.url = "https://api.openai.com/v1/chat/completions";
        this.model = "gpt-4o-mini";
        this.key = "";
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
    }

    changeModel(event) {
        const selectedModel = window.chat.models.find(model => model.name === event.target.value);
        if (selectedModel) {
            window.settings.model = selectedModel.name;
            window.settings.provider = selectedModel.provider;
            window.settings.url = selectedModel.url;

            window.settings.key = window.chat.keys[selectedModel.provider];
        }
    }

}

window.settings = new Settings();