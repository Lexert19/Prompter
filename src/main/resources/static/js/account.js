

function toggleUserPanel() {
    var panel = document.getElementById("accountPanel");

    panel.classList.toggle('active');
}

function saveKey() {
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
            alert(data);
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
            alert('Error saving key: ' + error.message);
        });
}

function setNewPassword() {
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
        document.getElementById('newPassword').value = '';
        document.getElementById('confirmPassword').value = '';
    })
    .catch(error => {
        console.error("Błąd:", error);
        alert(`Błąd zmiany hasła: ${error.message}`);
    });
}