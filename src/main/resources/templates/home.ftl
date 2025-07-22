<#include "/parts/head.ftl" />
    <div class="center" style="height: 100vh;">
        <div class="block" style="max-width: 600px; padding: 20px;">
            <div class="chat-header d-flex flex-column align-items-center" style="border-radius: 10px; margin-bottom: 24px; text-align: center;">
                    <img src="/favicon" class="d-block" width="90px" height="90px" style="vertical-align: middle; margin-left: 10px; filter: drop-shadow(2px 2px 3px rgba(0,0,0,0.3));">
                <h1 style="margin: 0; font-size: 4rem; font-weight: bold;">
                    <span class="text-shadow-md" style="color: #6366f1;">Prompter</span>
                </h1>
                <p class="text-white fs-5 text-shadow-lg" style="margin-top: 10px; font-size: 1.2rem;">Podłącz klucze API i płać tylko za wykorzystanie.</p>
            </div>
            <div class="panel center mb-1">
                <a href="/chat" class="open_chat text-decoration-none rounded-pill" style="padding: 15px 30px; font-size: 1.1rem;">
                    <i class="fas fa-comment-dots"></i>
                    Rozpocznij czat
                </a>
            </div>
            <div class="alert-warning d-none mt-1">
                <h3 class="center" style="margin: 0 0 10px 0; color: #ffc107;">
                    <i class="fas fa-exclamation-triangle"></i>
                    Wskazówka!
                </h3>
                <p style="margin: 0; color: #ffb347;">
                    Korzystanie z własnego klucza API to oszczędność i pełna kontrola nad kosztami. <br>
                    Uniknij wysokich opłat subskrypcyjnych!
                </p>
            </div>
            <div class="mt-1 d-none" style="background: rgba(45,45,49,0.8); padding: 20px; border-radius: 10px;">
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

<#include "/parts/footer.ftl" />
