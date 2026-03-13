<#import "/spring.ftl" as spring />
<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "/parts/links.ftl" />
        <title>${post.id?has_content?then('Edycja','Nowy')} wpis</title>
    </head>
    <body class="background">
        <#include "/parts/navbar.ftl" />
        <div class="container" style="margin-top: 80px;">
            <div class="content-wrapper px-2">
                <h2 style="color: var(--accent);">${post.id?has_content?then('Edytuj','Nowy')} wpis</h2>
                <form id="postForm">
                    <input type="hidden" name="id" value="${post.id!}">
                    <input type="hidden" name="parentId" value="${(post.parent.id)!}">
                    <div class="mb-2">
                        <label>Tytuł</label>
                        <input type="text"
                               name="title"
                               value="${post.title!}"
                               required
                               class="form-control">
                    </div>
                    <div class="mb-2">
                        <label>Slug (opcjonalny)</label>
                        <input type="text" name="slug" value="${post.slug!}" class="form-control">
                    </div>
                    <div class="mb-2">
                        <label>Język (pl/en)</label>
                        <input type="text"
                               name="lang"
                               value="${post.lang!'pl'}"
                               maxlength="2"
                               required
                               class="form-control">
                    </div>
                    <div class="mb-2">
                        <label>Treść</label>
                        <textarea name="content" rows="15" required class="form-control">${post.content!}</textarea>
                    </div>
                    <button type="submit" class="btn btn-primary">Zapisz</button>
                    <a href="/admin/blog" class="btn btn-secondary">Anuluj</a>
                </form>
            </div>
        </div>
        <script src="/static/otherJs/blog-admin.js"></script>
        <#include "/parts/footer.ftl" />
    </body>
</html>
