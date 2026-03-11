<#import "/spring.ftl" as spring />
<!DOCTYPE html>
<html>
    <head>
        <title>${post.title}</title>
        <#include "/parts/links.ftl" />
    </head>
    <body>
        <#include "/parts/navbar.ftl">
        <div class="mesh-background">
            <div class="cloud cloud-1"></div>
            <div class="cloud cloud-2"></div>
            <div class="cloud cloud-3"></div>
        </div>
        <div class="container main-container">
            <div class="panel">
                <h1>${post.title}</h1>
                <p>${post.createdAt?datetime.iso?string('dd-MM-yyyy HH:mm')} | Język: ${post.lang}</p>
                <div>${post.content}</div>
                <#if post.parent??>
                <p>
                    <a href="/blog/${post.parent.slug}">← wersja oryginalna</a>
                </p>
                </#if>
                <#if postTranslations??>
                <p>
                    Inne wersje językowe:
                    <#list postTranslations as t>
                    <a href="/blog/${t.slug}">${t.lang}</a>
                    </#list>
                </p>
                </#if>
                <a href="/blog">← powrót do listy</a>
            </div>
        </div>
        <#include "/parts/footer.ftl">
    </body>
</html>
