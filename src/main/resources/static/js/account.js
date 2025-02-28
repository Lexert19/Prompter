

function toggleUserPanel() {
    var panel = document.getElementById("accountPanel");

    panel.classList.toggle('active');
}

function saveKey() {
    const keyName = document.getElementById("keyName");
    const keyValue = document.getElementById("keyValue");

    const url = "/account/save-key/"+keyName.value;

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
            alert(data);
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
            alert('Error saving key: ' + error.message);
        });
}