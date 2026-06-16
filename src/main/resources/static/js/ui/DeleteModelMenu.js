class DeleteModelMenu {
    static _instance = null;

    static instance() {
        if (!DeleteModelMenu._instance) {
            DeleteModelMenu._instance = new DeleteModelMenu();
        }
        return DeleteModelMenu._instance;
    }

    constructor() {
        if (DeleteModelMenu._instance) {
            return DeleteModelMenu._instance;
        }
        DeleteModelMenu._instance = this;
    }

    showDeleteMenu(model) {

        const confirmDeleteModel = t.t("confirmDeleteModel",  { name: model.name });
        const html = `
            <p>${confirmDeleteModel}</p>
            <div class="d-flex justify-content-between" style="gap: 10px; margin-top: 20px;">
                <button id="cancelDeleteBtn">${t.t("cancel")}</button>
                <button id="confirmDeleteBtn" class="btn-danger">${t.t("delete")}</button>
            </div>
        `;
        Modal.instance().open(t.t("confirmDelete"), html, null);
        document.getElementById('confirmDeleteBtn').addEventListener('click', () => this.deleteModel(model.id));
        document.getElementById('cancelDeleteBtn').addEventListener('click', () => Modal.instance().close());
    }

    deleteModel(id) {
        fetchWithAuth(`/api/models/user-models/${id}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' }
        })
            .then(res => res.text())
            .then(() => {
            ModelsView.instance().renderUserModels();
            ModelSelector.instance().syncModels();
            Modal.instance().close();
        })
            .catch(console.error);
    }
}
DeleteModelMenu.instance()