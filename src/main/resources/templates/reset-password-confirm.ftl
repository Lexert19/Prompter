<!DOCTYPE html>
<html lang="en">
<head>
    <#include "/parts/links.ftl" />
    <title>Reset Password</title>
</head>
<body class="background">
<div class="mesh-background">
    <div class="cloud cloud-1"></div>
    <div class="cloud cloud-2"></div>
    <div class="cloud cloud-3"></div>
</div>
<#include "/parts/navbar.ftl" />
<div class="content-wrapper single-page-container px-2">
    <div class="login-container p-4 panel">
        <#if error??>
        <div class="alert-error">
            <p>${error}</p>
        </div>
    </#if>
    <form method="post" action="/auth/reset-password-confirm">
        <input type="hidden" name="token" value="${token}" />
        <div class="form-group">
            <label for="password">New Password</label>
            <input type="password" id="password" name="password" required>
        </div>
        <div class="form-group">
            <label for="password_confirmation">Confirm Password</label>
            <input type="password" id="password_confirmation" name="password_confirmation" required>
        </div>
        <button type="submit">Reset Password</button>
    </form>
    <div class="back-to-login">
        <a href="/auth/login">← Return to Login Page</a>
    </div>
</div>
</div>
</body>
</html>