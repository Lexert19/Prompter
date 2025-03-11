
<div id="accountPanel" tabindex="0" class="account-panel">
    <div class="mb-1">
        <label class="block" for="keyName">Nazwa klucza</label>
        <input type="text" id="keyName" name="keyName">
        <label class="block" for="keyValue">Wartość klucza</label>
        <input type="text" id="keyValue" name="keyValue">
        <button onclick="saveKey()">Zapisz</button>
    </div>

    <div>
        <label class="checkbox-container">
            <input type="checkbox" id="chatHistoryInput" name="chatHistoryInput" value="chatHistoryInput" onchange="window.settings.change(event)">
            <label for="chatHistoryInput" class="custom-checkbox"></label>
            <span>Zapisuj do historii</span>
        </label>
    </div>
</div>