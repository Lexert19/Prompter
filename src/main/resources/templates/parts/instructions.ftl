<div class="chat-settings" id="chatSettings">
    <div class="instruction-field">
        <label class="checkbox-container">
            <input type="checkbox"
                   id="memory"
                   name="memory"
                   value="memory"
                   onchange="Settings.instance().change(event)">
            <label for="memory" class="custom-checkbox"></label>
            <span><@spring.message "settings.memory" /></span>
        </label>
    </div>
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
               onchange="Settings.instance().change(event)">
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
                   onchange="Settings.instance().change(event)">
            <span class="custom-checkbox"></span>
            <span><@spring.message "settings.use.shared.keys" /></span>
        </label>
    </div>
    <button class="node-selector-button rounded-1 d-flex align-items-center justify-content-between w-100" onclick="HostedNodesView.instance().openRegisterNodeModal()">
        <i class="fas fa-plus"></i><@spring.message "settings.add_model_button" />
    </button>
    <button class="node-selector-button my-3 rounded-1 d-flex align-items-center justify-content-between w-100" onclick="HostedNodesView.instance().openNodesListModal()">
        <@spring.message "settings.show_nodes_button" />
    </button>
    <button class="node-selector-button my-3 rounded-1 d-flex align-items-center justify-content-between w-100" onclick="ToolsView.instance().openModal()">
        <i class="fas fa-wrench"></i>
        <@spring.message "settings.tools_button" />
    </button>
    <div id="openRouterSettingsContainer">
        <div class="instruction-field">
            <label for="openRouterProviderOrder">
                <@spring.message "settings.openRouterProviderOrder.label" />
            </label>
            <input type="text" id="openRouterProviderOrder" name="openRouterProviderOrder"
                   class="form-control"
                   data-i18n-placeholder="settings.openRouterProviderOrder.placeholder"
                   placeholder="<@spring.message 'settings.openRouterProviderOrder.placeholder' />">
        </div>
        <div class="instruction-field checkbox-container">
            <label class="checkbox-container">
                <input type="checkbox" id="openRouterAllowFallbacks" name="openRouterAllowFallbacks">
                <label for="openRouterAllowFallbacks" class="custom-checkbox"></label>
                <span><@spring.message "settings.openRouterAllowFallbacks.label" /></span>
            </label>
        </div>
        <div id="advancedOptionsContainer"></div>
    </div>
</div>
