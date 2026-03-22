<!DOCTYPE html>
<html lang="en">
<head>
    <#include "/parts/links.ftl" />
    <title>Password Reset Requested</title>
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
        <div class="alert-warning">
            <h3>
                <i class="fas fa-envelope"></i> Check Your Email
            </h3>
            <p>
                If an account with the provided email exists, we've sent a password reset link.
                Please check your inbox and follow the instructions to reset your password.
            </p>
        </div>
        <div class="back-to-login">
            <a href="/auth/login">← Return to Login Page</a>
        </div>
    </div>
</div>
</body>
</html>