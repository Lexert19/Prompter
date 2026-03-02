class AccountView{
    constructor(){

    }

    openAddKeyModal() {
        const content = `
            <form id="keyForm" class="d-flex flex-column">
                <label for="keyName">Nazwa klucza</label>
                <input type="text" id="keyName" name="keyName" class="mb-05" required>
                <label for="keyValue">Wartość klucza</label>
                <input type="text" id="keyValue" name="keyValue" class="mb-05" required>
                <button class="rounded-1 mt-2 ms-auto" type="submit">Zapisz</button>
            </form>
        `;
        window.modal.open('Dodaj klucz API', content, (formData) => {
            this.saveKey(formData.get('keyName'), formData.get('keyValue'));
        });
    }

    openChangePasswordModal() {
        const content = `
            <form  class="d-flex flex-column" id="passwordForm">
                <label for="newPassword">Nowe hasło</label>
                <input type="password" id="newPassword" name="newPassword" class="mb-05" required>
                <label for="confirmPassword">Powtórz hasło</label>
                <input type="password" id="confirmPassword" name="confirmPassword" class="mb-05" required>
                <button class="rounded-1 mt-2 ms-auto" type="submit">Zmień hasło</button>
            </form>
        `;
        window.modal.open('Zmień hasło', content, (formData) => {
            this.setNewPassword(formData.get('newPassword'), formData.get('confirmPassword'));
        });
    }

    saveKey(){
        const keyName = document.getElementById("keyName");
        const keyValue = document.getElementById("keyValue");

        const url = "/api/account/save-key/"+keyName.value;

        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: keyValue.value,
        })
            .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
            .then(data => {
            console.log(data);
            window.settings.loadKeys();
            window.modal.close();
            alert(data);
        })
            .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
            alert('Error saving key: ' + error.message);
        });
    }


    setNewPassword(){
        const newPass = document.getElementById('newPassword').value;
        const confirmPass = document.getElementById('confirmPassword').value;

        if (!newPass || !confirmPass) {
            alert("Proszę wypełnić oba pola.");
            return;
        }

        if (newPass !== confirmPass) {
            alert("Hasła nie są identyczne!");
            return;
        }

        fetch('/api/account/change-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                newPassword: newPass,
                confirmPassword: confirmPass

            }),
            credentials: 'include'
        })
            .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text) });
            }
            return response.text();
        })
            .then(data => {
            alert("Hasło zostało zmienione!");
            window.modal.close();
            document.getElementById('newPassword').value = '';
            document.getElementById('confirmPassword').value = '';
        })
            .catch(error => {
            console.error("Błąd:", error);
            alert(`Błąd zmiany hasła: ${error.message}`);
        });
    }
}

window.accountView = new AccountView();
