
function fetchWithCsrf(url, options = {}) {
    if (!options.headers) options.headers = {};
    if (options.method && ['POST', 'PUT', 'DELETE'].includes(options.method.toUpperCase())) {
        options.headers['X-XSRF-TOKEN'] = getCsrfToken();
    }
    return fetch(url, options);
}

    function getCsrfToken() {
        const name = 'XSRF-TOKEN';
        const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
        return match ? match[2] : null;
    }
