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


(() => {
    const loginForm = document.getElementById('login-form');
    const loginBtn = document.getElementById('login-btn');
    const errorBox = document.getElementById('login-error');
    const googleBtn = document.getElementById('google-login-btn');

    const twofaModal = document.getElementById('twofaModal');
    const twofaInput = document.getElementById('twofa-code');
    const twofaSubmit = document.getElementById('twofa-submit');
    const twofaCancel = document.getElementById('twofa-cancel');
    const twofaError = document.getElementById('twofa-error');

    let preAuthToken = null;

    function showError(el, msg) {
        el.textContent = msg;
        el.style.display = 'block';
    }
    function hideError(el) {
        el.style.display = 'none';
    }

        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            hideError(errorBox);
            loginBtn.disabled = true;

            const email = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;

            try {
                const res = await fetch('/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify({ email, password })
                });

                const data = await res.json().catch(() => ({}));

                if (!res.ok) {
                    showError(errorBox, data.error || 'Nieprawidłowy email lub hasło');
                    return;
                }

                if (data.requires2fa) {
                    preAuthToken = data.preAuthToken;
                    twofaModal.style.display = 'flex';
                    twofaInput.focus();
                    return;
                }
                window.location.href = '/chat';
            } catch (err) {
                showError(errorBox, 'Błąd połączenia');
            } finally {
                loginBtn.disabled = false;
            }
        });

    async function verify2fa() {
        hideError(twofaError);
        const code = twofaInput.value.trim();
        if (!code) return;

        twofaSubmit.disabled = true;
        try {
            const res = await fetch('/auth/2fa/verify?code=' + encodeURIComponent(code), {
                method: 'POST',
                headers: { 'X-Pre-Auth-Token': preAuthToken },
                credentials: 'include'
            });
            const data = await res.json().catch(() => ({}));
            if (!res.ok) {
                showError(twofaError, data.error || 'Nieprawidłowy kod');
                return;
            }
            window.location.href = '/chat';
        } catch {
            showError(twofaError, 'Błąd połączenia');
        } finally {
            twofaSubmit.disabled = false;
        }
    }

    if (twofaSubmit) twofaSubmit.addEventListener('click', verify2fa);
    if (twofaInput) twofaInput.addEventListener('keydown', e => { if (e.key === 'Enter') verify2fa(); });
    if (twofaCancel) twofaCancel.addEventListener('click', () => {
        twofaModal.style.display = 'none';
        preAuthToken = null;
    });

    if (googleBtn) {
        googleBtn.addEventListener('click', () => {
            window.location.href = '/oauth2/authorization/google';
        });
    }

    const params = new URLSearchParams(window.location.search);
    const oauthToken = params.get('token');
    if (oauthToken) {
        window.history.replaceState({}, '', '/chat');
        window.location.href = '/chat';
    }
})();