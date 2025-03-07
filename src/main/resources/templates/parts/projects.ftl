<div id="projects" class="projects">
    <!-- Sekcja dodawania projektu -->
    <div class="create-project">
        <h2>Utwórz nowy projekt</h2>
        <button class="btn-create">Utwórz</button>
    </div>
    <!-- Lista projektów -->
    <div class="projects-list">
        <h2>Twoje projekty</h2>
        <select class="project-select">
            <option value="12345">Nazwa projektu</option>
        </select>
    </div>
    <!-- Sekcja szczegółów projektu -->
    <div class="project-details">
        <h2>Szczegóły projektu</h2>
        <div class="detail-section">
            <p><strong>Nazwa:</strong> <span class="project-name-display"></span></p>
            <p><strong>Właściciel:</strong> <span class="project-owner"></span></p>
        </div>
        <!-- Sekcja przeglądania plików -->
        <div class="file-browser">
            <div class="file-grid">
                <div class="file-item">📄 nazwa_pliku.txt</div>
                <div class="file-item">📄 inny_plik.doc</div>
                <!-- Dodaj więcej plików tutaj -->
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

.btn-create, .btn-add-file {
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
    grid-template-columns: repeat(7, 1fr);
    gap: 10px;
}

.file-item {
    display: flex;
    align-items: center;
    padding: 10px;
    border: 1px solid #eee;
    border-radius: 5px;
}

.icon-file {
    margin-right: 10px;
}

.icon-plus {
    font-size: 1.2em;
    margin-right: 5px;
}
</style>