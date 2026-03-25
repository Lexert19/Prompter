document.addEventListener('DOMContentLoaded', async function() {
    await loadMediaList();

    document.getElementById('uploadForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData();
        formData.append('file', document.getElementById('fileInput').files[0]);
        try {
            const response = await fetchWithCsrf('/api/admin/media/upload', { method: 'POST', body: formData });
            if (response.ok) {
                const imageUrl = await response.text();
                document.getElementById('imageUrl').value = window.location.origin + imageUrl;
                document.getElementById('uploadResult').style.display = 'block';
                setTimeout(() => location.reload(), 1500);
            } else {
                alert('Błąd przesyłania');
            }
        } catch (error) {
            alert('Błąd: ' + error);
        }
    });

    window.copyUrl = function() {
        const urlInput = document.getElementById('imageUrl');
        urlInput.select();
        document.execCommand('copy');
        alert('Skopiowano!');
    };

    window.deleteMedia = async (event, id) => {
        event.preventDefault();
        if (!confirm('Czy na pewno usunąć?')) return;
        try {
            const response = await fetchWithCsrf(`/api/admin/media/${id}`, { method: 'DELETE' });
            if (response.ok) location.reload();
            else alert('Błąd usuwania');
        } catch (error) {
            alert('Błąd: ' + error);
        }
    };

    async function loadMediaList() {
        try {
            const response = await fetchWithCsrf('/api/admin/media');
            if (!response.ok) throw new Error();
            const mediaList = await response.json();
            renderMediaGrid(mediaList);
        } catch (error) {
            console.error('Błąd:', error);
        }
    }

    function renderMediaGrid(mediaList) {
        const grid = document.getElementById('mediaGrid');
        grid.innerHTML = '';
        if (!mediaList.length) {
            grid.innerHTML = '<p>Brak przesłanych zdjęć.</p>';
            return;
        }
        mediaList.forEach(media => {
            const item = document.createElement('div');
            item.className = 'media-item';
            item.innerHTML = `
            <img src="${media.url}" alt="${media.fileName}">
            <div class="media-info">
                <div>${media.fileName}</div>
                <div>${new Date(media.uploadedAt).toLocaleString()}</div>
            </div>
            <form class="delete-form" onsubmit="deleteMedia(event, ${media.id})">
                <button type="submit" class="delete-btn" title="Usuń">×</button>
            </form>
        `;
            grid.appendChild(item);
        });
    }
});