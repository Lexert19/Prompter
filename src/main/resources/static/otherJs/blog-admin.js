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
                parent: formData.get('parentId') ? { id: formData.get('parentId') } : null,
                thumbnailId: formData.get('thumbnailId') ? parseInt(formData.get('thumbnailId')) : null
            };
            const postId = formData.get('id');
            const url = postId ? `/api/admin/blog/posts/${postId}` : '/api/admin/blog/posts';
            const method = postId ? 'PUT' : 'POST';

            try {
                const response = await fetchWithCsrf(url, {
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
        const response = await fetchWithCsrf('/api/admin/blog/posts');
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
        tbody.innerHTML = '<tr><td colspan="4" style="text-align: center;">Brak wpisów</td></tr>';
        return;
    }

    posts.forEach(post => {
        const row = document.createElement('tr');
        const thumbnailHtml = post.thumbnailUrl
            ? `<img src="${post.thumbnailUrl}" style="width: 50px; height: 50px; object-fit: cover;">`
            : '';
        row.innerHTML = `
            <td>${thumbnailHtml}</td>
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
                const response = await fetchWithCsrf(`/api/admin/blog/posts/${postId}`, {
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

async function openMediaPicker() {
    try {
        const response = await fetchWithCsrf('/api/admin/media', {
            credentials: 'include'
        });
        if (!response.ok) throw new Error('Nie udało się pobrać mediów');
        const mediaList = await response.json();

        let contentHtml = '<div class="media-grid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 10px; max-height: 60vh; overflow-y: auto;">';

        mediaList.forEach(media => {
            const fileName = media.filePath.substring(media.filePath.lastIndexOf('/') + 1);
            const imgUrl = `/media/${fileName}`;

            contentHtml += `
                <div class="media-item-selectable"
                     data-id="${media.id}"
                     data-url="${imgUrl}"
                     style="cursor: pointer; border: 1px solid #444; padding: 5px; border-radius: 5px; background: #333;">
                    <img src="${imgUrl}" alt="${media.fileName}" style="width: 100%; height: 120px; object-fit: cover; border-radius: 4px;">
                    <div style="font-size: 0.8rem; text-align: center; margin-top: 5px; color: #ddd;">${media.fileName}</div>
                </div>
            `;
        });
        contentHtml += '</div>';

        window.modal.open('Wybierz miniaturkę', contentHtml, null);

        document.querySelectorAll('.media-item-selectable').forEach(el => {
            el.addEventListener('click', () => {
                const mediaId = el.dataset.id;
                const imgUrl = el.dataset.url;

                document.getElementById('thumbnailId').value = mediaId;

                const previewDiv = document.getElementById('thumbnailPreview');
                previewDiv.innerHTML = `<img src="${imgUrl}" style="max-width: 200px; max-height: 200px; border-radius: 4px;">`;

                window.modal.close();
            });
        });
    } catch (error) {
        console.error('Błąd podczas otwierania pickera:', error);
        alert('Nie udało się załadować biblioteki mediów.');
    }
}

function selectThumbnail(id, fileName) {
    document.getElementById('thumbnailId').value = id;
    const preview = document.getElementById('thumbnailPreview');
    preview.innerHTML = `<img src="/media/${fileName}" style="max-width: 200px; max-height: 200px;">`;
    window.modal.close();
}