class ModelsView{
    constructor(){
        this.models = [];
        this.renderUserModels();
    }


//    renderUserModels(){
//        fetch('/api/models/user-models', {
//            method: 'GET',
//            headers: {
//                'Content-Type': 'application/json'
//            },
//            credentials: 'include'
//        })
//            .then(response => response.json())
//            .then(models => {
//            this.models = models.map(model => ({
//                id: model.id,
//                name: model.name,
//                text: model.text || model.name,
//                provider: model.provider,
//                url: model.url,
//                type: model.type
//            }));
//            this.renderModels(models);
//        })
//    }

    renderUserModels() {
        fetch('/api/models/user-models', { credentials: 'include' })
            .then(res => res.json())
            .then(models => {
            this.models = models;
            this.renderModels(models);
        });
    }


    renderModels(models) {
        const modelsDiv = document.getElementById('models');
        let html = '';
        models.forEach(model => {
            html += `
                <div class="mb-1" style="display: flex; align-items: center;">
                    <span style="flex-grow: 1;">${model.text || model.name}</span>
                    <button class="me-1 rounded-1" data-id="${model.id}" onclick="window.modelsView.editModel(this)">${t.t("edit")}</button>
                    <button class="rounded-1" data-id="${model.id}" onclick="window.modelsView.deleteModel(this)">${t.t("delete")}</button>
                </div>
            `;
        });
        html += '<button class="rounded-1" onclick="window.addEditModelMenu.showAddMenu()">'+t.t("addModel")+'</button>';
        modelsDiv.innerHTML = html;
    }

    editModel(btn) {
        const id = btn.getAttribute('data-id');
        const model = this.models.find(m => m.id == id);
        if (model) window.addEditModelMenu.showEditMenu(model);
    }

    deleteModel(btn) {
        const id = btn.getAttribute('data-id');
        const model = this.models.find(m => m.id == id);
        if (model) window.deleteModelMenu.showDeleteMenu(model);
    }
}

document.addEventListener('DOMContentLoaded', function() {
    window.modelsView = new ModelsView();
});