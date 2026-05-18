class ModelSelector {
    static _instance = null;

    static instance(containerId = 'modelSelectorContainer') {
        if (!ModelSelector._instance) {
            ModelSelector._instance = new ModelSelector(containerId);
        }
        return ModelSelector._instance;
    }

    constructor(containerId) {
        if (ModelSelector._instance) {
            return ModelSelector._instance;
        }
        ModelSelector._instance = this;

        this.container = document.getElementById(containerId);
        if (!this.container) return;

        this.render();
        this.attachEvents();
    }

    render() {
        const currentModel = Settings.instance().model;
        const models = Settings.instance().models || [];
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

    async syncModels() {
        try {
            const response = await fetchWithCsrf('/api/models/all-models', { credentials: 'include' });
            const models = await response.json();

            const newModels = models.map(model => ({
                name: model.name,
                text: model.text || model.name,
                provider: model.provider,
                url: model.url,
                type: model.type
            }));

            const uniqueModels = [...new Map(newModels.map(m => [m.name, m])).values()];
            uniqueModels.sort((a, b) => a.text.localeCompare(b.text));

            Settings.instance().models = uniqueModels;

            this.render();
            this.attachEvents();
        } catch (error) {
            console.error('syncModels error:', error);
        }
    }

    openModal() {
        const models = Settings.instance().models || [];

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
            const selectedClass = (model.name === Settings.instance().model) ? 'selected' : '';
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

        Modal.instance().open(
            t.t('selectModel'),
            modalHtml,
            null,
            ['modal-wide']
        );


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
                Settings.instance().model = name;
                Settings.instance().provider = provider;
                Settings.instance().url = url;
                Settings.instance().type = type;
                Settings.instance().key = (Settings.instance().keys && provider) ? (Settings.instance().keys[provider.toUpperCase()] || '') : '';
                Settings.instance().save();
                Modal.instance().close();
                this.render();
                this.attachEvents();
            });
        });
    }
}

document.addEventListener('i18n:ready', () => {
    ModelSelector.instance('modelSelectorContainer');
});