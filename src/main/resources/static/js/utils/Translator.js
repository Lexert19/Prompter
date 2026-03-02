class Translator {
    constructor() {
        this.translations = {};
        this.currentLang = 'pl';
    }

    async load(lang) {
        const response = await fetch(`/static/locales/${lang}.json`);
        this.translations = await response.json();
        this.currentLang = lang;
        //this.updateDOM();
    }

    t(key, params = {}) {
        let text = this.translations[key] || key;
        for (let [k, v] of Object.entries(params)) {
            text = text.replace(new RegExp(`{{${k}}}`, 'g'), v);
        }
        return text;
    }

    updateDOM() {
        document.querySelectorAll('[data-i18n]').forEach(el => {
            el.innerText = this.t(el.dataset.i18n);
        });
        document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
            el.placeholder = this.t(el.dataset.i18nPlaceholder);
        });
    }
}

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}

const t = new Translator();
const lang = getCookie('lang') || 'pl';
t.load(lang);