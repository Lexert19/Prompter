<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title Reset password</title>
</head>
<body>
    <h1>Reset Password</h1>
    <#if error??>
        <div id="error">${error}</div>
    </#if>
    <form action="/auth/reset-password-confirm">
        <input type="hidden" name="token" value="${token}">
        <label for="newPassword">New Password:</label>
        <input type="password" id="newPassword" name="newPassword" required>
        <button type="submit">Reset Password</button>
    </form>
</body>
</html>