class FileItem{
    constructor(fileId, projectId){
        this.editMenu = window.editFileView;
        this.fileId = fileId;
        this.projectId = projectId;
    }

    createHtmlItem(file, destination){
        const fileElement = document.createElement('div');
         fileElement.id = `file-item-${Math.random().toString(36).substr(2, 9)}`;
                fileElement.className = 'file-item';
                fileElement.innerHTML = `
                    <span class="icon-file">📄</span>
                    <span class="file-name">${file.fileName}</span>
                `;

                fileElement.dataset.fileId = file.id;
                fileElement.dataset.projectId = this.projectId;

                fileElement.addEventListener('click', () => {
                    this.showFileContent(fileElement.dataset.projectId, fileElement.dataset.fileId);
                });

                 fileElement.addEventListener('contextmenu', (event) => {
                            event.preventDefault();
                            this.selectFile(this.projectId, file.id, fileElement.id);
                   //this.selectFile(fileElement.dataset.projectId, fileElement.dataset.fileId, fileElement.id);
                            this.editMenu.show(event);
                        });

                destination.appendChild(fileElement);
    }

    selectFile(projectId, fileId, fileItemId){
        this.editMenu.selectedFile = fileId;
        this.editMenu.selectedProject = projectId;
        this.editMenu.selectedFileItemId = fileItemId;
    }

    async showFileContent(projectId, fileId) {
            try {
              const response = await fetchWithCsrf(`/api/projects/${projectId}/files/${fileId}`, {
                credentials: 'include'
              });

              if (!response.ok) throw new Error('Nie udało się pobrać pliku');

              const blob = await response.blob();
              const url = URL.createObjectURL(blob);
              window.open(url, '_blank');
            } catch (error) {
                //this.showError(t.t("openFileFailed"));
                console.error('Błąd:', error);
            }
        }
}