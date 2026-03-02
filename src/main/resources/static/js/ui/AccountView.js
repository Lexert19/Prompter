class AccountView{
    constructor(){
        document.addEventListener('DOMContentLoaded', function() {
            const select = document.getElementById('languageSelect');
            if (select) {
                const lang = getCookie('lang') || 'pl';
                select.value = lang;
            }
        });
    }

    changeLanguage(lang) {
        document.cookie = `lang=${lang}; path=/; max-age=31536000`;
        location.reload();
    }

    openAddKeyModal() {
        const content = `
            <form id="keyForm" class="d-flex flex-column">
                <label for="keyName">${t.t("keyName")}</label>
                <input type="text" id="keyName" name="keyName" class="mb-05" required>
                <label for="keyValue">${t.t("keyValue")}</label>
                <input type="text" id="keyValue" name="keyValue" class="mb-05" required>
                <button class="rounded-1 mt-2 ms-auto" type="submit">${t.t("save")}</button>
            </form>
        `;
        window.modal.open(t.t("addApiKey"), content, (formData) => {
            this.saveKey(formData.get('keyName'), formData.get('keyValue'));
        });
    }

    openChangePasswordModal() {
        const content = `
            <form  class="d-flex flex-column" id="passwordForm">
                <label for="newPassword">${t.t("newPassword")}</label>
                <input type="password" id="newPassword" name="newPassword" class="mb-05" required>
                <label for="confirmPassword">${t.t("confirmPassword")}</label>
                <input type="password" id="confirmPassword" name="confirmPassword" class="mb-05" required>
                <button class="rounded-1 mt-2 ms-auto" type="submit">${t.t("changePassword")}</button>
            </form>
        `;
        window.modal.open(t.t("changePassword"), content, (formData) => {
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
            alert(t.t("errorSavingKey") + error.message);
        });
    }


    setNewPassword(){
        const newPass = document.getElementById('newPassword').value;
        const confirmPass = document.getElementById('confirmPassword').value;

        if (!newPass || !confirmPass) {
            alert(t.t("fillBothFields"));
            return;
        }

        if (newPass !== confirmPass) {
            alert(t.t("passwordsNotMatch"));
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
            alert(t.t("passwordChanged"));
            window.modal.close();
            document.getElementById('newPassword').value = '';
            document.getElementById('confirmPassword').value = '';
        })
            .catch(error => {
            console.error("Błąd:", error);
            alert(`${t.t("passwordChangeError")}${error.message}`);
        });
    }
}

window.accountView = new AccountView();
