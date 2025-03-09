<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="/static/css/main.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" integrity="..." crossorigin="anonymous" referrerpolicy="no-referrer" />
    <title>Prompter</title>
</head>

<body>
    <div class="chat-main">
        <#include "/parts/navigation.ftl" />
        <div class="chat-container">
            <div class="chat-messages" id="chatMessages">
            </div>
            <div id="documents">
            </div>
            <div class="chat-input">
                <div class="cache-container">
                </div>
                <textarea type="text" onkeydown="window.chat.appendText(event)" id="input"
                    placeholder="Wpisz wiadomość..."></textarea>
                <button id="send-button" class="send-button" onclick="window.chat.chat()">
                    <div id="send-icon" class="center">
                        <svg xmlns="http://www.w3.org/2000/svg" height="16" width="12" viewBox="0 0 384 512">
                            <path fill="#ffffff" d="M214.6 41.4c-12.5-12.5-32.8-12.5-45.3 0l-160 160c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0L160 141.2 160 448c0 17.7 14.3 32 32 32s32-14.3 32-32l0-306.7L329.4 246.6c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3l-160-160z" />
                        </svg>
                    </div>
                    <div id="stop-icon" class="center" style="display:none">
                        <svg xmlns="http://www.w3.org/2000/svg" height="16" width="12" viewBox="0 0 384 512">
                            <path fill="#ffffff" d="M0 96C0 60.7 28.7 32 64 32H320c35.3 0 64 28.7 64 64V416c0 35.3-28.7 64-64 64H64c-35.3 0-64-28.7-64-64V96z" />
                        </svg>
                    </div>
                </button>
            </div>
        </div>
    </div>
    <#include "/parts/editMessage.ftl" />
    <script src="/static/js/main.js"></script>
    <script src="/static/js/projects.js"></script>
    <script src="/static/js/settings.js"></script>
    <script src="/static/js/htmlParser.js"></script>
    <script src="/static/js/editMessageView.js"></script>
    <script src="/static/js/account.js"></script>
    <script src="/static/js/message.js"></script>
    <script src="/static/js/chatApi.js"></script>
    <script src="/static/js/navigation-view.js"></script>
    <script src="/static/js/chat.js"></script>
</body>

</html>