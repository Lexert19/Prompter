class Settings{
    constructor(){
        this.memory = false;
        this.cache = false;
        this.maxTokens = 8000;
        this.temperature = 0.0;
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

}

window.settings = new Settings();