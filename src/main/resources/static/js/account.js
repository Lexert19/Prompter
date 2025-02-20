

function toggleUserPanel() {
    var panel = document.getElementById("accountPanel");

    panel.classList.toggle('active');
}

function saveKey(key) {
    const inputKey = document.getElementById(key);

    let url;
    switch (key) {
        case "nemotronKey":
            url = "/account/nvidia-key";
            
            break;
        case "claudeKey":
            url = "/account/claude-key";     
            break;
        case "openaiKey":
            url = "/account/chatgpt-key";
            break;
        case "geminiKey":
            url = "/account/gemini-key";
            break;
        default:
            console.error("Invalid key type");
            return;
    }

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: inputKey.value,
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