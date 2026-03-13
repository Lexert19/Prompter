<#import "/spring.ftl" as spring />
<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "/parts/links.ftl" />
        <title>Edycja wpisu</title>
    </head>
    <body class="background">
        <#include "/parts/navbar.ftl" />
        <div id="modalOverlay" class="overlay"></div>
        <div id="modal" class="modal-menu">
            <div class="modal-header">
                <h3 id="modalTitle"></h3>
                <button class="close-modal">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div id="modalContent" class="modal-content"></div>
        </div>
        <div class="container" style="margin-top: 80px;">
            <div class="content-wrapper px-2">
                <div id="post-form-container"></div>
            </div>
        </div>
        <script src="/static/js/ui/Modal.js"></script>
        <script src="/static/otherJs/blog-edit.js"></script>
        <#include "/parts/footer.ftl" />
    </body>
</html>
