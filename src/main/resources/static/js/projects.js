class Projects {
    constructor() {
        this.controllerUrl = '/api/projects';
        this.projectSelect = document.getElementById('project-select');
        this.createButton = document.getElementById('btn-create');
        this.projectDetails = document.querySelector('.project-details');
        this.projectNameDisplay = document.querySelector('.project-name-display');
        this.fileGrid = document.querySelector('.file-grid');
        this.addFileButton = document.getElementById('btn-add-file');
        this.errorMessages = document.querySelector('.error-messages');
        this.currentProjectId = null;

        this.initialize();
    }

    initialize() {
        this.loadProjects();
        this.setupEventListeners();
    }

    setupEventListeners() {
        this.createButton.addEventListener('click', () => this.createProject());
        this.projectSelect.addEventListener('change', () => this.loadProjectDetails());
        this.addFileButton.addEventListener('click', () => this.handleFileUpload());
    }

    populateProjectSelect(projects) {
        this.projectSelect.innerHTML = '<option value="">Wybierz projekt</option>';
        projects.forEach(project => {
            const option = document.createElement('option');
            option.value = project.id;
            option.textContent = project.name;
            this.projectSelect.appendChild(option);
        });
    }

    async getContext(query){
        if (window.settings.projectSwitch && window.settings.project) {
            const fragments = await this.searchSimilarFragments(window.settings.project, encodeURIComponent(query));
            return fragments;
        }
        return [];
    }

    async createProject() {
        const projectName = prompt('Podaj nazwę nowego projektu:');
        if (!projectName) return;

        try {
            const response = await fetch(`${this.controllerUrl}/create`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: projectName
            });

            if (!response.ok) throw new Error('Failed to create project');
            
            await response.json();
            this.loadProjects();
        } catch (error) {
            this.showError('Nie udało się utworzyć projektu');
            console.error('Error:', error);
        }
    }

    async loadProjectDetails() {
        const projectId = this.projectSelect.value;
        if (!projectId) return;

        try {
            const response = await fetch(`${this.controllerUrl}/${projectId}`, {
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Project not found');
            
            const project = await response.json();
            this.displayProjectDetails(project);
        } catch (error) {
            this.showError('Projekt nie istnieje');
            console.error('Error:', error);
        }
    }

    displayProjectDetails(project) {
        this.projectNameDisplay.textContent = project.name;
        this.currentProjectId = project.id; 
        this.loadProjectFiles(project.id);
    }

    async loadProjects() {
        try {
            const response = await fetch(`${this.controllerUrl}`, {
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Failed to fetch projects');
            
            const projects = await response.json();
            this.populateProjectSelect(projects);
        } catch (error) {
            this.showError('Brak dostępu do projektu');
            console.error('Error:', error);
        }
    }

    displayFiles(files) {
        this.fileGrid.innerHTML = '';
        files.forEach(file => {
              const fileItem = new FileItem(file.id, this.currentProjectId);
              fileItem.createHtmlItem(file, this.fileGrid);

        });

    }

    handleFileUpload() {
        const fileInput = document.createElement('input');
        fileInput.type = 'file';
        fileInput.onchange = (e) => {
            const file = e.target.files[0];
            if (!file) return;

            const reader = new FileReader();
            reader.onload = (e) => {
                const fileContent = e.target.result;
                this.addFileToProject(file.name, fileContent);
            };
            reader.readAsText(file);
        };
        fileInput.click();
    }

    async addFileToProject(fileName, fileContent) {
        const projectId = this.projectSelect.value;
        if (!projectId) return;

        try {
            const response = await fetch(`${this.controllerUrl}/${projectId}/files`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify({
                    name: fileName,
                    content: fileContent
                })
            });

            if (!response.ok) throw new Error('Failed to add file');
            
            await response.json();
            this.loadProjectFiles(projectId);
        } catch (error) {
            this.showError('Nie udało się dodać pliku');
            console.error('Error:', error);
        }
    }

    async searchSimilarFragments(projectId, query) {
        try {
            const response = await fetch(
                `${this.controllerUrl}/${projectId}/similar-fragments?query=${encodeURIComponent(query)}`,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    credentials: 'include'
                }
            );
    
            if (!response.ok) throw new Error('Błąd podczas wyszukiwania');
    
            const fragments = await response.json();
            return fragments;
        } catch (error) {
            this.showError('Nie udało się znaleźć podobnych fragmentów');
            console.error('Błąd:', error);
        }
    }

    async loadProjectFiles(projectId) {
        try {
            const response = await fetch(`${this.controllerUrl}/${projectId}/files`, {
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Failed to fetch files');
            
            const files = await response.json();
            this.displayFiles(files);
        } catch (error) {
            this.showError('Nie udało się wczytać plików');
            console.error('Error:', error);
        }
    }

    showError(message) {
        this.errorMessages.style.display = 'block';
        this.errorMessages.textContent = message;
        setTimeout(() => {
            this.errorMessages.style.display = 'none';
        }, 3000);
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

window.projects = new Projects();   