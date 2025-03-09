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
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" integrity="..." crossorigin="anonymous" referrerpolicy="no-referrer" />
    <title>Prompter</title>
</head>

<body>
    <div class="center" style="height: 100vh;">
        <div class="block" style="max-width: 600px; padding: 20px;">
            <div class="chat-header" style="border-radius: 10px; margin-bottom: 30px; text-align: center;">
                <h1 style="margin: 0; font-size: 3rem; font-weight: bold;">
                    <span style="color: #6366f1;">Prompter</span>
                    <img src="/favicon.ico" width="50px" height="50px" style="vertical-align: middle; margin-left: 10px; filter: drop-shadow(2px 2px 3px rgba(0,0,0,0.3));"> <!-- Powiększono logo i dodano cień -->
                </h1>
                <p style="margin-top: 10px; color: #a0a6b9; font-size: 1.2rem;">Twój inteligentny asystent AI do kreatywnych zadań</p>
            </div>
            <div class="panel center mb-1">
                <a href="/chat" class="button" style="padding: 15px 30px; font-size: 1.1rem;">
                    <i class="fas fa-comment-dots"></i>
                    Rozpocznij czat
                </a>
            </div>
            <div class="alert-warning mt-1">
                <h3 class="center" style="margin: 0 0 10px 0; color: #ffc107;">
                    <i class="fas fa-exclamation-triangle"></i>
                    Wskazówka!
                </h3>
                <p style="margin: 0; color: #ffb347;">
                    Korzystanie z własnego klucza API to oszczędność i pełna kontrola nad kosztami. <br>
                    Uniknij wysokich opłat subskrypcyjnych!
                </p>
            </div>
            <div class="mt-1" style="background: rgba(45,45,49,0.8); padding: 20px; border-radius: 10px;">
                <h2 style="color: white; margin-bottom: 20px; font-size: 1.5rem;">Zaawansowane funkcje Promptera</h2>
                <p style="color: #a0a6b9; margin-bottom: 20px;">Odkryj możliwości Promptera, które przeniosą Twoją interakcję z AI na wyższy poziom:</p>
                <div class="file-grid" style="grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 20px;">
                    <div class="file-item" style="text-align: center;">
                        <div class="icon-file" style="font-size: 3rem; margin-bottom: 10px;"> <i class="fas fa-search-plus"></i> </div>
                        <div class="file-name" style="color: white; font-weight: bold;">RAG (Retrieval-Augmented Generation)</div>
                        <div class="file-description" style="color: #a0a6b9; font-size: 0.9rem; margin-top: 5px;">Generuj odpowiedzi z dostępem do aktualnej wiedzy i kontekstu.</div>
                    </div>
                    <div class="file-item" style="text-align: center;">
                        <div class="icon-file" style="font-size: 3rem; margin-bottom: 10px;"> <i class="fas fa-sliders-h"></i> </div>
                        <div class="file-name" style="color: white; font-weight: bold;">Kontrola parametrów zapytania</div>
                        <div class="file-description" style="color: #a0a6b9; font-size: 0.9rem; margin-top: 5px;">Precyzyjnie dostosuj zachowanie AI do swoich potrzeb.</div>
                    </div>
                    <div class="file-item" style="text-align: center;">
                        <div class="icon-file" style="font-size: 3rem; margin-bottom: 10px;"> <i class="fas fa-magic"></i> </div>
                        <div class="file-name" style="color: white; font-weight: bold;">Prompt Engineering</div>
                        <div class="file-description" style="color: #a0a6b9; font-size: 0.9rem; margin-top: 5px;">Twórz skuteczne prompty i uzyskuj najlepsze rezultaty.</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>

</html>