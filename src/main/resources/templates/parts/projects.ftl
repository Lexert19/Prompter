<div id="projects" class="projects">
    <div class="projects-list">
        <label>Twoje projekty</label>
        <div class="d-flex mt-05">
            <select id="project-select" class="project-select">
            </select>
            <button  id="btn-create"  class="btn-create">Utwórz</button>
        </div>
    </div>
    <div class="project-details">
        <label>Szczegóły projektu</label>
        <div class="detail-section">
            <p><strong>Nazwa:</strong> <span  id="project-name-display"  class="project-name-display"></span></p>
        </div>
        <div class="file-browser">
            <div class="file-grid">
               
            </div>
        </div>
        <div class="add-file-section mt-05">
            <input type="file" id="file-input" style="display: none;">
            <button id="btn-add-file" class="btn-add-file">
                <span class="icon-plus">+</span> Dodaj plik
            </button>
        </div>
    </div>
    <div id="error-messages"  class="error-messages" style="display: none;">
         <div class="alert alert-error"></div>
    </div>
</div>
