class TwoFactorManager {
    constructor() {
        this.status = false;
        this.email = '';
        this.lastSentEmail = '';
        this.elements = {
            statusDiv: null,
            emailInput: null,
            sendBtn: null,
            codeInput: null,
            enableBtn: null,
            disableBtn: null,
            errorMsg: null,
            successMsg: null
        };
        this.init();
    }

    init() {
        this.elements.statusDiv = document.getElementById('twofaStatus');
        this.elements.emailInput = document.getElementById('twofaEmail');
        this.elements.sendBtn = document.getElementById('twofaSendCodeBtn');
        this.elements.codeInput = document.getElementById('twofaCode');
        this.elements.enableBtn = document.getElementById('twofaEnableBtn');
        this.elements.disableBtn = document.getElementById('twofaDisableBtn');
        this.elements.errorMsg = document.getElementById('twofaErrorMsg');
        this.elements.successMsg = document.getElementById('twofaSuccessMsg');

        if (!this.elements.statusDiv) return;

        this.elements.sendBtn.addEventListener('click', () => this.sendTestCode());
        this.elements.enableBtn.addEventListener('click', () => this.enableTwoFactor());
        this.elements.disableBtn.addEventListener('click', () => this.disableTwoFactor());

        this.loadStatus();
    }

    async loadStatus() {
        try {
            const response = await fetchWithCsrf('/api/user');
            if (!response.ok) throw new Error('Nie udało się pobrać statusu');
            const data = await response.json();
            this.status = data.twoFactorEnabled;
            this.email = data.twoFactorEmail || '';
            this.updateUI();
        } catch (err) {
            console.error('Błąd ładowania statusu 2FA:', err);
            this.showError('Nie udało się załadować ustawień 2FA');
        }
    }

    updateUI() {
        const { statusDiv, emailInput, sendBtn, enableBtn, disableBtn, codeInput, errorMsg, successMsg } = this.elements;
        if (errorMsg) errorMsg.textContent = '';
        if (successMsg) successMsg.textContent = '';

        if (this.status) {
            statusDiv.innerHTML = `<span class="text-success"></span><br>
                                   <small><@spring.message "account.twofactor.email" />: ${this.email}</small>`;
            emailInput.disabled = true;
            sendBtn.style.display = 'none';
            enableBtn.style.display = 'none';
            disableBtn.style.display = 'inline-block';
            codeInput.disabled = false;
        } else {
            statusDiv.innerHTML = `<span class="text-secondary"></span>`;
            emailInput.disabled = false;
            sendBtn.style.display = 'inline-block';
            enableBtn.style.display = 'inline-block';
            disableBtn.style.display = 'none';
            codeInput.disabled = false;
            codeInput.value = '';
        }
    }

    showError(msg) {
        if (this.elements.errorMsg) {
            this.elements.errorMsg.textContent = msg;
            setTimeout(() => this.elements.errorMsg.textContent = '', 5000);
        }
    }

    showSuccess(msg) {
        if (this.elements.successMsg) {
            this.elements.successMsg.textContent = msg;
            setTimeout(() => this.elements.successMsg.textContent = '', 5000);
        }
    }

    async sendTestCode() {
        const email = this.elements.emailInput.value.trim();
        if (!email) {
            this.showError('Podaj adres e-mail');
            return;
        }
        try {
            const response = await fetchWithCsrf('/api/2fa/send-test', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ email: email })
            });
            if (!response.ok) {
                const err = await response.json();
                throw new Error(err.message || 'Błąd wysyłania kodu');
            }
            this.lastSentEmail = email;
            this.showSuccess(`Kod został wysłany na adres ${email}`);
            this.elements.codeInput.focus();
        } catch (err) {
            this.showError(err.message);
        }
    }

    async enableTwoFactor() {
        const email = this.elements.emailInput.value.trim();
        const code = this.elements.codeInput.value.trim();
        if (!email || !code) {
            this.showError('Podaj adres e-mail i kod');
            return;
        }
        try {
            const response = await fetchWithCsrf('/api/2fa/enable', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ email: email, code: code })
            });
            const data = await response.json();
            if (response.ok && data.success) {
                this.status = true;
                this.email = email;
                this.updateUI();
                this.showSuccess('2FA zostało włączone');
                this.elements.codeInput.value = '';
            } else {
                this.showError(data.error || 'Nieprawidłowy kod');
            }
        } catch (err) {
            this.showError('Błąd połączenia z serwerem');
        }
    }

    async disableTwoFactor() {
        const code = this.elements.codeInput.value.trim();
        if (!code) {
            this.showError('Podaj kod weryfikacyjny');
            return;
        }
        try {
            const response = await fetchWithCsrf('/api/2fa/disable', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ code: code })
            });
            const data = await response.json();
            if (response.ok && data.success) {
                this.status = false;
                this.email = '';
                this.updateUI();
                this.showSuccess('2FA zostało wyłączone');
                this.elements.codeInput.value = '';
            } else {
                this.showError(data.error || 'Nieprawidłowy kod');
            }
        } catch (err) {
            this.showError('Błąd połączenia z serwerem');
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('twofaStatus')) {
        window.twoFactorManager = new TwoFactorManager();
    }
});