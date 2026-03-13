<#import "/spring.ftl" as spring />
<!DOCTYPE html>
<html>
<head>
    <title>Blog</title>
    <#include "/parts/links.ftl" />
    <link rel="stylesheet" href="/static/css/blog.css">
</head>
<body>
<#include "/parts/navbar.ftl">
<div class="mesh-background">
    <div class="cloud cloud-1"></div>
    <div class="cloud cloud-2"></div>
    <div class="cloud cloud-3"></div>
</div>
<div class="container main-container">
    <div class="panel blog-panel">
        <div class="row g-4">
            <#list posts as p>
            <div class="col-md-6 px-5">
                <article class="post-item h-100">
                    <#if p.thumbnailUrl??>
                    <div class="post-thumbnail">
                        <img src="${p.thumbnailUrl}" alt="${p.title}" class="img-fluid">
                    </div>
                </#if>
                <div class="post-content p-3">
                    <h2 class="post-title h5">
                        <a href="/blog/${p.slug}" class="text-decoration-none stretched-link">${p.title}</a>
                    </h2>
                    <div class="post-meta small">
                        ${p.createdAt?datetime.iso?string('dd-MM-yyyy HH:mm')}
                    </div>
                    <p class="post-excerpt mt-2">
                        ${p.shortDescription!}
                    </p>
                </div>
                </article>
            </div>
        </#list>
    </div>
</div>
</div>
<#include "/parts/footer.ftl">
</body>
</html>