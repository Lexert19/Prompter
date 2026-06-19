document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('resetRequestForm');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = document.getElementById('submitBtn');
    btn.disabled = true;

    const payload = { email: form.email.value.trim() };
    const alertBox = document.getElementById('alert');

    try {
      const res = await fetch('/auth/reset-password-request', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        credentials: 'include'
      });

      alertBox.style.display = 'block';
      if (res.ok) {
        alertBox.className = 'alert-warning';
        alertBox.innerHTML = `<h3><i class="fas fa-exclamation-triangle"></i> Password Reset Request Sent</h3>
                                      <p>We have sent a link to ${payload.email}. Check your inbox.</p>`;
        form.reset();
      } else {
        const err = await res.json().catch(() => ({ message: 'Request failed' }));
        alertBox.className = 'alert-error';
        alertBox.textContent = err.message || 'Request failed';
      }
    } catch (err) {
      console.error(err);
    } finally {
      btn.disabled = false;
    }
  });
});