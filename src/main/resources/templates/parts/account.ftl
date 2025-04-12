<div id="accountPanel" tabindex="0" class="account-panel">
    <div class="mb-1">
        <label class="block" for="keyName">Nazwa klucza</label>
        <input type="text" class="mb-05" id="keyName" name="keyName">
        <label class="block" for="keyValue">Wartość klucza</label>
        <input type="text" class="mb-05" id="keyValue" name="keyValue">
        <button onclick="saveKey()">Zapisz</button>
    </div>

    <div class="mb-1">
        <label class="checkbox-container">
            <input type="checkbox" id="chatHistoryInput" name="chatHistoryInput" value="chatHistoryInput" onchange="window.settings.change(event)">
            <label for="chatHistoryInput" class="custom-checkbox"></label>
            <span>Zapisuj do historii</span>
        </label>
    </div>

    <div>
        <label>Nowe hasło</label>
        <input class="block mb-05" type="password" id="newPassword">
        <label>Powtórz hasło</label>
        <input class="block mb-05" type="password" id="confirmPassword">
        <button onclick="setNewPassword()">Ustaw</button>
    </div>
</div>