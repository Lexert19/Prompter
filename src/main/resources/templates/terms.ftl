<#import "/spring.ftl" as spring />
<!DOCTYPE html>
<html lang="pl">
    <head>
        <#include "/parts/links.ftl" />
        <title>Regulamin - Prompter</title>
    </head>
    <body class="background">
        <#include "/parts/navbar.ftl" />
        <div class="container">
            <div class="content-wrapper px-2">
                <div class="legal-container">
                    <h1>Regulamin korzystania z Prompter</h1>

                </div>
            </div>
        </div>
        <style>
    .legal-container {
        max-width: 800px;
        margin: 80px auto 40px;
        background: rgba(45,45,49,0.95);
        padding: 40px;
        border-radius: 12px;
        color: white;
    }
    .legal-container h1 {
        color: var(--accent);
        margin-bottom: 0.5rem;
    }
    .legal-container .last-updated {
        color: #aaa;
        margin-bottom: 2rem;
        font-style: italic;
    }
    .legal-container h2 {
        color: var(--accent);
        font-size: 1.3rem;
        margin-top: 1.5rem;
        margin-bottom: 1rem;
    }
    .legal-container p, .legal-container li {
        color: #ddd;
        line-height: 1.6;
    }
    .legal-container ul {
        margin-left: 1.5rem;
        margin-bottom: 1rem;
    }
    .legal-section {
        margin-bottom: 2rem;
        border-bottom: 1px solid rgba(255,255,255,0.1);
        padding-bottom: 1rem;
    }
        </style>
        <#include "/parts/footer.ftl" />
    </body>
</html>
