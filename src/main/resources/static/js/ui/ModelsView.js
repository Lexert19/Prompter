class ModelsView{
    constructor(){
        this.models = [];
        this.renderUserModels();
    }


    renderUserModels() {
        fetchWithCsrf('/api/models/user-models', { credentials: 'include' })
            .then(res => res.json())
            .then(models => {
            this.models = models;
            this.renderModels(models);
        });
    }


    renderModels(models) {
        const modelsDiv = document.getElementById('models-list');
        let html = '';
        models.forEach(model => {
            html += `
    <div class="model-item">
        <span class="model-name">${model.text || model.name}</span>
        <div class="model-actions">
            <button class="btn-edit" data-id="${model.id}" onclick="window.modelsView.editModel(this)" title="${t.t("edit")}">
                <i class="fas fa-pencil-alt"></i>
            </button>
            <button class="btn-delete" data-id="${model.id}" onclick="window.modelsView.deleteModel(this)" title="${t.t("delete")}">
                <i class="fas fa-trash-alt"></i>
            </button>
        </div>
    </div>
`;
        });
        //html += '<button class="rounded-1" onclick="window.addEditModelMenu.showAddMenu()">'+t.t("addModel")+'</button>';
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