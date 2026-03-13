document.addEventListener('DOMContentLoaded', async function() {
    await loadPosts();

    const postForm = document.getElementById('postForm');
    if (postForm) {
        postForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(postForm);
            const postData = {
                title: formData.get('title'),
                slug: formData.get('slug') || '',
                content: formData.get('content'),
                lang: formData.get('lang') || 'pl',
                parent: formData.get('parentId') ? { id: formData.get('parentId') } : null
            };
            const postId = formData.get('id');
            const url = postId ? `/api/admin/blog/posts/${postId}` : '/api/admin/blog/posts';
            const method = postId ? 'PUT' : 'POST';

            try {
                const response = await fetch(url, {
                    method: method,
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(postData)
                });
                if (response.ok) {
                    window.location.href = '/admin/blog';
                } else {
                    alert('Błąd zapisu');
                }
            } catch (error) {
                alert('Błąd: ' + error);
            }
        });
    }
});

async function loadPosts() {
    try {
        const response = await fetch('/api/admin/blog/posts');
        if (!response.ok) throw new Error('Failed to load posts');
        const posts = await response.json();
        renderPosts(posts);
    } catch (error) {
        console.error('Error loading posts:', error);
    }
}

function renderPosts(posts) {
    const tbody = document.getElementById('posts-table-body');
    tbody.innerHTML = '';

    if (posts.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" style="text-align: center;">Brak wpisów</td></tr>';
        return;
    }

    posts.forEach(post => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${escapeHtml(post.title)}</td>
            <td>${formatDate(post.createdAt)}</td>
            <td>
                <a href="/admin/blog/edit/${post.id}" class="btn-small">Edytuj</a>
                <button class="btn-small btn-danger delete-post-btn" data-id="${post.id}">Usuń</button>
            </td>
        `;
        tbody.appendChild(row);
    });

    attachDeleteHandlers();
}

function formatDate(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleDateString('pl-PL');
}

function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function attachDeleteHandlers() {
    document.querySelectorAll('.delete-post-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.preventDefault();
            if (!confirm('Czy na pewno usunąć ten wpis?')) return;
            const postId = btn.dataset.id;
            try {
                const response = await fetch(`/api/admin/blog/posts/${postId}`, {
                    method: 'DELETE'
                });
                if (response.ok) {
                    await loadPosts();
                } else {
                    alert('Błąd usuwania');
                }
            } catch (error) {
                alert('Błąd: ' + error);
            }
        });
    });
}