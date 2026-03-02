class DeleteModelMenu {
    showDeleteMenu(model) {
        const html = `
            <p>Czy na pewno chcesz usunąć model <strong>${model.name}</strong>?</p>
            <div class="d-flex justify-content-between" style="gap: 10px; margin-top: 20px;">
                <button id="cancelDeleteBtn">Anuluj</button>
                <button id="confirmDeleteBtn" class="btn-danger">Usuń</button>
            </div>
        `;
        window.modal.open('Potwierdzenie usunięcia', html, null);
        document.getElementById('confirmDeleteBtn').addEventListener('click', () => this.deleteModel(model.id));
        document.getElementById('cancelDeleteBtn').addEventListener('click', () => window.modal.close());
    }

    deleteModel(id) {
        fetch(`/api/models/user-models/${id}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' }
        })
            .then(res => res.text())
            .then(() => {
            window.modelsView.renderUserModels();
            window.modal.close();
        })
            .catch(console.error);
    }
}
window.deleteModelMenu = new DeleteModelMenu();