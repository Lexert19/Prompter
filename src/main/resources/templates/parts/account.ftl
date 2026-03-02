<div id="accountPanel" tabindex="0" class="account-panel">
    <div class="mb-2">
        <button class="rounded-1" onclick="window.accountView.openAddKeyModal()">Dodaj klucz API</button>
    </div>

    <div class="mb-2">
        <button class="rounded-1" onclick="window.accountView.openChangePasswordModal()">Zmień hasło</button>
    </div>

    <div class="mb-1">
        <label class="checkbox-container">
            <input type="checkbox" id="chatHistoryInput" name="chatHistoryInput" value="chatHistoryInput" onchange="window.settings.change(event)">
            <span class="custom-checkbox"></span>
            <span>Zapisuj do historii</span>
        </label>
    </div>
</div>