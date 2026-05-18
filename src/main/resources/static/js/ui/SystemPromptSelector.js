class SystemPromptSelector {
    static _instance = null;

    static instance(containerId = 'systemPromptContainer') {
        if (!SystemPromptSelector._instance) {
            SystemPromptSelector._instance = new SystemPromptSelector(containerId);
        }
        return SystemPromptSelector._instance;
    }

    constructor(containerId) {
        if (SystemPromptSelector._instance) {
            return SystemPromptSelector._instance;
        }
        SystemPromptSelector._instance = this;

        this.container = document.getElementById(containerId);
        if (!this.container) return;

        this.render();
        this.attachEvents();
    }

    render() {
        const system = Settings.instance().system || '';
        const enabled = Settings.instance().systemSwitch || false;
        const preview = system.length > 24 ? system.substring(0, 24) + '…' : (system || t.t('systemPromptEmpty'));
        const status = enabled ? t.t('enabled') : t.t('disabled');

        this.container.innerHTML = `
            <div class="instruction-field">
                <label>${t.t('settings.system')}</label>
                <button class="system-prompt-button rounded-1 d-flex align-items-center justify-content-between w-100" id="systemPromptBtn">
                    <span class="system-prompt-preview">${preview}</span>
                    <span class="system-prompt-status">${status}</span>
                </button>
            </div>
        `;
    }

    attachEvents() {
        const btn = document.getElementById('systemPromptBtn');
        if (btn) {
            btn.addEventListener('click', () => this.openModal());
        }
    }

    openModal() {
        const system = Settings.instance().system || '';
        const enabled = Settings.instance().systemSwitch || false;

        const modalContent = `
            <form id="systemPromptForm" class="d-flex flex-column">
                <label for="systemPromptText">${t.t('systemPrompt')}</label>
                <textarea id="systemPromptText" name="systemPrompt" class="mb-2" rows="5">${system}</textarea>
                <label class="checkbox-container mb-2">
                    <input type="checkbox" id="systemPromptEnable" ${enabled ? 'checked' : ''}>
                    <span class="custom-checkbox"></span>
                    <span>${t.t('enableSystemPrompt')}</span>
                </label>
                <button type="submit" class="ms-auto rounded-1">${t.t('save')}</button>
            </form>
        `;

        Modal.instance().open(
            t.t('editSystemPrompt'),
            modalContent,
            (formData) => {
                const newText = document.getElementById('systemPromptText').value;
                const newEnabled = document.getElementById('systemPromptEnable').checked;

                Settings.instance().system = newText;
                Settings.instance().systemSwitch = newEnabled;
                Settings.instance().save();

                this.render();
                this.attachEvents();
                Modal.instance().close();
            },
            ["modal-wide"]
        );
    }
}

document.addEventListener('i18n:ready', () => {
    SystemPromptSelector.instance('systemPromptContainer');
});