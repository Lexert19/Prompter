<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "/parts/links.ftl" />
        <title>Reset Password</title>
    </head>
    <body class="background">
        <#include "/parts/navbar.ftl" />
        <div class="login-container p-4 panel">
            <h2>Set New Password</h2>
            <#if error??>
            <div class="alert-error">${error}</div>
            </#if>
            <#if message??>
            <div class="alert-success">${message}</div>
            </#if>
            <form action="/auth/reset-password-confirm" method="POST">
                <input type="hidden" name="token" value="${token}">
                <input type="password"
                       name="newPassword"
                       placeholder="New password"
                       required>
                <button type="submit">Reset Password</button>
            </form>
            <div class="back-to-login">
                <a href="/auth/login">Back to login</a>
            </div>
        </div>
    </body>
</html>
