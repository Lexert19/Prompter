class ModelsView{
    constructor(){
        this.renderUserModels();
    }


    renderUserModels(){
        fetch('/api/models/user-models', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        })
            .then(response => response.json())
            .then(models => {
            this.models = models.map(model => ({
                id: model.id,
                name: model.name,
                text: model.text || model.name,
                provider: model.provider,
                url: model.url,
                type: model.type
            }));
            this.renderModels(models);
        })
    }

    renderModels(models) {
        const modelsDiv = document.getElementById('models');
        modelsDiv.innerHTML = '';
        models.forEach(model => {
            const modelDiv = document.createElement('div');
            modelDiv.style.display = 'flex';
            modelDiv.style.alignItems = 'center';
            const modelNameSpan = document.createElement('span');
            modelNameSpan.textContent = model.text || model.name;
            modelNameSpan.style.flexGrow = '1';
            const editButton = document.createElement('button');
            editButton.textContent = 'Edytuj';
            editButton.onclick = () => window.addEditModelMenu.showEditMenu(model);
            const deleteButton = document.createElement('button');
            deleteButton.textContent = 'Usuń';
            deleteButton.onclick = () => window.deleteModelMenu.showDeleteMenu(model);
            modelDiv.appendChild(modelNameSpan);
            modelDiv.appendChild(editButton);
            modelDiv.appendChild(deleteButton);
            modelsDiv.appendChild(modelDiv);
        });
        const addButton = document.createElement('button');
        addButton.textContent = 'Dodaj model';
        addButton.onclick = () => window.addEditModelMenu.showAddMenu();
        modelsDiv.appendChild(addButton);
    }
}

window.modelsView = new ModelsView();