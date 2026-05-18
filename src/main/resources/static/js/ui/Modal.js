class Modal {
    static _instance = null;

    static instance() {
        if (!Modal._instance) {
            Modal._instance = new Modal();
        }
        return Modal._instance;
    }

    constructor() {
        if (Modal._instance) {
            return Modal._instance;
        }
        Modal._instance = this;

        this.overlay = document.getElementById('modalOverlay');
        this.modal = document.getElementById('modal');
        this.titleEl = document.getElementById('modalTitle');
        this.contentEl = document.getElementById('modalContent');
        this.closeBtn = this.modal.querySelector('.close-modal');

        this.currentOptions = [];

        this.closeBtn.addEventListener('click', () => this.close());
        this.overlay.addEventListener('click', () => this.close());
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.modal.classList.contains('show')) this.close();
        });
    }

    open(title, contentHtml, onSubmit = null, options = []) {
        this.titleEl.textContent = title;
        this.contentEl.innerHTML = contentHtml;
        this.overlay.classList.add('show');
        this.modal.classList.add('show');
        this.setOptions(options);

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

    setOptions(options){
        this.currentOptions = Array.isArray(options) ? options.filter(Boolean) : [];
        for (const name of this.currentOptions) {
            switch (name) {
                case 'modal-wide':
                case 'modal-small':
                case 'dark':
                    this.modal.classList.add(name);
                    break;
                default:
                    if (typeof name === 'string') {
                        this.modal.classList.add(name);
                    }
            }
        }
    }

    removeOptions(){
        for (const name of this.currentOptions) {
            switch (name) {
                case 'modal-wide':
                case 'modal-small':
                case 'dark':
                    this.modal.classList.remove(name);
                    break;
                default:
                    if (typeof name === 'string') {
                        this.modal.classList.remove(name);
                    }
            }
        }
        this.currentOptions = [];
    }

    close() {
        this.overlay.classList.remove('show');
        this.modal.classList.remove('show');
        setTimeout(() => {
            this.removeOptions();
            if (!this.modal.classList.contains('show')) {
                this.contentEl.innerHTML = '';
            }
        }, 300);
    }
}

Modal.instance();
