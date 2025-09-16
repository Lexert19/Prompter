<#import "/spring.ftl" as spring/>
<#include "/parts/head.ftl" />
<div class="center" style="height: 100vh;">
    <div class="block" style="max-width: 600px; padding: 20px;">
        <div class="chat-header d-flex flex-column align-items-center"
             style="border-radius: 10px; margin-bottom: 24px; text-align: center;">
            <img src="/favicon" class="d-block" width="90px" height="90px"
                 style="vertical-align: middle; margin-left: 10px; filter: drop-shadow(2px 2px 3px rgba(0,0,0,0.3));">
            <h1 style="margin: 0; font-size: 4rem; font-weight: bold;">
                <span class="text-shadow-md" style="color: #6366f1;">Prompter</span>
            </h1>
            <p class="text-white fs-5 text-shadow-lg" style="margin-top: 10px; font-size: 1.2rem;">
                <@spring.message "app.description"/>
            </p>
        </div>
        <div class="panel center mb-1">
            <a href="/chat" class="open_chat text-decoration-none rounded-pill"
               style="padding: 15px 30px; font-size: 1.1rem;">
                <i class="fas fa-comment-dots"></i>
                <@spring.message "start_chat"/>
            </a>
        </div>
    </div>
</div>

<#include "/parts/footer.ftl" />
