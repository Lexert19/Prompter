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
                <h2>Reset Password</h2>
                <div id="alert" style="display:none"></div>
                <form id="resetRequestForm">
                    <input type="email" name="email" placeholder="Email address" required>
                    <button type="submit" id="submitBtn">Request Password Reset</button>
                </form>

                <div class="back-to-login">
                    <a href="/auth/login">Back to login</a>
                </div>
            </div>
        </div>    <script src="/static/otherJs/reset-request.js" defer></script>


    </body>
</html>
