class AddEditModelMenu {
    showAddMenu() {
        const html = `
            <form class="d-flex flex-column" id="modelForm">
                <label>Nazwa modelu</label>
                <input name="name" id="modelName" class="mb-05" required>
                <label>Tekst wyświetlany</label>
                <input name="text" id="modelText" class="mb-05">
                <label>Dostawca</label>
                <select name="provider" id="modelProvider" class="mb-05" required>
                    ${this.providerOptions()}
                </select>
                <label>URL</label>
                <input name="url" id="modelUrl" class="mb-05" required>
                <label>Typ</label>
                <select name="type" id="modelType" class="mb-05" required>
                    <option value="chat">chat</option>
                    <option value="completion">completion</option>
                </select>
                <button class="ms-auto rounded-1" type="submit">Dodaj</button>
            </form>
        `;
        window.modal.open('Dodaj model', html, (formData) => this.addModel(formData));
    }

    showEditMenu(model) {
        const html = `
            <form class="d-flex flex-column" id="modelForm">
                <input type="hidden" id="modelId" value="${model.id}">
                <label>Nazwa modelu</label>
                <input name="name" id="modelName" value="${model.name}" class="mb-05" required>
                <label>Tekst wyświetlany</label>
                <input name="text" id="modelText" value="${model.text || ''}" class="mb-05">
                <label>Dostawca</label>
                <select name="provider" id="modelProvider" class="mb-05" required>
                    ${this.providerOptions(model.provider)}
                </select>
                <label>URL</label>
                <input name="url" id="modelUrl" value="${model.url || ''}" class="mb-05" required>
                <label>Typ</label>
                <select name="type" id="modelType" class="mb-05" required>
                    <option value="chat" ${model.type === 'chat' ? 'selected' : ''}>chat</option>
                    <option value="completion" ${model.type === 'completion' ? 'selected' : ''}>completion</option>
                </select>
                <button class="ms-auto rounded-1" type="submit">Zapisz</button>
            </form>
        `;
        window.modal.open('Edytuj model', html, (formData) => this.editModel(model.id, formData));
    }

    providerOptions(selected = '') {
        const keys = Object.keys(window.settings.keys || {});
        let options = '<option value="" disabled>Wybierz dostawcę</option>';
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
        fetch('/api/models/user-models', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        })
            .then(res => res.text())
            .then(() => {
            window.modelsView.renderUserModels();
            window.modal.close();
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
        fetch(`/api/models/user-models/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        })
            .then(res => res.text())
            .then(() => {
            window.modelsView.renderUserModels();
            window.modal.close();
        })
            .catch(console.error);
    }
}
window.addEditModelMenu = new AddEditModelMenu();