<#import "/spring.ftl" as spring />
<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "/parts/links.ftl" />
        <title>Zarządzanie wpisami</title>
    </head>
    <body class="background">
        <#include "/parts/navbar.ftl" />
        <div class="mesh-background">
            <div class="cloud cloud-1"></div>
            <div class="cloud cloud-2"></div>
            <div class="cloud cloud-3"></div>
        </div>
        <div class="container main-container">
            <div class="content-wrapper px-2">
                <div class="panel p-3 d-flex flex-column w-100">
                    <h2 style="color: var(--accent);">Zarządzanie wpisami</h2>
                    <a href="/admin/blog/new" class="pb-3">+ Nowy wpis</a>
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>Miniaturka</th>
                                <th>Tytuł</th>
                                <th>Data</th>
                                <th style="width: 10%">Akcje</th>
                            </tr>
                        </thead>
                        <tbody id="posts-table-body">
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <script src="/static/otherJs/blog-admin.js"></script>
        <#include "/parts/footer.ftl" />
    </body>
</html>
