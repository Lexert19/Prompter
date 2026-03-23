<div class="chat-settings" id="chatSettings">
    <div class="instruction-field pe-4">
        <label class="checkbox-container">
            <input type="checkbox"
                   id="memory"
                   name="memory"
                   value="memory"
                   onchange="window.settings.change(event)">
            <label for="memory" class="custom-checkbox"></label>
            <span><@spring.message "settings.memory" /></span>
        </label>
    </div>
    <div class="instruction-field ">
        <label class="checkbox-container">
            <input type="checkbox"
                   id="cache"
                   name="cache"
                   value="cache"
                   onchange="window.settings.change(event)">
            <label for="cache" class="custom-checkbox"></label>
            <span><@spring.message "settings.cache" /></span>
        </label>
    </div>
    <#include "/parts/maxTokenView.ftl" />
    <div class="instruction-field ">
        <label for="temperature">
            <span><@spring.message "settings.temperature" /></span>
        </label>
        <input type="range"
               id="temperature"
               name="temperature"
               min="0"
               max="100"
               step="1"
               value="0"
               onchange="window.settings.change(event)">
    </div>
    <div class="instruction-field ">
        <label for="top_p">
            <span><@spring.message "settings.top_p" /></span>
        </label>
        <input type="range"
               id="top_p"
               name="top_p"
               min="0"
               max="1"
               step="0.01"
               value="0.95"
               onchange="window.settings.change(event)">
    </div>
    <div class="instruction-field">
        <label for="frequencyPenalty">
            <span><@spring.message "settings.frequency_penalty" /></span>
        </label>
        <input type="range"
               id="frequencyPenalty"
               name="frequencyPenalty"
               min="-2.0"
               max="2.0"
               step="0.01"
               value="0"
               onchange="window.settings.change(event)">
    </div>
    <div class="instruction-field">
        <label for="presencePenalty">
            <span><@spring.message "settings.presence_penalty" /></span>
        </label>
        <input type="range"
               id="presencePenalty"
               name="presencePenalty"
               min="-2.0"
               max="2.0"
               step="0.01"
               value="0"
               onchange="window.settings.change(event)">
    </div>
    <div id="systemPromptContainer"></div>
    <div id="projectSelectorContainer"></div>
    <div id="modelSelectorContainer"></div>
    <#include "/parts/instructionThinkingEffort.ftl" />
    <div class="instruction-field">
        <label class="checkbox-container">
            <input type="checkbox"
                   id="useSharedKeys"
                   name="useSharedKeys"
                   onchange="window.settings.change(event)">
            <span class="custom-checkbox"></span>
            <span><@spring.message "settings.use.shared.keys" /></span>
        </label>
    </div>
</div>
