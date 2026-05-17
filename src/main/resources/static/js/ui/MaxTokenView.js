class MaxTokenView{
    static _instance = null;

    static instance() {
        if (!MaxTokenView._instance) {
            MaxTokenView._instance = new MaxTokenView();
        }
        return MaxTokenView._instance;
    }

    constructor(){
        if (MaxTokenView._instance) {
            return MaxTokenView._instance;
        }
        MaxTokenView._instance = this;


        document.querySelector('input[name="maxTokens"]').value = Settings.instance().maxTokens;
    }

    change(event){
        let value = event.target.value;

        if (!isNaN(value) && Number.isInteger(Number(value)) && value != "") {
            Settings.instance().maxTokens = parseInt(event.target.value);
            Settings.instance().save();
        } else {
            event.target.value = Settings.instance().maxTokens;
            document.querySelector('input[name="maxTokens"]').value = Settings.instance().maxTokens;
        }

    }
}

document.addEventListener('DOMContentLoaded', function() {
    MaxTokenView.instance();
});