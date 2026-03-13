<#import "/spring.ftl" as spring />
<!DOCTYPE html>
<html>
    <head>
        <title>Blog</title>
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
                <h1>Blog</h1>
                <#list posts as p>
                <div style="border-bottom:1px solid #ccc; padding:20px 0;">
                    <#if p.thumbnailId??>
                    <img src="/api/files/${p.thumbnailId}" style="width: 150px; height: 150px; object-fit: cover;">
                </#if>
                    <div>
                    <h2>
                        <a href="/blog/${p.slug}">${p.title}</a>
                    </h2>
                    <p>${p.createdAt?datetime.iso?string('dd-MM-yyyy HH:mm')}</p>
                    <p>${(p.content?length > 200)?then(p.content?substring(0,200) + '...', p.content)}</p>
                    </div>
                </div>
                </#list>
            </div>
        </div>
        <#include "/parts/footer.ftl">
    </body>
</html>
