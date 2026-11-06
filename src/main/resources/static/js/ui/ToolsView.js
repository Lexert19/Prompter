class ToolsView {
    static _instance = null;

    static instance() {
        if (!ToolsView._instance) {
            ToolsView._instance = new ToolsView();
        }
        return ToolsView._instance;
    }

    constructor() {
        if (ToolsView._instance) {
            return ToolsView._instance;
        }
        ToolsView._instance = this;
        this.availableTools = [];
        this.loadTools();
    }

    async loadTools() {
        try {
            const response = await fetchWithAuth('/api/tools', { credentials: 'include' });
            if (response.ok) {
                this.availableTools = await response.json();
            }
        } catch (error) {
            console.error('Failed to load tools:', error);
        }
    }

    openModal() {
        const settings = Settings.instance();
        const useTools = settings.useTools;
        const enabledTools = settings.enabledTools || [];

        let checkboxesHtml = '';
        if (this.availableTools.length === 0) {
            checkboxesHtml = `<p class="text-secondary">${t.t('tools.noAvailable')}</p>`;
        } else {
            this.availableTools.forEach(tool => {
                const isChecked = enabledTools.includes(tool.name);
                checkboxesHtml += `
                    <label class="checkbox-container mb-2 d-flex align-items-center">
                        <input type="checkbox" class="tool-checkbox" value="${tool.name}" ${isChecked ? 'checked' : ''} ${!useTools ? 'disabled' : ''}>
                        <span class="custom-checkbox"></span>
                        <div class="ms-2">
                            <strong>${tool.name}</strong><br>
                            <small class="text-secondary">${tool.description}</small>
                        </div>
                    </label>
                `;
            });
        }

        const modalContent = `
            <form id="toolsForm" class="d-flex flex-column">
                <label class="checkbox-container mb-3" style="font-weight: bold;">
                    <input type="checkbox" id="masterUseTools" ${useTools ? 'checked' : ''}>
                    <span class="custom-checkbox"></span>
                    <span>${t.t('tools.enableFunctionCalling')}</span>
                </label>
                <hr>
                <div id="toolsListContainer" style="opacity: ${useTools ? '1' : '0.5'}; pointer-events: ${useTools ? 'auto' : 'none'};">
                    ${checkboxesHtml}
                </div>
                <button type="submit" class="ms-auto rounded-1 mt-3">${t.t('save')}</button>
            </form>
        `;

        Modal.instance().open(t.t('tools.settingsTitle'), modalContent, (formData) => {
            const useToolsChecked = document.getElementById('masterUseTools').checked;
            const selectedTools = [];
            document.querySelectorAll('.tool-checkbox:checked').forEach(cb => {
                selectedTools.push(cb.value);
            });

            Settings.instance().useTools = useToolsChecked;
            Settings.instance().enabledTools = selectedTools;
            Settings.instance().save();

            Modal.instance().close();
        });

        document.getElementById('masterUseTools').addEventListener('change', (e) => {
            const container = document.getElementById('toolsListContainer');
            const checkboxes = document.querySelectorAll('.tool-checkbox');
            if (e.target.checked) {
                container.style.opacity = '1';
                container.style.pointerEvents = 'auto';
                checkboxes.forEach(cb => cb.disabled = false);
            } else {
                container.style.opacity = '0.5';
                container.style.pointerEvents = 'none';
                checkboxes.forEach(cb => cb.disabled = true);
            }
        });
    }
}

document.addEventListener('i18n:ready', () => {
    ToolsView.instance();
});