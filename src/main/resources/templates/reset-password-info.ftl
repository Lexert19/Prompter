<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Password Reset Requested</title>
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
            text-align: center;
        }

        .alert-warning {
            background: rgba(255, 235, 205, 0.3);
            border: 1px solid rgba(255, 204, 0, 0.4);
            border-radius: 8px;
            padding: 15px;
            margin: 15px 0;
        }

        .alert-warning h3 {
            margin: 0 0 10px 0;
            color: #ffc107;
            font-weight: bold;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .alert-warning h3 i {
            margin-right: 10px;
            font-size: 1.5rem;
        }

        .alert-warning p {
            margin: 0;
            color: #ffb347;
            line-height: 1.5;
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
        <div class="alert-warning">
            <h3>
                <i class="fas fa-envelope"></i>
                Check Your Email
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

    <!-- Font Awesome for icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
</body>
</html>