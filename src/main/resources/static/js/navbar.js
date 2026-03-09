(function() {
    function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
        return null;
    }

    async function setLanguage(lang) {
        document.cookie = `lang=${lang}; path=/; max-age=31536000`;
        const currentPath = window.location.pathname;
        try {
            const response = await fetch(`/public/translate-url?url=${encodeURIComponent(currentPath)}&targetLang=${lang}`);
            if (response.ok) {
                const newPath = await response.text();
                window.location.href = window.location.origin + newPath + window.location.search + window.location.hash;
            } else {
                location.reload();
            }
        } catch {
            location.reload();
        }
    }

    document.addEventListener('DOMContentLoaded', function() {
        const currentLang = getCookie('lang') || 'pl';
        const btnText = document.getElementById('currentLangText');
        const dropdownBtn = document.getElementById('langDropdownBtn');
        const dropdownMenu = document.getElementById('langDropdownMenu');
        const items = document.querySelectorAll('.dropdown-item');

        if (btnText) btnText.textContent = currentLang.toUpperCase();

        dropdownBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            dropdownMenu.classList.toggle('show');
        });

        items.forEach(item => {
            item.addEventListener('click', function() {
                const lang = this.dataset.lang;
                setLanguage(lang);
            });
        });

        document.addEventListener('click', function() {
            dropdownMenu.classList.remove('show');
        });
    });
})();