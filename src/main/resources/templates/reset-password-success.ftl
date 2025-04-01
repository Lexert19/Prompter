<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Password Reset Successful</title>
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

.alert-success {
background: rgba(40, 167, 69, 0.2);
border: 1px solid rgba(40, 167, 69, 0.4);
border-radius: 8px;
padding: 15px;
margin: 15px 0;
}

.alert-success h3 {
margin: 0 0 10px 0;
color: #28a745;
font-weight: bold;
display: flex;
align-items: center;
justify-content: center;
}

.alert-success h3 i {
margin-right: 10px;
font-size: 1.5rem;
}

.alert-success p {
margin: 0;
color: #7be495;
line-height: 1.5;
}

.back-to-login {
margin-top: 1.5rem;
}

.back-to-login a {
display: inline-block;
padding: 0.75rem 1.5rem;
background-color: rgba(99, 102, 241, 0.9);
color: white;
text-decoration: none;
border-radius: 6px;
transition: background-color 0.2s;
}

.back-to-login a:hover {
background-color: rgba(79, 70, 229, 0.9);
text-decoration: none;
}
</style>
</head>
<body>
<div class="info-container">
        <div class="alert-success">
            <h3>
                <i class="fas fa-check-circle"></i>
                Password Reset Successful
            </h3>
            <p>
                Your password has been successfully updated. You can now log in with your new password.
            </p>
        </div>

        <div class="back-to-login">
            <a href="/auth/login">Continue to Login Page</a>
        </div>
    </div>

    <!-- Font Awesome for icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
</body>
</html>