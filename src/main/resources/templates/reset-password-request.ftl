<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Reset Password Request</title>
    <link rel="stylesheet" href="/static/css/style.css">
</head>
<body>
    <div class="chat-container center">
        <div class="pages">
            <div id="reset-password-request" class="page active">
                <div class="chat-header">
                    <h1>Reset Password</h1>
                </div>
                <div class="chat-messages">
                    <div class="message assistant">
                        <p>Enter your email address to request a password reset.</p>
                        <#if error??>
                            <div class="alert-error">${error}</div>
                        </#if>
                        <form action="/auth/reset-password-request" method="post">
                            <div class="mt-1">
                                <input type="email" id="email" name="email" placeholder="Your email address" required>
                            </div>
                            <div class="mt-1">
                                <button type="submit">Request Password Reset</button>
                            </div>
                        </form>
                        <#if token??>
                            <div class="alert-warning mt-1">
                                <h3><i class="fas fa-exclamation-triangle"></i>Password Reset Request Sent</h3>
                                <p>
                                    We have sent a password reset link to your email address.
                                    Please check your inbox and follow the instructions to reset your password.
                                </p>
                                <p>Token: <strong>${token}</strong> (for testing purposes, in production this should not be displayed)</p>
                            </div>
                        </#if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>