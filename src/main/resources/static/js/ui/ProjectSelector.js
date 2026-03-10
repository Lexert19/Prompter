class ProjectSelector {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        if (!this.container) return;
        this.projects = [];
        this.render();
        this.attachEvents();
        this.loadProjects();
    }

    render() {
        const currentProjectId = window.settings.project || '';
        const enabled = window.settings.projectSwitch || false;
        const currentProject = this.projects.find(p => p.id == currentProjectId);
        const projectName = currentProject ? currentProject.name : t.t('noProjectSelected');
        const status = enabled ? t.t('enabled') : t.t('disabled');

        this.container.innerHTML = `
            <div class="instruction-field">
                <label>${t.t('settings.project')}</label>
                <button class="project-selector-button rounded-1 d-flex align-items-center justify-content-between w-100" id="projectSelectorBtn">
                    <span class="project-selector-preview">${projectName}</span>
                    <span class="project-selector-status">${status}</span>
                </button>
            </div>
        `;
    }

    attachEvents() {
        const btn = document.getElementById('projectSelectorBtn');
        if (btn) {
            btn.addEventListener('click', () => this.openModal());
        }
    }

    async loadProjects() {
        try {
            const response = await fetch('/api/projects', {
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Failed to load projects');
            this.projects = await response.json();
            this.render();
            this.attachEvents();
        } catch (error) {
            console.error('Error loading projects:', error);
        }
    }

    openModal() {
        const currentProjectId = window.settings.project || '';
        const enabled = window.settings.projectSwitch || false;

        let listHtml = '<div class="project-list">';
        this.projects.forEach(project => {
            const selectedClass = (project.id == currentProjectId) ? 'selected' : '';
            listHtml += `
                <div class="project-item-selectable ${selectedClass}" data-id="${project.id}">
                    <span class="project-item-name">${project.name}</span>
                </div>
            `;
        });
        listHtml += '</div>';

        const modalContent = `
            <div class="d-flex flex-column">
                ${listHtml}
                <label class="checkbox-container mt-2">
                    <input type="checkbox" id="projectEnable" ${enabled ? 'checked' : ''}>
                    <span class="custom-checkbox"></span>
                    <span>${t.t('enableProject')}</span>
                </label>
            </div>
        `;

        window.modal.open(
            t.t('selectProject'),
            modalContent,
            null
        );

        document.querySelectorAll('.project-item-selectable').forEach(el => {
            el.addEventListener('click', (e) => {
                const projectId = el.dataset.id;
                const project = this.projects.find(p => p.id == projectId);
                if (project) {
                    window.settings.project = project.id;
                    window.settings.save();
                }
                window.modal.close();
                this.render();
                this.attachEvents();
            });
        });

        const enableCheckbox = document.getElementById('projectEnable');
        if (enableCheckbox) {
            enableCheckbox.addEventListener('change', (e) => {
                window.settings.projectSwitch = e.target.checked;
                window.settings.save();
            });
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('projectSelectorContainer')) {
        window.projectSelector = new ProjectSelector('projectSelectorContainer');
    }
});