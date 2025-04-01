<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
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

        .info-container {
            background-color: rgba(45, 45, 49, 0.9);
            padding: 2rem;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
            width: 100%;
            max-width: 500px;
        }

        .alert-error {
            background: rgba(255, 0, 0, 0.1);
            border: 1px solid rgba(255, 0, 0, 0.3);
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 20px;
        }

        .alert-error p {
            margin: 0;
            color: #ff4444;
        }

        .form-group {
            margin-bottom: 1.5rem;
        }

        label {
            display: block;
            margin-bottom: 0.5rem;
            color: #e0e7ff;
        }

        input[type="password"] {
            width: 100%;
            padding: 0.75rem;
            border-radius: 6px;
            border: 1px solid #4a4a4e;
            background-color: #333337;
            color: white;
            font-size: 1rem;
        }

        button {
            width: 100%;
            padding: 1rem;
            background-color: rgba(99, 102, 241, 0.9);
            color: white;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-size: 1rem;
            transition: background-color 0.2s;
        }

        button:hover {
            background-color: rgba(79, 70, 229, 0.9);
        }

        .back-to-login {
            margin-top: 1.5rem;
            text-align: center;
        }

        .back-to-login a {
            color: #e0e7ff;
            text-decoration: none;
            transition: color 0.2s;
        }

        .back-to-login a:hover {
            color: rgba(99, 102, 241, 0.9);
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="info-container">
        <#if error??>
            <div class="alert-error">
                <p>${error}</p>
            </div>
        </#if>

        <form method="post" action="/auth/reset-password-confirm">
            <input type="hidden" name="token" value="${token}"/>

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
</body>
</html>