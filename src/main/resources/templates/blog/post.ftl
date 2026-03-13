<#import "/spring.ftl" as spring />
<!DOCTYPE html>
<html>
<head>
    <title>${post.title}</title>
    <#include "/parts/links.ftl" />
    <link rel="stylesheet" href="/static/css/blog-post.css">
</head>
<body>
<#include "/parts/navbar.ftl">
<div class="mesh-background">
    <div class="cloud cloud-1"></div>
    <div class="cloud cloud-2"></div>
    <div class="cloud cloud-3"></div>
</div>
<div class="container main-container">
    <div class="panel post-panel">
        <#if post.thumbnailUrl??>
        <div class="post-featured-image">
            <img src="${post.thumbnailUrl}" alt="${post.title}">
        </div>
    </#if>
    <h1 class="post-title">${post.title}</h1>
    <div class="post-meta">
        <span class="post-date">${post.createdAt?datetime.iso?string('dd-MM-yyyy HH:mm')}</span>
        <span class="post-lang">Język: ${post.lang}</span>
    </div>
    <div class="post-content">
        ${post.content}
    </div>

    <#if post.parent??>
    <div class="post-navigation">
        <a href="/blog/${post.parent.slug}" class="btn btn-outline">← wersja oryginalna</a>
    </div>
</#if>

<#if postTranslations?? && postTranslations?size gt 0>
<div class="post-translations">
    <h3>Inne wersje językowe:</h3>
    <ul>
        <#list postTranslations as t>
        <li><a href="/blog/${t.slug}">${t.lang}</a></li>
    </#list>
    </ul>
</div>
</#if>

<div class="post-back">
    <a href="/blog" class="btn btn-primary">← powrót do listy</a>
</div>
</div>
</div>
<#include "/parts/footer.ftl">
</body>
</html>