<div class="chat-settings" id="chatSettings">
    <div>
        <input type="checkbox" id="memory" name="memory" value="memory"
            onclick="window.chat.changeSettings(event)">
        <label for="memory">Pamięć (pamięta poprzednie wiadomości)</label>
    </div>
    <div>
        <input type="checkbox" id="cache" name="cache" value="cache"
            onclick="window.chat.changeSettings(event)">
        <label for="cache">Cache</label>
    </div>
    <div class="d-flex">
        <input type="range" id="temperature" name="temperature" min="0" max="100" step="1" value="0"
            oninput="window.chat.changeSettings(event)">
        <label class="center" for="temperature"><span>Temperatura</span></label>
    </div>
    <div>
        <select id="modelOptions" name="model" class="mb-1" onclick="window.chat.changeModel(event)">
        </select>
    </div>
</div>