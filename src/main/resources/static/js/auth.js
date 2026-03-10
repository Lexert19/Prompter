const googleBtn = document.getElementById('google-login-btn');
if (googleBtn) {
    if (localStorage.getItem('termsAccepted') === 'true') {
        googleBtn.onclick = function() {
            window.location.href = '/oauth2/authorization/google';
        };
    } else {
        googleBtn.onclick = function(e) {
            e.preventDefault();
            const modal = document.getElementById('termsModal');
            if (modal) modal.style.display = 'flex';
        };
    }
}

document.getElementById('acceptTerms')?.addEventListener('change', function() {
    const acceptBtn = document.getElementById('acceptAndContinue');
    if (acceptBtn) acceptBtn.disabled = !this.checked;
});

document.getElementById('acceptAndContinue')?.addEventListener('click', function() {
    localStorage.setItem('termsAccepted', 'true');
    window.location.href = '/oauth2/authorization/google';
});

window.closeModal = function() {
    const modal = document.getElementById('termsModal');
    if (modal) modal.style.display = 'none';
};

window.onclick = function(event) {
    const modal = document.getElementById('termsModal');
    if (modal && event.target == modal) {
        modal.style.display = 'none';
    }
};