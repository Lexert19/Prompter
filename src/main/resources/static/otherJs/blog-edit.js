document.addEventListener('DOMContentLoaded', async () => {
    const container = document.getElementById('post-form-container');
    const path = window.location.pathname;
    const isEdit = path.includes('/edit/');
    const postId = isEdit ? path.split('/').pop() : null;

    let post = null;
    if (isEdit) {
        try {
            const res = await fetch(`/api/admin/blog/posts/${postId}`, { credentials: 'include' });
            if (!res.ok) throw new Error('Nie udało się pobrać danych');
            post = await res.json();
        } catch (err) {
            container.innerHTML = `<p class="text-danger">Błąd: ${err.message}</p>`;
            return;
        }
    }

    container.innerHTML = `
        <h2 style="color: var(--accent);">${post ? 'Edytuj' : 'Nowy'} wpis</h2>
        <form id="postForm">
            <input type="hidden" id="postId" value="${post?.id || ''}">
            <div class="mb-2">
                <label for="title">Tytuł</label>
                <input type="text" id="title" name="title" value="${post?.title || ''}" class="form-control" required>
            </div>
            <div class="mb-2">
                <label for="slug">Slug (opcjonalny)</label>
                <input type="text" id="slug" name="slug" value="${post?.slug || ''}" class="form-control">
            </div>
            <div class="mb-2">
                <label for="lang">Język (pl/en)</label>
                <input type="text" id="lang" name="lang" value="${post?.lang || 'pl'}" maxlength="2" class="form-control" required>
            </div>
            <div class="mb-2">
                <label for="shortDescription">Krótki opis</label>
                <textarea id="shortDescription" name="shortDescription" rows="3" class="form-control">${post?.shortDescription || ''}</textarea>
            </div>
            <div class="mb-2">
                <label for="content">Treść</label>
                <textarea id="content" name="content" rows="15" class="form-control" required>${post?.content || ''}</textarea>
            </div>
            <div class="mb-2">
                <label for="thumbnailId">Miniaturka (ID obrazka)</label>
                <div class="d-flex align-items-center">
                    <input type="number" name="thumbnailId" id="thumbnailId" class="form-control" style="width: 150px;" placeholder="ID" value="${post?.thumbnailId || ''}">
                    <button type="button" class="btn btn-secondary ms-2" onclick="openMediaPicker()">Wybierz z biblioteki</button>
                </div>
                <div id="thumbnailPreview" class="mt-2"></div>
            </div>
            <button type="submit" class="btn btn-primary">Zapisz</button>
            <a href="/admin/blog" class="btn btn-secondary">Anuluj</a>
        </form>
    `;

    document.getElementById('postForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const data = {
            title: formData.get('title'),
            slug: formData.get('slug') || '',
            content: formData.get('content'),
            lang: formData.get('lang') || 'pl',
            shortDescription: formData.get('shortDescription') || '',
            thumbnailId: formData.get('thumbnailId') ? parseInt(formData.get('thumbnailId')) : null
        };

        const url = postId ? `/api/admin/blog/posts/${postId}` : '/api/admin/blog/posts';
        const method = postId ? 'PUT' : 'POST';

        try {
            const res = await fetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data),
                credentials: 'include'
            });
            if (res.ok) {
                window.location.href = '/admin/blog';
            } else {
                alert('Błąd zapisu: ' + await res.text());
            }
        } catch (err) {
            alert('Błąd: ' + err.message);
        }
    });
});

async function openMediaPicker() {
    try {
        const response = await fetch('/api/admin/media', { credentials: 'include' });
        if (!response.ok) throw new Error('Nie udało się pobrać mediów');
        const mediaList = await response.json();

        let contentHtml = '<div class="media-grid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 10px; max-height: 60vh; overflow-y: auto;">';
        mediaList.forEach(media => {
            contentHtml += `
                <div class="media-item-selectable" data-id="${media.id}" data-url="${media.url}" style="cursor: pointer; border: 1px solid #444; padding: 5px; border-radius: 5px; background: #333;">
                    <img src="${media.url}" alt="${media.fileName}" style="width: 100%; height: 120px; object-fit: cover; border-radius: 4px;">
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
                document.getElementById('thumbnailPreview').innerHTML = `<img src="${imgUrl}" style="max-width: 200px; max-height: 200px; border-radius: 4px;">`;
                window.modal.close();
            });
        });
    } catch (error) {
        console.error('Błąd podczas otwierania pickera:', error);
        alert('Nie udało się załadować biblioteki mediów.');
    }
}


async function loadThumbnailPreview() {
    const thumbnailId = document.getElementById('thumbnailId').value;
    if (thumbnailId) {
        try {
            const response = await fetch(`/api/admin/media/${thumbnailId}`, { credentials: 'include' });
            if (response.ok) {
                const media = await response.json();
                document.getElementById('thumbnailPreview').innerHTML = `<img src="${media.url}" style="max-width: 200px; max-height: 200px; border-radius: 4px;">`;
            }
        } catch (error) {
            console.error('Błąd ładowania miniatury:', error);
        }
    }
}