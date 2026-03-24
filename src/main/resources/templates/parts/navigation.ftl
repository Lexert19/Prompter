<div class="chat-navigation" id="ChatNavigation">
    <div class="side-panel">
        <button class="close-nav-btn" id="closeNavBtn" aria-label="Zamknij nawigację">
            <i class="fas fa-times"></i>
        </button>
        <button id="newChatBtn" class="new-chat-btn" aria-label="Nowy czat">
            <i class="fas fa-plus"></i>
        </button>
    </div>

    <div class="pages" id="pages">
        <#include "/parts/history.ftl" />
        <#include "/parts/account.ftl" />
        <#include "/parts/instructions.ftl" />
        <#include "/parts/projects.ftl" />
        <#include "/parts/models.ftl" />
    </div>
    <#include "/parts/panel.ftl" />
</div>
