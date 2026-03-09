if (localStorage.getItem('termsAccepted') === 'true') {
    document.getElementById('google-login-btn').onclick = function() {
        window.location.href = '/oauth2/authorization/google';
    };
} else {
    document.getElementById('google-login-btn').onclick = function(e) {
        e.preventDefault();
        document.getElementById('termsModal').style.display = 'flex';
    };
}

document.getElementById('acceptTerms').addEventListener('change', function() {
    document.getElementById('acceptAndContinue').disabled = !this.checked;
});

document.getElementById('acceptAndContinue').addEventListener('click', function() {
    localStorage.setItem('termsAccepted', 'true');
    window.location.href = '/oauth2/authorization/google';
});

window.closeModal = function() {
    document.getElementById('termsModal').style.display = 'none';
};

window.onclick = function(event) {
    var modal = document.getElementById('termsModal');
    if (event.target == modal) {
        modal.style.display = 'none';
    }
};