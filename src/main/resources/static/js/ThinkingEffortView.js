class ThinkingEffortView{
    change(event){
        window.settings.thinkingEffort = event.target.value;
        window.settings.save();
    }
}

window.thinkingEffortView = new ThinkingEffortView();