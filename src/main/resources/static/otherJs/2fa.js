document.addEventListener('DOMContentLoaded', function() {
    const resendLink = document.getElementById('resendCodeLink');
    if (!resendLink) return;

    const originalLabel = resendLink.getAttribute('data-resend-label');
    const sendingLabel = resendLink.getAttribute('data-sending-label');
    const successMsg = resendLink.getAttribute('data-success-msg');
    const errorMsg = resendLink.getAttribute('data-error-msg');

    resendLink.addEventListener('click', async (e) => {
        e.preventDefault();
        const link = e.target;
        link.textContent = sendingLabel;
        link.style.pointerEvents = 'none';

        try {
            const response = await fetchWithCsrf('/auth/2fa/resend', { method: 'POST' });
            if (response.ok) {
                const msgDiv = document.createElement('div');
                msgDiv.className = 'alert-success';
                msgDiv.textContent = successMsg;
                const form = document.getElementById('twoFactorForm');
                form.parentNode.insertBefore(msgDiv, form);
                setTimeout(() => msgDiv.remove(), 5000);
            } else {
                throw new Error();
            }
        } catch (err) {
            const msgDiv = document.createElement('div');
            msgDiv.className = 'alert-error';
            msgDiv.textContent = errorMsg;
            const form = document.getElementById('twoFactorForm');
            form.parentNode.insertBefore(msgDiv, form);
            setTimeout(() => msgDiv.remove(), 5000);
        } finally {
            link.textContent = originalLabel;
            link.style.pointerEvents = 'auto';
        }
    });
});