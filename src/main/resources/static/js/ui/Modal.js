class Modal {
    constructor() {
        this.overlay = document.getElementById('modalOverlay');
        this.modal = document.getElementById('modal');
        this.titleEl = document.getElementById('modalTitle');
        this.contentEl = document.getElementById('modalContent');
        this.closeBtn = this.modal.querySelector('.close-modal');

        this.closeBtn.addEventListener('click', () => this.close());
        this.overlay.addEventListener('click', () => this.close());
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.modal.classList.contains('show')) this.close();
        });
    }

    open(title, contentHtml, onSubmit = null) {
        this.titleEl.textContent = title;
        this.contentEl.innerHTML = contentHtml;
        this.overlay.classList.add('show');
        this.modal.classList.add('show');

        if (onSubmit) {
            const form = this.contentEl.querySelector('form');
            if (form) {
                form.addEventListener('submit', (e) => {
                    e.preventDefault();
                    onSubmit(new FormData(form));
                }, { once: true });
            }
        }
    }

    close() {
        this.overlay.classList.remove('show');
        this.modal.classList.remove('show');
        setTimeout(() => {
            if (!this.modal.classList.contains('show')) {
                this.contentEl.innerHTML = '';
            }
        }, 300);
    }
}

window.modal = new Modal();