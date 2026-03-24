<#import "/spring.ftl" as spring />


<div id="accountPanel" tabindex="0" class="account-panel">
    <div class="mb-2">
        <label for="languageSelect" class="d-block mb-025">
            <@spring.message "account.language" />
        </label>
        <select id="languageSelect"
                class="rounded-1 w-100"
                onchange="window.accountView.changeLanguage(this.value)">
            <option value="pl">Polski</option>
            <option value="en">English</option>
        </select>
    </div>
    <div class="mb-2">
        <label><@spring.message "account.points" />: <span id="userPoints">0</span></label>
    </div>
    <div class="mb-2">
        <button class="rounded-1" onclick="window.accountView.openAddKeyModal()">
            <@spring.message "account.add.api.key" />
        </button>
    </div>
    <div class="mb-2">
        <button class="rounded-1"
                onclick="window.accountView.openChangePasswordModal()">
            <@spring.message "account.change.password" />
        </button>
    </div>
    <div class="mb-2">
        <button class="rounded-1"
                onclick="window.accountView.openAddSharedKeyModal()">
            <@spring.message "addSharedApiKey" />
        </button>
    </div>
    <div class="mb-1">
        <label class="checkbox-container">
            <input type="checkbox"
                   id="chatHistoryInput"
                   name="chatHistoryInput"
                   value="chatHistoryInput"
                   onchange="window.settings.change(event)">
            <span class="custom-checkbox"></span>
            <span><@spring.message "account.save.history" /></span>
        </label>
    </div>

    <div class="mt-2 pt-2 border-top">
        <div class="mb-2 fw-bold"><@spring.message "account.twofactor" /></div>

        <div id="twofaStatus">
        </div>

        <div id="twofaSetupForm">
            <div class="mb-2">
                <label for="twofaEmail" class="d-block mb-025">
                    <@spring.message "account.twofactor.email" />
                </label>
                <input type="email" id="twofaEmail" class="rounded-1 w-100"
                       placeholder="<@spring.message 'account.twofactor.email.placeholder' />">
            </div>
            <div class="mb-2">
                <button class="rounded-1 btn-sm" id="twofaSendCodeBtn">
                    <@spring.message "account.twofactor.sendCode" />
                </button>
            </div>
            <div class="mb-2">
                <label for="twofaCode" class="d-block mb-025">
                    <@spring.message "account.twofactor.code" />
                </label>
                <input type="text" id="twofaCode" class="rounded-1 w-100" maxlength="6">
            </div>
            <div class="mb-2">
                <button class="rounded-1 btn-primary" id="twofaEnableBtn">
                    <@spring.message "account.twofactor.enable" />
                </button>
                <button class="rounded-1 btn-danger" id="twofaDisableBtn" style="display: none;">
                    <@spring.message "account.twofactor.disable" />
                </button>
            </div>
            <div class="mt-1 text-danger" id="twofaErrorMsg"></div>
            <div class="mt-1 text-success" id="twofaSuccessMsg"></div>
        </div>
    </div>
</div>
