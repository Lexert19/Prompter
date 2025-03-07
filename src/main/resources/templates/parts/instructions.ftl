<div class="chat-settings" id="chatSettings">
    <div>
        <label class="checkbox-container">
            <input type="checkbox" id="memory" name="memory" value="memory" onchange="window.settings.change(event)">
            <label for="memory" class="custom-checkbox"></label>
            <span>Pamięć (pamięta poprzednie wiadomości)</span>
        </label>
    </div>
    <div>
        <label class="checkbox-container">
            <input type="checkbox" id="cache" name="cache" value="cache" onchange="window.settings.change(event)">
            <label for="cache" class="custom-checkbox"></label>
            <span>Cache</span>
        </label>
    </div>
    <div>
        <input type="number" name="maxTokens" value="8000" onchange="window.settings.change(event)">
    </div>
    <div class="d-flex">
        <input type="range" id="temperature" name="temperature" min="0" max="100" step="1" value="0"
            oninput="window.settings.change(event)">
        <label class="center" for="temperature"><span>Temperatura xxx</span></label>
    </div>
    <div>
        <label for="system">System</label>
        <div class="d-flex">
            <textarea id="system" name="system" onchange="window.settings.change(event)"></textarea>
            <label class="checkbox-container">
                <input type="checkbox" id="systemSwitch" name="systemSwitch" onchange="window.settings.change(event)">
                <label for="systemSwitch" class="custom-checkbox"></label>
            </label>
        </div>
    </div>
    <div>
        <select id="modelOptions" name="model" class="mb-1" onclick="window.settings.changeModel(event)">
        </select>
    </div>
</div>