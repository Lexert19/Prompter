function getCookie(name) {
    const m = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    return m? decodeURIComponent(m[2]) : null;
}

function fetchWithAuth(url, options = {}) {
    options.headers = options.headers || {};
    const token = getToken();
    if (token) {
        options.headers['Authorization'] = 'Bearer ' + token;
    }
    return fetch(url, options);
}

function getToken() {
    return localStorage.getItem('jwt_token');
}