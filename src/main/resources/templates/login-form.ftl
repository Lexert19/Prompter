<!DOCTYPE html>
<html lang="en">
<head>
     <#include "/parts/links.ftl" />

    <title>Login</title>

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
            margin-bottom: 0.5rem; /* Add margin between buttons */
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

        .login-container .alert-warning {
            background: rgba(255, 235, 205, 0.3);
            border: 1px solid rgba(255, 204, 0, 0.4);
            border-radius: 8px;
            padding: 15px;
            margin-top: 15px;
            text-align: center;
        }

        .login-container .alert-warning h3 {
            margin: 0 0 10px 0;
            color: #ffc107;
            font-weight: bold;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .login-container .alert-warning h3 i {
            margin-right: 10px;
            font-size: 1.5rem;
        }

        .login-container .alert-warning p {
            margin: 0;
            color: #ffb347;
        }

        /* Style for the Google Sign-In button */
        .google-login-button {
            background-color: #4285f4; /* Google blue */
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .google-login-button:hover {
            background-color: #357ae8; /* Darker Google blue */
        }

        .google-login-button img {
            height: 20px;
            margin-right: 10px;
        }

        .login-container .forgot-password {
                    text-align: center;
                    margin-top: 1rem;
                }

                .login-container .forgot-password a {
                    color: #e0e7ff;
                    text-decoration: none;
                }

                .login-container .forgot-password a:hover {
                    text-decoration: underline;
                }
    </style>
</head>
<body class="background">
<div class="mesh-background">
    <div class="cloud cloud-1"></div>
    <div class="cloud cloud-2"></div>
    <div class="cloud cloud-3"></div>
</div>
<#include "/parts/navbar.ftl" />

<div class="content-wrapper px-2">
    <div class="login-container">
        <h2>Login</h2>

        <#if error??>
        <div class="alert-error">
            ${error}
        </div>
    </#if>

    <form action="/auth/login" method="POST">
        <input type="text" name="username" placeholder="Username" required>
        <input type="password" name="password" placeholder="Password" required>
        <button type="submit">Login</button>
    </form>

    <button class="google-login-button" onclick="window.location.href='/oauth2/authorization/google'">
        <img src="https://img.icons8.com/color/20/000000/google-logo.png" alt="Google Logo">
        Sign in with Google
    </button>

    <div class="forgot-password">
        <a href="/auth/reset-password-request">Forgot your password?</a>
    </div>
</div>
</div>
</body>
</html>