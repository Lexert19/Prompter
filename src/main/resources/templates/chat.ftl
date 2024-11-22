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
    <title>PromptEngineering</title>
</head>

<body>
    <div class="chat-main">
        <#include "/parts/navigation.ftl" />
        <div class="chat-container">
            <div class="chat-messages" id="chatMessages">
            </div>
            <div id="documents">
                <img width="24" height="24" src="/static/svg/document.svg">
            </div>
            <div class="chat-input">
                <div class="cache-container">
                </div>
                <textarea type="text" onkeydown="window.chat.appendText(event)" id="input"
                    placeholder="Wpisz wiadomość..."></textarea>
                <button onclick="window.chat.chat()">Wyślij</button>
            </div>
        </div>
    </div>
    <script src="/static/js/main.js"></script>
    <script src="/static/js/account.js"></script>
    <script src="/static/js/message.js"></script>
    <script src="/static/js/chatApi.js"></script>
    <script src="/static/js/claudeApi.js"></script>
    <script src="/static/js/chatGPTApi.js"></script>
    <script src="/static/js/prompter.js"></script>
    <script src="/static/js/navigation-view.js"></script>
    <script src="/static/js/llamaApi.js"></script>
    <script src="/static/js/chat.js"></script>
</body>

</html>