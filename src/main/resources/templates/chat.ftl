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
                <button class="send-button" onclick="window.chat.chat()">
                    <div class="center">
                        <svg xmlns="http://www.w3.org/2000/svg" height="16" width="12" viewBox="0 0 384 512">
                            <!--!Font Awesome Free 6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2025 Fonticons, Inc.-->
                            <path fill="#ffffff" d="M214.6 41.4c-12.5-12.5-32.8-12.5-45.3 0l-160 160c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0L160 141.2 160 448c0 17.7 14.3 32 32 32s32-14.3 32-32l0-306.7L329.4 246.6c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3l-160-160z" />
                        </svg>
                    </div>
                </button>
            </div>
        </div>
    </div>
    <#include "/parts/editMessage.ftl" />
    <script src="/static/js/settings.js"></script>
    <script src="/static/js/htmlParser.js"></script>
    <script src="/static/js/editMessageView.js"></script>
    <script src="/static/js/main.js"></script>
    <script src="/static/js/account.js"></script>
    <script src="/static/js/message.js"></script>
    <script src="/static/js/chatApi.js"></script>
    <script src="/static/js/claudeApi.js"></script>
    <script src="/static/js/chatGPTApi.js"></script>
    <script src="/static/js/navigation-view.js"></script>
    <script src="/static/js/llamaApi.js"></script>
    <script src="/static/js/chat.js"></script>
</body>

</html>