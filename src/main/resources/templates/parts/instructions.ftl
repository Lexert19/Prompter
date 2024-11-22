<div class="chat-settings" id="chatSettings">
    <div>
        <input type="checkbox" id="memory" name="memory" value="memory"
            onclick="window.chat.changeSettings(event)">
        <label for="memory">Pamięć (pamięta poprzednie wiadomości)</label>
    </div>
    <div>
        <input type="checkbox" id="longDocument" name="longDocument" value="longDocument"
            onclick="window.chat.changeSettings(event)">
        <label for="longDocument">Przytocz odpowiednie fragmenty</label>
    </div>
    <div>
        <input type="checkbox" id="programmer" name="programmer" value="programmer"
            onclick="window.chat.changeSettings(event)">
        <label for="programmer">Programista</label>
    </div>
    <div>
        <input type="checkbox" id="manySolutions" name="manySolutions" value="manySolutions"
            onclick="window.chat.changeSettings(event)">
        <label for="manySolutions">Wiele możliwości</label>
    </div>
    <div>
        <input type="checkbox" id="chainOfThoughts" name="chainOfThoughts" value="chainOfThoughts"
            onclick="window.chat.changeSettings(event)">
        <label for="chainOfThoughts">Chain of thoughts</label>
    </div>
    <div>
        <input type="checkbox" id="cache" name="cache" value="cache"
            onclick="window.chat.changeSettings(event)">
        <label for="cache">Cache</label>
    </div>
    <div>
        <input type="range" id="temperature" name="temperature" min="0" max="100" step="1" value="0"
            oninput="window.chat.changeSettings(event)">
        <label for="temperature">Temperatura</label>
    </div>
    <div>
        <select id="modelOptions" name="model" onclick="window.chat.changeModel(event)">
        </select>
    </div>
</div>