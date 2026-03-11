<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "/parts/links.ftl" />
        <title>Reset Password</title>
    </head>
    <body class="background">
        <#include "/parts/navbar.ftl" />
        <div class="login-container panel">
            <h2>Reset Password</h2>
            <#if error??>
            <div class="alert-error">${error}</div>
            </#if>
            <#if message??>
            <div class="alert-success">${message}</div>
            </#if>
            <form action="/auth/reset-password-request" method="POST">
                <input type="email" name="email" placeholder="Email address" required>
                <button type="submit">Send reset link</button>
            </form>
            <div class="back-to-login">
                <a href="/auth/login">Back to login</a>
            </div>
        </div>
    </body>
</html>
