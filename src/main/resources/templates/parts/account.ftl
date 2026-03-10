<div id="accountPanel" tabindex="0" class="account-panel">
    <div class="mb-2">
        <label for="languageSelect" class="d-block mb-025"><@spring.message "account.language"/></label>
        <select id="languageSelect" class="rounded-1 w-100" onchange="window.accountView.changeLanguage(this.value)">
            <option value="pl">Polski</option>
            <option value="en">English</option>
        </select>
    </div>


    <div class="mb-2">
        <button class="rounded-1" onclick="window.accountView.openAddKeyModal()"><@spring.message "account.add.api.key"/></button>
    </div>

    <div class="mb-2">
        <button class="rounded-1" onclick="window.accountView.openChangePasswordModal()"><@spring.message "account.change.password"/></button>
    </div>

    <div class="mb-2">
        <button class="rounded-1" onclick="window.accountView.openAddSharedKeyModal()"><@spring.message "addSharedApiKey"/></button>
    </div>

    <div class="mb-1">
        <label class="checkbox-container">
            <input type="checkbox" id="chatHistoryInput" name="chatHistoryInput" value="chatHistoryInput" onchange="window.settings.change(event)">
            <span class="custom-checkbox"></span>
            <span><@spring.message "account.save.history"/></span>
        </label>
    </div>
</div>