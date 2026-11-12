<#import "/spring.ftl" as spring />


<div id="accountPanel" tabindex="0" class="account-panel">
    <div class="mb-2">
        <label for="languageSelect" class="d-block mb-025">
            <@spring.message "account.language" />
        </label>
        <select id="languageSelect"
                class="rounded-1 w-100"
                onchange="AccountView.instance().changeLanguage(this.value)">
            <option value="pl">Polski</option>
            <option value="en">English</option>
        </select>
    </div>
    <div class="mb-2">
        <label><@spring.message "account.points" />: <span id="userPoints">0</span></label>
    </div>
    <div class="mb-2">
        <button class="rounded-1 w-100 d-flex align-items-center justify-content-between indigo-button" onclick="AccountView.instance().openAddKeyModal()">
            <@spring.message "account.add.api.key" />
        </button>
    </div>
    <div class="mb-2">
        <button class="rounded-1  w-100 d-flex align-items-center justify-content-between indigo-button"
                onclick="AccountView.instance().openChangePasswordModal()">
            <@spring.message "account.change.password" />
        </button>
    </div>
    <div class="mb-2">
        <button class="rounded-1  w-100 d-flex align-items-center justify-content-between indigo-button"
                onclick="AccountView.instance().openAddSharedKeyModal()">
            <@spring.message "addSharedApiKey" />
        </button>
    </div>

    <div class="mt-2 pt-2 border-top">
        <button class="rounded-1 w-100 d-flex align-items-center justify-content-between indigo-button"
                onclick="TwoFactorManager.instance().openModal()">
            <@spring.message "account.twofactor" />
        </button>
    </div>
    <div class="mt-3 pt-3 border-top">
        <button class="rounded-1 w-100 btn-danger" onclick="AccountView.instance().logout()">
            <i class="fas fa-sign-out-alt"></i> <@spring.message "account.logout" />
        </button>
    </div>
</div>
