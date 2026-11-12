class TwoFactorManager {
    static _instance = null;

    static instance() {
        if (!TwoFactorManager._instance) {
            TwoFactorManager._instance = new TwoFactorManager();
        }
        return TwoFactorManager._instance;
    }

    constructor() {
        if (TwoFactorManager._instance) {
            return TwoFactorManager._instance;
        }
        TwoFactorManager._instance = this;

        this.status = false;
        this.email = '';
        this.lastSentEmail = '';
        this.elements = {};

        this.loadStatus();
    }

    async loadStatus() {
        try {
            const response = await fetchWithAuth('/api/user');
            if (!response.ok) throw new Error('Nie udało się pobrać statusu');
            const data = await response.json();
            this.status = data.twoFactorEnabled;
            this.email = data.twoFactorEmail || '';
            if (this.elements.statusDiv) this.updateUI();
        } catch (err) {
            console.error('Error loading 2FA status:', err);
        }
    }

    openModal() {
        const modalHtml = `
            <div class="twofa-modal d-flex flex-column">
                <div id="twofaStatus" class="mb-2"></div>
                <div id="twofaSetupForm">
                    <div class="mb-2">
                        <label for="twofaEmail" class="d-block mb-025">
                            ${t.t('account.twofactor.email')}
                        </label>
                        <input type="email" id="twofaEmail" class="rounded-1 w-100"
                               placeholder="${t.t('account.twofactor.email.placeholder')}">
                    </div>
                    <div class="mb-2">
                        <button class="rounded-1 w-100 d-flex align-items-center justify-content-between indigo-button btn-sm"
                                id="twofaSendCodeBtn" type="button">
                            ${t.t('account.twofactor.sendCode')}
                        </button>
                    </div>
                    <div class="mb-2">
                        <label for="twofaCode" class="d-block mb-025">
                            ${t.t('account.twofactor.code')}
                        </label>
                        <input type="text" id="twofaCode" class="rounded-1 w-100" maxlength="6">
                    </div>
                    <div class="mb-2 d-flex gap-2">
                        <button class="rounded-1 indigo-button btn-primary" id="twofaEnableBtn" type="button">
                            ${t.t('account.twofactor.enable')}
                        </button>
                        <button class="rounded-1 btn-danger" id="twofaDisableBtn" type="button" style="display:none;">
                            ${t.t('account.twofactor.disable')}
                        </button>
                    </div>
                    <div class="mt-1 text-danger" id="twofaErrorMsg"></div>
                    <div class="mt-1 text-success" id="twofaSuccessMsg"></div>
                </div>
            </div>
        `;

        Modal.instance().open(t.t('account.twofactor'), modalHtml, null);

        this.elements.statusDiv  = document.getElementById('twofaStatus');
        this.elements.emailInput = document.getElementById('twofaEmail');
        this.elements.sendBtn    = document.getElementById('twofaSendCodeBtn');
        this.elements.codeInput  = document.getElementById('twofaCode');
        this.elements.enableBtn  = document.getElementById('twofaEnableBtn');
        this.elements.disableBtn = document.getElementById('twofaDisableBtn');
        this.elements.errorMsg   = document.getElementById('twofaErrorMsg');
        this.elements.successMsg = document.getElementById('twofaSuccessMsg');

        this.elements.sendBtn.addEventListener('click', () => this.sendTestCode());
        this.elements.enableBtn.addEventListener('click', () => this.enableTwoFactor());
        this.elements.disableBtn.addEventListener('click', () => this.disableTwoFactor());

        this.updateUI();
    }

    updateUI() {
        const { statusDiv, emailInput, sendBtn, enableBtn, disableBtn, codeInput, errorMsg, successMsg } = this.elements;
        if (!statusDiv) return;

        if (errorMsg) errorMsg.textContent = '';
        if (successMsg) successMsg.textContent = '';

        if (this.status) {
            statusDiv.innerHTML = `<span class="text-success">${t.t('enabled')}</span><br>
                                   <small>${t.t('account.twofactor.email')}: ${this.email}</small>`;
            emailInput.disabled = true;
            if (this.email) emailInput.value = this.email;
            sendBtn.style.display = 'none';
            enableBtn.style.display = 'none';
            disableBtn.style.display = 'inline-block';
            codeInput.disabled = false;
        } else {
            statusDiv.innerHTML = `<span class="text-secondary">${t.t('disabled')}</span>`;
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
            this.showError('Enter an email address');
            return;
        }
        try {
            const response = await fetchWithAuth('/api/2fa/send-test', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ email: email })
            });
            if (!response.ok) {
                const err = await response.json();
                throw new Error(err.message || 'Failed to send code');
            }
            this.lastSentEmail = email;
            this.showSuccess(`A code has been sent to ${email}`);
            this.elements.codeInput.focus();
        } catch (err) {
            this.showError(err.message);
        }
    }

    async enableTwoFactor() {
        const email = this.elements.emailInput.value.trim();
        const code = this.elements.codeInput.value.trim();
        if (!email || !code) {
            this.showError('Enter email address and code');
            return;
        }
        try {
            const response = await fetchWithAuth('/api/2fa/enable', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ email: email, code: code })
            });
            const data = await response.json();
            if (response.ok && data.success) {
                this.status = true;
                this.email = email;
                this.updateUI();
                this.showSuccess('2FA has been enabled');
                this.elements.codeInput.value = '';
            } else {
                this.showError(data.error || 'Invalid code');
            }
        } catch (err) {
            this.showError('Server connection error');
        }
    }

    async disableTwoFactor() {
        const code = this.elements.codeInput.value.trim();
        if (!code) {
            this.showError('Enter verification code');
            return;
        }
        try {
            const response = await fetchWithAuth('/api/2fa/disable', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ code: code })
            });
            const data = await response.json();
            if (response.ok && data.success) {
                this.status = false;
                this.email = '';
                this.updateUI();
                this.showSuccess('2FA has been disabled');
                this.elements.codeInput.value = '';
            } else {
                this.showError(data.error || 'Invalid code');
            }
        } catch (err) {
            this.showError('Server connection error');
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    TwoFactorManager.instance();
});