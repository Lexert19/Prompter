<div id="projects" class="projects">
    <div class="projects-list">
        <label>Twoje projekty</label>
        <div class="d-flex">
            <select class="project-select">
                <option value="12345">Nazwa projektu</option>
            </select>
            <button class="btn-create">Utwórz</button>
        </div>
    </div>
    <!-- Sekcja szczegółów projektu -->
    <div class="project-details">
        <label>Szczegóły projektu</label>
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
        <div class="add-file-section mt">
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
