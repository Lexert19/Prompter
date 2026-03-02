class ThinkingEffortView{
    constructor(){
        const selectElement = document.getElementById('thinkingEffort');
        if (selectElement) {
            selectElement.value = window.settings.thinkingEffort;
            selectElement.addEventListener('change', this.change.bind(this));
        }
    }

    change(event){
        window.settings.thinkingEffort = event.target.value;
        window.settings.save();
    }
}

document.addEventListener('DOMContentLoaded', function() {
    window.thinkingEffortView = new ThinkingEffortView();
});