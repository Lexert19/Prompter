class EditFileView {
    static _instance = null;

    static instance() {
        if (!EditFileView._instance) {
            EditFileView._instance = new EditFileView();
        }
        return EditFileView._instance;
    }

    constructor() {
        if (EditFileView._instance) {
            return EditFileView._instance;
        }
        EditFileView._instance = this;

        this.editFileHtml = document.getElementById("edit-file-menu");
        this.deleteButton = document.getElementById("delete-file-button");

        document.addEventListener("click", (event) => {
            if (!this.editFileHtml.contains(event.target)) {
                this.hide();
            }
        });

        this.selectedFile = "";
        this.selectedProject = "";
        this.selectedFileItemId = null;

        this.addDeleteButtonListener();
    }

    show(event) {
        this.editFileHtml.style.position = "fixed";
        this.editFileHtml.style.left = `${event.clientX}px`;
        this.editFileHtml.style.top = `${event.clientY}px`;
        this.editFileHtml.style.display = "block";
    }

    hide() {
        this.editFileHtml.style.display = "none";
    }

    addDeleteButtonListener() {
        this.deleteButton.addEventListener("click", () => {
                fetchWithAuth(`/api/projects/${this.selectedProject}/files/${this.selectedFile}`, {
                    method: "DELETE",
                })
                    .then((response) => {
                        if (response.ok) {
                            const fileItem = document.getElementById(this.selectedFileItemId);
                            fileItem.remove();
                        } else {
                            console.error("Błąd podczas usuwania pliku");
                        }
                    })
                    .catch((error) => {
                        console.error("Błąd podczas usuwania pliku:", error);
                    });
                this.hide();
        });
    }
}
EditFileView.instance();
