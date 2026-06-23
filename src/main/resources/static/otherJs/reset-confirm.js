document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('resetConfirmForm');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = document.getElementById('submitBtn');
    btn.disabled = true;

    const payload = {
      token: form.dataset.token,
      password: document.getElementById('password').value,
      passwordConfirmation: document.getElementById('password_confirmation').value
    };

    const alertBox = document.getElementById('alert');

    if (payload.password !== payload.passwordConfirmation) {
      showError('Passwords do not match');
      btn.disabled = false;
      return;
    }

    try {
      const res = await fetch('/auth/reset-password-confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        credentials: 'include'
      });

      if (res.ok) {
        window.location.href = '/auth/login?reset=success';
      } else {
        const err = await res.json().catch(() => ({ message: 'Invalid token' }));
        showError(err.message);
      }
    } catch (err) {
      showError('Network error');
    } finally {
      btn.disabled = false;
    }

    function showError(msg) {
      alertBox.style.display = 'block';
      alertBox.className = 'alert-error';
      alertBox.innerHTML = `<p>${msg}</p>`;
    }
  });
});