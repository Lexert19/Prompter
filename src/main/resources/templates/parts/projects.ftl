<div id="projects" class="projects">
    <h1>Projekty</h1>
    <!-- Sekcja dodawania projektu -->
    <div class="create-project">
        <h2>Utwórz nowy projekt</h2>
        <form class="project-form">
            <input type="text" placeholder="Nazwa projektu" class="project-name" required>
            <button type="submit" class="btn-create">Utwórz</button>
        </form>
    </div>
    <!-- Lista projektów -->
    <div class="projects-list">
        <h2>Twoje projekty</h2>
        <ul class="project-items">
            <!-- Przykładowy element listy -->
            <li class="project-item">
                <div class="project-card">
                    <h3>Nazwa projektu</h3>
                    <p>ID: <span>12345</span></p>
                    <button class="btn-view">Przejdź do projektu</button>
                </div>
            </li>
        </ul>
    </div>
    <!-- Sekcja szczegółów projektu -->
    <div class="project-details" style="display: none;">
        <h2>Szczegóły projektu</h2>
        <div class="detail-section">
            <p><strong>Nazwa:</strong> <span class="project-name-display"></span></p>
            <p><strong>Właściciel:</strong> <span class="project-owner"></span></p>
        </div>
        <!-- Dodawanie plików -->
        <div class="add-file-section">
            <h3>Dodaj plik</h3>
            <form class="file-form">
                <input type="text" placeholder="Nazwa pliku" class="file-name" required>
                <input type="text" placeholder="Ścieżka pliku" class="file-path" required>
                <button type="submit" class="btn-add-file">Dodaj plik</button>
            </form>
        </div>
        <!-- Sekcja indeksowania -->
        <div class="index-section">
            <button class="btn-index">Zaindeksuj projekt</button>
        </div>
        <!-- Sekcja wyszukiwania -->
        <div class="search-section">
            <h3>Podobne fragmenty</h3>
            <input type="text" placeholder="Wprowadź zapytanie" class="search-query">
            <button class="btn-search">Szukaj</button>
            <div class="results">
                <ul class="result-list">
                    <!-- Przykładowy wynik -->
                    <li class="result-item">Fragment 1...</li>
                </ul>
            </div>
        </div>
    </div>
    <!-- Komunikaty błędów -->
    <div class="error-messages" style="display: none;">
        <div class="alert alert-error">Błąd: Brak dostępu do projektu</div>
        <div class="alert alert-error">Błąd: Projekt nie istnieje</div>
    </div>
</div>


<style>
    /* Podstawowe style */
    .container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 20px;
    }

    .btn-create, .btn-add-file, .btn-index, .btn-search {
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

    .result-list {
        list-style: none;
        padding: 0;
    }

    .result-item {
        background: #f0f0f0;
        padding: 10px;
        margin: 5px 0;
        border-radius: 3px;
    }
</style>