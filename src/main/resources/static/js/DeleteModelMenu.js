class DeleteModelMenu {
    constructor() {
        this.modelId = null;
        this.setListeners();
    }
    showDeleteMenu(model) {
        document.getElementById("overlay").style.display = "block";
        document.getElementById("deletedModelMenu").style.display = "block";
        this.modelId = model.id;
        document.getElementById("deleteModelConfirmText").innerText = `Are you sure you want to delete ${model.name}?`;
    }
    closeMenu() {
        document.getElementById("overlay").style.display = "none";
        document.getElementById("deletedModelMenu").style.display = "none";
    }
    deleteModel() {
        fetch(`/api/models/user-models/${this.modelId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => response.text())
            .then(data => {
            console.log(data);
            this.closeMenu();
            window.modelsView.renderUserModels();
        })
            .catch(error => console.error(error));
    }

    setListeners(){
        document.getElementById("overlay").addEventListener("click", () => {
            if (window.deleteModelMenu) {
                window.deleteModelMenu.closeMenu();
            }
        });
        document.getElementById("confirmDeleteButton").addEventListener("click", () => {
            if (window.deleteModelMenu) {
                window.deleteModelMenu.deleteModel();
            }
        });
        document.getElementById("cancelDeleteButton").addEventListener("click", () => {
            if (window.deleteModelMenu) {
                window.deleteModelMenu.closeMenu();
            }
        });
    }
}

window.deleteModelMenu = new DeleteModelMenu();