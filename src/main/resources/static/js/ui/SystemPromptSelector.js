class SystemPromptSelector {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        if (!this.container) return;
        this.render();
        this.attachEvents();
    }

    render() {
        const system = window.settings.system || '';
        const enabled = window.settings.systemSwitch || false;
        const preview = system.length > 30 ? system.substring(0, 30) + '…' : (system || t.t('systemPromptEmpty'));
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
        const system = window.settings.system || '';
        const enabled = window.settings.systemSwitch || false;

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

        window.modal.open(
            t.t('editSystemPrompt'),
            modalContent,
            (formData) => {
                const newText = document.getElementById('systemPromptText').value;
                const newEnabled = document.getElementById('systemPromptEnable').checked;

                window.settings.system = newText;
                window.settings.systemSwitch = newEnabled;
                window.settings.save();

                this.render();
                this.attachEvents();
                window.modal.close();
            }
        );

        const form = document.getElementById('systemPromptForm');
        if (form) {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                const newText = document.getElementById('systemPromptText').value;
                const newEnabled = document.getElementById('systemPromptEnable').checked;

                window.settings.system = newText;
                window.settings.systemSwitch = newEnabled;
                window.settings.save();

                this.render();
                this.attachEvents();
                window.modal.close();
            });
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('systemPromptContainer')) {
        window.systemPromptSelector = new SystemPromptSelector('systemPromptContainer');
    }
});