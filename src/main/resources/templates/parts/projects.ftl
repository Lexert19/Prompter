<div id="projects" class="projects">
    <!-- Sekcja dodawania projektu -->
    <div class="create-project">
        <h4>Utwórz nowy projekt</h4>
        <button class="btn-create">Utwórz</button>
    </div>
    <!-- Lista projektów -->
    <div class="projects-list">
        <h4>Twoje projekty</h4>
        <select class="project-select">
            <option value="12345">Nazwa projektu</option>
        </select>
    </div>
    <!-- Sekcja szczegółów projektu -->
    <div class="project-details">
        <h4>Szczegóły projektu</h4>
        <div class="detail-section">
            <p><strong>Nazwa:</strong> <span class="project-name-display"></span></p>
        </div>
        <!-- Sekcja przeglądania plików -->
        <div class="file-browser">
            <div class="file-grid">
                <div class="file-item">
                    <span class="icon-file">📄</span>
                    <span class="file-name">nazwa_pliku.txt</span>
                </div>
                <div class="file-item">
                    <span class="icon-file">📄</span>
                    <span class="file-name">inny_plik.doc</span>
                </div>
                <!-- Dodaj więcej plików tutaj -->
                <div class="file-item">
                    <span class="icon-file">📄</span>
                    <span class="file-name">plik1.pdf</span>
                </div>
                <div class="file-item">
                    <span class="icon-file">📄</span>
                    <span class="file-name">plik2.docx</span>
                </div>
                <div class="file-item">
                    <span class="icon-file">📄</span>
                    <span class="file-name">plik3.xlsx</span>
                </div>
                <div class="file-item">
                    <span class="icon-file">📄</span>
                    <span class="file-name">plik4.jpg</span>
                </div>
                <div class="file-item">
                    <span class="icon-file">📄</span>
                    <span class="file-name">plik5.png</span>
                </div>
                <div class="file-item">
                    <span class="icon-file">📄</span>
                    <span class="file-name">plik6.zip</span>
                </div>
                <div class="file-item">
                    <span class="icon-file">📄</span>
                    <span class="file-name">plik7.rar</span>
                </div>
                <div class="file-item">
                    <span class="icon-file">📄</span>
                    <span class="file-name">plik8.mp3</span>
                </div>
                <div class="file-item">
                    <span class="icon-file">📄</span>
                    <span class="file-name">plik9.mp4</span>
                </div>
                <div class="file-item">
                    <span class="icon-file">📄</span>
                    <span class="file-name">plik10.avi</span>
                </div>
            </div>
        </div>
        <!-- Dodawanie plików -->
        <div class="add-file-section">
            <button class="btn-add-file">
                <span class="icon-plus">+</span> Dodaj plik
            </button>
        </div>
    </div>
    <!-- Komunikaty błędów -->
    <div class="error-messages" style="display: none;">
        <div class="alert alert-error">Błąd: Brak dostępu do projektu</div>
        <div class="alert alert-error">Błąd: Projekt nie istnieje</div>
    </div>
</div>
<style>
.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.btn-create,
.btn-add-file {
    background: #2196F3;
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 5px;
    cursor: pointer;
}

.project-card {
    border: 1px solid #ddd;
    padding: 15px;
    margin: 10px 0;
    border-radius: 5px;
}

.project-details {
    margin-top: 30px;
    border-top: 2px solid #eee;
    padding-top: 20px;
}

.alert-error {
    background: #ffdddd;
    color: #cc0000;
    padding: 10px;
    margin: 10px 0;
    border-radius: 5px;
}

.project-select {
    width: 100%;
    padding: 10px;
    border-radius: 5px;
    border: 1px solid #ddd;
}

.file-browser {
    max-height: 300px;
    overflow-y: auto;
    border: 1px solid #ddd;
    padding: 10px;
    border-radius: 5px;
    margin-top: 20px;
}

.file-grid {
    display: grid;
    grid-template-columns: repeat(5, minmax(100px, 1fr));
        gap: 10px;
}

.file-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 10px;
    border-radius: 5px;
    text-align: center;
}

.icon-file {
    font-size: 1.5em;
    margin-bottom: 5px;
}

.file-name {
    font-size: 0.8em;
    word-wrap: break-word;
    width: 100%;
}

.icon-plus {
    font-size: 1.2em;
    margin-right: 5px;
}
</style>