class AddEditModelMenu {
    static _instance = null;

    static instance() {
        if (!AddEditModelMenu._instance) {
            AddEditModelMenu._instance = new AddEditModelMenu();
        }
        return AddEditModelMenu._instance;
    }

    constructor() {
        if (AddEditModelMenu._instance) {
            return AddEditModelMenu._instance;
        }
        AddEditModelMenu._instance = this;
    }

    showAddMenu() {
        const html = `
            <form class="d-flex flex-column" id="modelForm">
                <label>${t.t("modelName")}</label>
                <input name="name" id="modelName" class="mb-05" required>
                <label>${t.t("displayText")}</label>
                <input name="text" id="modelText" class="mb-05">
                <label>${t.t("provider")}</label>
                <select name="provider" id="modelProvider" class="mb-05" required>
                    ${this.providerOptions()}
                </select>
                <label>${t.t("url")}</label>
                <input name="url" id="modelUrl" class="mb-05" required>
                <label>${t.t("type")}</label>
                <select name="type" id="modelType" class="mb-05" required>
                    <option value="text">text</option>
                    <option value="vision">vision</option>
                </select>
                <button class="ms-auto rounded-1" type="submit">${t.t("addModel")}</button>
            </form>
        `;
        Modal.instance().open(t.t("addModel"), html, (formData) => this.addModel(formData));
    }

    showEditMenu(model) {
        const html = `
            <form class="d-flex flex-column" id="modelForm">
                <input type="hidden" id="modelId" value="${model.id}">
                <label>${t.t("modelName")}</label>
                <input name="name" id="modelName" value="${model.name}" class="mb-05" required>
                <label>${t.t("displayText")}</label>
                <input name="text" id="modelText" value="${model.text || ''}" class="mb-05">
                <label>${t.t("provider")}</label>
                <select name="provider" id="modelProvider" class="mb-05" required>
                    ${this.providerOptions(model.provider)}
                </select>
                <label>${t.t("url")}</label>
                <input name="url" id="modelUrl" value="${model.url || ''}" class="mb-05" required>
                <label>Typ</label>
                <select name="type" id="modelType" class="mb-05" required>
                    <option value="text" ${model.type === 'text' ? 'selected' : ''}>text</option>
                    <option value="vision" ${model.type === 'vision' ? 'selected' : ''}>vision</option>
                </select>
                <button class="ms-auto rounded-1" type="submit">${t.t("save")}</button>
            </form>
        `;
        Modal.instance().open(t.t("editModel"), html, (formData) => this.editModel(model.id, formData));
    }

    providerOptions(selected = '') {
        const keys = Object.keys(Settings.instance().keys || {});
        let options = '<option value="" disabled>'+t.t("selectProvider")+'</option>';
        keys.forEach(key => {
            options += `<option value="${key}" ${key === selected ? 'selected' : ''}>${key}</option>`;
        });
        return options;
    }

    addModel(formData) {
        const dto = {
            name: formData.get('name'),
            text: formData.get('text'),
            provider: formData.get('provider'),
            url: formData.get('url'),
            type: formData.get('type')
        };
        fetchWithCsrf('/api/models/user-models', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        })
            .then(res => res.text())
            .then(() => {
            ModelsView.instance().renderUserModels();
            ModelSelector.instance().syncModels();
            Modal.instance().close();
        })
            .catch(console.error);
    }

    editModel(id, formData) {
        const dto = {
            name: formData.get('name'),
            text: formData.get('text'),
            provider: formData.get('provider'),
            url: formData.get('url'),
            type: formData.get('type')
        };
        fetchWithCsrf(`/api/models/user-models/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        })
            .then(res => res.text())
            .then(() => {
            ModelsView.instance().renderUserModels();
            ModelSelector.instance().syncModels();
            Modal.instance().close();
        })
            .catch(console.error);
    }
}

AddEditModelMenu.instance();
