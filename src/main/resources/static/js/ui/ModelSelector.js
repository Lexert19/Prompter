class ModelSelector {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        if (!this.container) return;
        this.render();
        this.attachEvents();
    }

    render() {
        const currentModel = window.settings.model;
        const models = window.settings.models || [];
        const currentModelObj = models.find(m => m.name === currentModel) || { text: currentModel || '...' };
        const displayText = currentModelObj.text || currentModel || 'Wybierz model';

        this.container.innerHTML = `
            <div class="instruction-field">
                <label>${t.t('settings.model')}</label>
                <button class="model-selector-button rounded-1 d-flex align-items-center justify-content-between w-100" id="modelSelectorBtn">
                    <span>${displayText}</span>
                    <i class="fas fa-chevron-down"></i>
                </button>
            </div>
        `;
    }

    attachEvents() {
        const btn = document.getElementById('modelSelectorBtn');
        if (btn) {
            btn.addEventListener('click', () => this.openModal());
        }
    }

    openModal() {
        const models = window.settings.models || [];
        let listHtml = '<div class="model-list">';
        models.forEach(model => {
            const selectedClass = (model.name === window.settings.model) ? 'selected' : '';
            listHtml += `
                <div class="model-item-selectable ${selectedClass}"
                     data-name="${model.name}"
                     data-provider="${model.provider}"
                     data-url="${model.url || ''}"
                     data-type="${model.type || 'text'}">
                    <span class="model-item-name">${model.text || model.name}</span>
                    <span class="model-item-provider">${model.provider}</span>
                </div>
            `;
        });
        listHtml += '</div>';

        window.modal.open(
            t.t('selectModel'),
            listHtml,
            null
        );

        document.querySelectorAll('.model-item-selectable').forEach(el => {
            el.addEventListener('click', (e) => {
                const name = el.dataset.name;
                const provider = el.dataset.provider;
                const url = el.dataset.url;
                const type = el.dataset.type;

                window.settings.model = name;
                window.settings.provider = provider;
                window.settings.url = url;
                window.settings.type = type;
                window.settings.key = window.settings.keys ? window.settings.keys[provider] : '';
                window.settings.save();

                window.modal.close();

                this.render();
                this.attachEvents();
            });
        });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('modelSelectorContainer')) {
        window.modelSelector = new ModelSelector('modelSelectorContainer');
    }
});