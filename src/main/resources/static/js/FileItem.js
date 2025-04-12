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
                    <span class="file-name">${file.name}</span>
                `;

                fileElement.dataset.fileId = file.id;
                fileElement.dataset.projectId = this.projectId;

                fileElement.addEventListener('click', () => {
                    this.showFileContent(fileElement.dataset.projectId, fileElement.dataset.fileId);
                });

                 fileElement.addEventListener('contextmenu', (event) => {
                            event.preventDefault();
                            this.selectFile(fileElement.dataset.projectId, fileElement.dataset.fileId, fileElement.id);
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
                const response = await fetch(`/api/projects/${projectId}/files/${fileId}`, {
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    credentials: 'include'
                });

                if (!response.ok) {
                    throw new Error('Nie udało się pobrać pliku');
                }

                const content = await response.text();
                const blob = new Blob([content], { type: 'text/plain' });
                const url = URL.createObjectURL(blob);
                window.open(url, '_blank');
            } catch (error) {
                this.showError('Nie udało się otworzyć pliku');
                console.error('Błąd:', error);
            }
        }
}