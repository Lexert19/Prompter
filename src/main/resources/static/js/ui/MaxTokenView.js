class MaxTokenView{
    constructor(){
        document.querySelector('input[name="maxTokens"]').value = window.settings.maxTokens;

    }

    change(event){
        let value = event.target.value;

        if (!isNaN(value) && Number.isInteger(Number(value)) && value != "") {
            window.settings.maxTokens = parseInt(event.target.value);
            window.settings.save();
        } else {
            event.target.value = window.settings.maxTokens;
            document.querySelector('input[name="maxTokens"]').value = window.settings.maxTokens;
        }

    }
}

document.addEventListener('DOMContentLoaded', function() {
    window.maxTokenView = new MaxTokenView();
});