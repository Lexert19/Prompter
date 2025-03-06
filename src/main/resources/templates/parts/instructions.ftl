<div class="chat-settings" id="chatSettings">
    <div>
        <input type="checkbox" id="memory" name="memory" value="memory"
            onclick="window.settings.change(event)">
        <label for="memory">Pamięć (pamięta poprzednie wiadomości)</label>
    </div>
    <div>
        <input type="checkbox" id="cache" name="cache" value="cache"
            onclick="window.settings.change(event)">
        <label for="cache">Cache</label>
    </div>
    <div>
        <input type="number" name="maxTokens" value="8000" onchange="window.settings.change(event)">
    </div>
    <div class="d-flex">
        <input type="range" id="temperature" name="temperature" min="0" max="100" step="1" value="0"
            oninput="window.settings.change(event)">
        <label class="center" for="temperature"><span>Temperatura</span></label>
    </div>
    <div>
        <label for="system">System</label>
        <textarea id="system" name="system"  onchange="window.settings.change(event)"></textarea>
        <input type="checkbox" id="systemSwitch" name="systemSwitch">
    </div>
    <div>
        <select id="modelOptions" name="model" class="mb-1" onclick="window.settings.changeModel(event)">
        </select>
    </div>
</div>