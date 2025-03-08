<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="/static/css/main.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.10.0/styles/default.min.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.10.0/highlight.min.js"
        integrity="sha512-6yoqbrcLAHDWAdQmiRlHG4+m0g/CT/V9AGyxabG8j7Jk8j3r3K6due7oqpiRMZqcYe9WM2gPcaNNxnl2ux+3tA=="
        crossorigin="anonymous" referrerpolicy="no-referrer"></script>
    <title>Prompter</title>
</head>

<body>
    <div class="center" style="height: 100vh;">
        <div class="block" style="max-width: 600px; padding: 20px;">
            <div class="chat-header" style="border-radius: 10px; margin-bottom: 30px;">
                <h1 style="margin: 0; font-size: 2.5rem;">
                    <span style="color: #6366f1;">Prompter</span>
                    <img src="/favicon.ico">
                    <#--  <span class="document-icon" style="vertical-align: middle; margin-left: 10px;"></span>  -->
                </h1>
                <p style="margin-top: 5px; color: #a0a6b9;">AI Assistant Platform</p>
            </div>

            <div class="panel center mb-1">
                <a href="/chat" class="button" style="padding: 15px 30px; font-size: 1.1rem;">
                    Rozpocznij czat
                </a>
            </div>

            <div class="alert-error" style="background: rgba(255, 221, 221, 0.1); border: 1px solid rgba(255, 0, 0, 0.2);">
                <h3 style="margin: 0 0 10px 0; color: #ff6b6b;">Uwaga!</h3>
                <p style="margin: 0; color: #ff9f9f;">
                    Subskrypcje są 3x droższe niż średnia rynkowa.<br>
                    Zalecamy użycie własnych kluczy API dla oszczędności.
                </p>
            </div>

            <div class="file-browser mt-1" style="background: rgba(45,45,49,0.8);">
                <div class="file-grid" style="grid-template-columns: repeat(3, 1fr);">
                    <div class="file-item">
                        <div class="icon-file">🔑</div>
                        <div class="file-name">OpenAI API</div>
                    </div>
                    <div class="file-item">
                        <div class="icon-file">🔧</div>
                        <div class="file-name">Anthropic</div>
                    </div>
                    <div class="file-item">
                        <div class="icon-file">⚡</div>
                        <div class="file-name">Dilman</div>
                    </div>
                </div>
            </div>

            <div class="mt-1" style="text-align: center; color: #a0a6b9; font-size: 0.9rem;">
                <p>Korzystając z własnego klucza API oszczędzasz do $120/miesięcznie</p>
            </div>
        </div>
    </div>
</body>

</html>