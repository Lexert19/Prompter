<#import "/spring.ftl" as spring />
<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "/parts/links.ftl" />
        <link rel="stylesheet" href="/static/css/media.css">
        <title>Zarządzanie mediami</title>
    </head>
    <body class="background">
        <#include "/parts/navbar.ftl" />
        <div class="container" style="margin-top: 80px;">
            <div class="content-wrapper px-2">
                <h2 style="color: var(--accent);">Zarządzanie mediami</h2>
                <div class="upload-section">
                    <h4>Dodaj nowe zdjęcie</h4>
                    <form id="uploadForm" enctype="multipart/form-data">
                        <input type="hidden" name="_csrf" class="csrfToken"/>
                        <input type="file" id="fileInput" name="file" accept="image/*" required>
                        <button type="submit" class="btn btn-primary">Prześlij</button>
                    </form>
                    <div id="uploadResult" style="display: none;">
                        <p>Adres URL obrazka:</p>
                        <input type="text" id="imageUrl" readonly onclick="this.select()">
                        <button onclick="copyUrl()">Kopiuj</button>
                    </div>
                </div>
                <h4>Lista zdjęć</h4>
                <div id="mediaGrid" class="media-grid"></div>
            </div>
        </div>
        <script src="/static/otherJs/media.js"></script>
        <#include "/parts/footer.ftl" />
    </body>
</html>
