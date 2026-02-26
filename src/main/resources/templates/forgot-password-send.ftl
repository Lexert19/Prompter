<!DOCTYPE html>
<html lang="en">
<head>
    <#include "/parts/links.ftl" />
    <title>Reset Password</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            color: white;
            background-color: #262629;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }

        .login-container {
            background-color: rgba(45, 45, 49, 0.9);
            padding: 2rem;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
            width: 100%;
            max-width: 400px;
        }

        .login-container h2 {
            text-align: center;
            margin-bottom: 1.5rem;
            color: #e0e7ff;
        }

        .login-container input {
            width: 100%;
            padding: 10px;
            margin-bottom: 1rem;
            border: none;
            border-radius: 5px;
            background-color: #374151;
            color: white;
            font-size: 16px;
            box-sizing: border-box;
        }

        .login-container input:focus {
            outline: none;
            border: 1px solid rgba(99, 102, 241, 0.9);
        }

        .login-container button {
            width: 100%;
            padding: 10px;
            border: none;
            border-radius: 5px;
            background-color: rgba(99, 102, 241, 0.9);
            color: white;
            font-size: 16px;
            cursor: pointer;
            transition: background-color 0.2s;
            margin-bottom: 0.5rem;
        }

        .login-container button:hover {
            background-color: rgba(99, 102, 241, 0.7);
        }

        .login-container .alert-error {
            background: #ffdddd;
            color: #cc0000;
            padding: 10px;
            margin-bottom: 1rem;
            border-radius: 5px;
            text-align: center;
        }

        .login-container .alert-success {
            background: #ddffdd;
            color: #006600;
            padding: 10px;
            margin-bottom: 1rem;
            border-radius: 5px;
            text-align: center;
        }

        .login-container .back-to-login {
            text-align: center;
            margin-top: 1rem;
        }

        .login-container .back-to-login a {
            color: #e0e7ff;
            text-decoration: none;
        }

        .login-container .back-to-login a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body class="background">
<#include "/parts/navbar.ftl" />

<div class="login-container">
    <h2>Reset Password</h2>

    <#if error??>
    <div class="alert-error">
        ${error}
    </div>
</#if>

<#if message??>
<div class="alert-success">
    ${message}
</div>
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