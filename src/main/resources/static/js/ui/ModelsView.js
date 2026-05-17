class ModelsView{
    static _instance = null;

    static instance() {
        if (!ModelsView._instance) {
            ModelsView._instance = new ModelsView();
        }
        return ModelsView._instance;
    }

    constructor(){
        if (ModelsView._instance) {
            return ModelsView._instance;
        }
        ModelsView._instance = this;

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
            <button class="btn-edit" data-id="${model.id}" onclick="ModelsView.instance().editModel(this)" title="${t.t("edit")}">
                <i class="fas fa-pencil-alt"></i>
            </button>
            <button class="btn-delete" data-id="${model.id}" onclick="ModelsView.instance().deleteModel(this)" title="${t.t("delete")}">
                <i class="fas fa-trash-alt"></i>
            </button>
        </div>
    </div>
`;
        });
        modelsDiv.innerHTML = html;
    }

    editModel(btn) {
        const id = btn.getAttribute('data-id');
        const model = this.models.find(m => m.id == id);
        if (model) AddEditModelMenu.instance().showEditMenu(model);
    }

    deleteModel(btn) {
        const id = btn.getAttribute('data-id');
        const model = this.models.find(m => m.id == id);
        if (model) DeleteModelMenu.instance().showDeleteMenu(model);
    }
}

document.addEventListener('DOMContentLoaded', function() {
    ModelsView.instance();
});