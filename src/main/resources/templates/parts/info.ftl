<#import "/spring.ftl" as spring />

<div class="info-panel" id="InfoPanel">
  <div class="side-panel">
    <button id="closeInfoBtn" class="close-info-btn" aria-label="Close info panel">
      <i class="fas fa-times"></i>
    </button>
  </div>

  <div id="request-stats"></div>

  <hr class="info-separator" />

  <div class="info-prompts">
    <div class="info-prompts-toolbar">
      <span class="info-prompts-title">
        <@spring.message "prompts.title" />
      </span>
      <button class="rounded-1 btn-sm"
              title="<@spring.message 'prompts.refresh' />"
              onclick="InfoPanel.getInstance().loadPrompts(true)">
        <i class="fas fa-sync-alt"></i>
      </button>
    </div>
    <div class="info-prompts-list" id="infoPromptsList">
      <p class="text-secondary"><@spring.message "prompts.loading" /></p>
    </div>
  </div>
</div>