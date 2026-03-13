<div class="chat-navigation" id="ChatNavigation">
    <button class="close-nav-btn" id="closeNavBtn" aria-label="Zamknij nawigację">
        <i class="fas fa-times"></i>
    </button>
    <div class="pages" id="pages">
        <#include "/parts/history.ftl" />
        <#include "/parts/account.ftl" />
        <#include "/parts/instructions.ftl" />
        <#include "/parts/projects.ftl" />
        <#include "/parts/models.ftl" />
    </div>
    <#include "/parts/panel.ftl" />
</div>
