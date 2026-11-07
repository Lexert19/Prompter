<#import "/spring.ftl" as spring />

<div class="chat-prompts" id="promptsPanel">
    <div class="prompts-toolbar d-flex justify-content-between align-items-center mb-2">
        <span class="prompts-title">
            <@spring.message "prompts.title" />
        </span>
        <button id="refreshPromptsBtn"
                class="rounded-1 btn-sm"
                title="<@spring.message 'prompts.refresh' />"
                onclick="PromptsPanelView.instance().refresh()">
            <i class="fas fa-sync-alt"></i>
        </button>
    </div>
    <div class="prompts-list" id="promptsList">
        <p class="text-secondary">
            <@spring.message "prompts.loading" />
        </p>
    </div>
</div>