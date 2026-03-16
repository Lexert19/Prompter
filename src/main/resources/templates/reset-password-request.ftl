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
        <div class="content-wrapper px-2">
            <div class="login-container p-4 panel">
                <h2>Reset Password</h2>
                <#if error??>
                <div class="alert-error">${error}</div>
                </#if>
                <form action="/auth/reset-password-request" method="post">
                    <input type="email" name="email" placeholder="Email address" required>
                    <button type="submit">Request Password Reset</button>
                </form>
                <#if token??>
                <div class="alert-warning">
                    <h3>
                        <i class="fas fa-exclamation-triangle"></i>Password Reset Request Sent
                    </h3>
                    <p>
                        We have sent a password reset link to your email address. Please check your inbox and follow the instructions to reset your password.
                    </p>
                </div>
                </#if>
                <div class="back-to-login">
                    <a href="/auth/login">Back to login</a>
                </div>
            </div>
        </div>
    </body>
</html>
