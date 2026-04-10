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

        let modalHtml = `
            <div class="model-selector-modal-wrapper">
                <div class="model-search-container mb-3">
                    <input type="text" id="modelSearchInput" class="model-search-input"
                           placeholder="${t.t('searchModels') || 'Szukaj modelu lub providera...'}">
                </div>
                <div style="min-height: 400px;">
                    <div class="model-list" id="modelListContainer">
        `;

        models.forEach(model => {
            const selectedClass = (model.name === window.settings.model) ? 'selected' : '';
            modalHtml += `
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

        modalHtml += '</div></div></div>';

        window.modal.open(
            t.t('selectModel'),
            modalHtml,
            null
        );

        const modalElement = document.querySelector('.modal-menu');
        if (modalElement) {
            modalElement.classList.add('modal-wide');
        }


        const searchInput = document.getElementById('modelSearchInput');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                const term = e.target.value.toLowerCase();
                const items = document.querySelectorAll('.model-item-selectable');

                items.forEach(item => {
                    const name = item.dataset.name.toLowerCase();
                    const provider = item.dataset.provider.toLowerCase();
                    const text = item.querySelector('.model-item-name').textContent.toLowerCase();

                    if (name.includes(term) || provider.includes(term) || text.includes(term)) {
                        item.style.display = 'flex';
                    } else {
                        item.style.display = 'none';
                    }
                });
            });
            setTimeout(() => searchInput.focus(), 100);
        }

        document.querySelectorAll('.model-item-selectable').forEach(el => {
            el.addEventListener('click', (e) => {
                const { name, provider, url, type } = el.dataset;
                window.settings.model = name;
                window.settings.provider = provider;
                window.settings.url = url;
                window.settings.type = type;
                window.settings.key = window.settings.keys ? window.settings.keys[provider] : '';
                window.settings.save();
                const modalElement = document.querySelector('.modal-menu');
                if (modalElement) modalElement.classList.remove('modal-wide');
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