<!DOCTYPE html>
<html lang="en">
<head>
    <#include "/parts/links.ftl" />
    <title>Password Reset Successful</title>
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
        <div class="alert-success">
            <h3>
                <i class="fas fa-check-circle"></i>
                Password Reset Successful
            </h3>
            <p>Your password has been successfully updated. You can now log in with your new password.</p>
        </div>
        <div class="back-to-login">
            <a href="/auth/login">Continue to Login Page</a>
        </div>
    </div>
</div>
</body>
</html>