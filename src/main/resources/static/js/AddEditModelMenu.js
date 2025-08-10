class AddEditModelMenu{
    loadProviders(){
        const providerSelect = document.getElementById("modelProvider");
        this.insertProviders(providerSelect);
    }

    insertProviders(providerSelect){
        const keys = Object.keys(window.settings.keys);

        providerSelect.innerHTML = '';

        const defaultOption = document.createElement('option');
        defaultOption.value = "";
        defaultOption.textContent = "Wybierz dostawcę";
        defaultOption.disabled = true;
        defaultOption.selected = true;
        providerSelect.appendChild(defaultOption);

        keys.forEach(key => {
            const option = document.createElement('option');
            option.value = key;
            option.textContent = key;
            providerSelect.appendChild(option);
        });
    }

    addModel() {
        const modelName = document.getElementById('modelName').value;
        const modelText = document.getElementById('modelText').value;
        const modelProvider = document.getElementById('modelProvider').value;
        const modelUrl = document.getElementById('modelUrl').value;
        const modelType = document.getElementById('modelType').value;
        const modelDto = {
            name: modelName,
            text: modelText,
            provider: modelProvider,
            url: modelUrl,
            type: modelType
        };
        fetch('/api/models/user-models', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(modelDto)
        })
            .then(response => response.text())
            .then(data => {
            window.modelsView.renderUserModels();
            this.closeMenu();
        })
            .catch(error => console.error(error));
    }

    closeMenu() {
        document.getElementById("overlay").style.display = "none";
        document.getElementById("addModelMenu").style.display = "none";
        document.getElementById("editModelMenu").style.display = "none";
    }

    showAddMenu(){
        document.getElementById("overlay").style.display = "block";
        document.getElementById("addModelMenu").style.display = "block";
        document.getElementById("editModelMenu").style.display = "none";
    }

    showEditMenu(model){
        document.getElementById("overlay").style.display = "block";
        document.getElementById("addModelMenu").style.display = "none";
        document.getElementById("editModelMenu").style.display = "block";

        document.getElementById("editModelMenu").querySelector('#editModelName').value = model.name;
        document.getElementById("editModelMenu").querySelector('#editModelText').value = model.text;
        const providerSelect = document.getElementById("editModelMenu").querySelector('#editModelProvider');
        this.insertProviders(providerSelect);
        providerSelect.value = model.provider;

        document.getElementById("editModelMenu").querySelector('#editModelUrl').value = model.url;
        document.getElementById("editModelMenu").querySelector('#editModelType').value = model.type;
        this.modelId = model.id;
    }


    editModel(){
        const modelName = document.getElementById('editModelName').value;
        const modelText = document.getElementById('editModelText').value;
        const modelProvider = document.getElementById('editModelProvider').value;
        const modelUrl = document.getElementById('editModelUrl').value;
        const modelType = document.getElementById('editModelType').value;
        const modelDto = {
            name: modelName,
            text: modelText,
            provider: modelProvider,
            url: modelUrl,
            type: modelType
        };
        fetch(`/api/models/user-models/${this.modelId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(modelDto)
        })
            .then(response => response.text())
            .then(data => {
            window.modelsView.renderUserModels();
            this.closeMenu();
        })
            .catch(error => console.error(error));
    }

}

document.getElementById("overlay").addEventListener("click", () => {
    window.addEditModelMenu.closeMenu();
});

window.addEditModelMenu = new AddEditModelMenu();

