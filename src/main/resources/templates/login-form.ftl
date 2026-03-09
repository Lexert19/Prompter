<#import "/spring.ftl" as spring/>
<!DOCTYPE html>
<html lang="en">
<head>
     <#include "/parts/links.ftl" />

    <title><@spring.message "login.title"/></title>
</head>
<body class="background single-page-body">
<div class="mesh-background">
    <div class="cloud cloud-1"></div>
    <div class="cloud cloud-2"></div>
    <div class="cloud cloud-3"></div>
</div>
<#include "/parts/navbar.ftl" />

<div class="single-page-container">
<div class="content-wrapper  px-2">
    <div class="login-container">
        <h2>Login</h2>

        <#if error??>
        <div class="alert-error">
            ${error}
        </div>
        </#if>

    <form action="/auth/login" method="POST">
        <input type="text" name="username" placeholder="<@spring.message 'login.username.placeholder'/>" required>
        <input type="password" name="password" placeholder="<@spring.message 'login.password.placeholder'/>" required>
        <button type="submit"><@spring.message "login.button"/></button>
    </form>

    <h6><@spring.message "login.register.heading"/></h6>
    <button id="google-login-btn" class="google-login-button">
        <img src="https://img.icons8.com/color/20/000000/google-logo.png" alt="Google Logo">
        <@spring.message "login.google.button"/>
    </button>

    <div id="termsModal" class="modal">
        <div class="modal-content">
            <h3><@spring.message "modal.heading"/></h3>
            <p><@spring.message "modal.intro"/>
                <a href="/terms" target="_blank" style="color:#99f;"><@spring.message "terms.label"/></a>   <@spring.message "modal.and"/>
                <a href="/privacy" target="_blank" style="color:#99f;"><@spring.message "privacy.label"/></a>.
            </p>
            <label>
                <span class="w-100"><@spring.message "modal.accept"/></span>
                <input class="w-auto" type="checkbox" id="acceptTerms">

            </label>
            <div>
                <button id="acceptAndContinue" disabled><@spring.message "modal.continue"/></button>
                <button onclick="closeModal()"><@spring.message "modal.cancel"/></button>
            </div>
        </div>
    </div>

    <div class="forgot-password">
        <a href="/auth/reset-password-request"><@spring.message "login.forgot"/></a>
    </div>
</div>
</div>
<script src="/static/js/auth.js"></script>
</body>
</html>