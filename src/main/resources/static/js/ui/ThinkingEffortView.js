class ThinkingEffortView{
    static _instance = null;

    static instance() {
        if (!ThinkingEffortView._instance) {
            ThinkingEffortView._instance = new ThinkingEffortView();
        }
        return ThinkingEffortView._instance;
    }

    constructor(){
        if (ThinkingEffortView._instance) {
            return ThinkingEffortView._instance;
        }
        ThinkingEffortView._instance = this;

        const selectElement = document.getElementById('thinkingEffort');
        if (selectElement) {
            selectElement.value = Settings.instance().thinkingEffort;
            selectElement.addEventListener('change', this.change.bind(this));
        }
    }

    change(event){
        Settings.instance().thinkingEffort = event.target.value;
        Settings.instance().save();
    }
}

document.addEventListener('DOMContentLoaded', function() {
    ThinkingEffortView.instance();
});