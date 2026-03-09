<#import "/spring.ftl" as spring />
<!DOCTYPE html>
<html lang="pl">
<head>
    <#include "/parts/links.ftl" />
    <title>Polityka Prywatności - Prompter</title>
</head>
<body class="background">
<#include "/parts/navbar.ftl" />

<div class="container">
    <div class="content-wrapper px-2">
        <div class="legal-container">
            <h1>Polityka Prywatności</h1>
            <p class="last-updated">Data ostatniej aktualizacji: 10 marca 2026</p>

            <div class="legal-section">
                <h2>1. Kim jesteśmy</h2>
                <p>Administratorem Twoich danych osobowych jest Dominik Chyziak, kontakt: dominikch19@gmail.com. Aplikacja Prompter umożliwia interakcję z modelami językowymi AI z wykorzystaniem własnych kluczy API użytkownika.</p>
            </div>

            <div class="legal-section">
                <h2>2. Jakie dane zbieramy</h2>
                <p><strong>Dane konta:</strong> Podczas logowania przez Google zbieramy Twój adres e-mail. Nie zbieramy imienia, nazwiska ani innych danych z profilu Google bez Twojej dodatkowej zgody.</p>
                <p><strong>Klucze API:</strong> Przechowujemy w zaszyfrowanej formie klucze API, które dobrowolnie wprowadzasz. Służą one wyłącznie do komunikacji z zewnętrznymi usługami AI.</p>
                <p><strong>Historia czatów:</strong> Jeśli włączysz opcję zapisywania historii, przechowujemy Twoje konwersacje, abyś mógł do nich wracać.</p>
                <p><strong>Pliki i projekty:</strong> Przechowujemy pliki i dokumenty, które dobrowolnie przesyłasz w ramach funkcji projektów.</p>
                <p><strong>Dane techniczne:</strong> Automatycznie zbieramy adres IP, typ przeglądarki, system operacyjny oraz dzienniki zdarzeń w celu zapewnienia bezpieczeństwa i analizy błędów.</p>
            </div>

            <div class="legal-section">
                <h2>3. W jaki sposób wykorzystujemy Twoje dane</h2>
                <p>Twoje dane wykorzystujemy do:</p>
                <ul>
                    <li>umożliwienia logowania i utrzymania Twojego konta;</li>
                    <li>przechowywania Twoich kluczy API (wyłącznie w zaszyfrowanej formie);</li>
                    <li>zapisywania historii czatów i projektów (jeśli wyrazisz zgodę);</li>
                    <li>zapewnienia bezpieczeństwa i stabilności Aplikacji;</li>
                    <li>kontaktowania się z Tobą w sprawach technicznych lub związanych z Twoim kontem.</li>
                </ul>
                <p><strong>Nie wykorzystujemy Twoich danych do trenowania modeli AI.</strong> Nie udostępniamy Twoich konwersacji zewnętrznym dostawcom AI – komunikacja odbywa się bezpośrednio z wykorzystaniem Twoich kluczy API.</p>
            </div>

            <div class="legal-section">
                <h2>4. Podstawa prawna przetwarzania</h2>
                <p>Przetwarzamy Twoje dane na podstawie:</p>
                <ul>
                    <li><strong>Art. 6 ust. 1 lit. b RODO</strong> – niezbędność do wykonania umowy (świadczenie usługi Aplikacji);</li>
                    <li><strong>Art. 6 ust. 1 lit. a RODO</strong> – Twoja zgoda (w przypadku historii czatów i plików);</li>
                    <li><strong>Art. 6 ust. 1 lit. f RODO</strong> – nasz prawnie uzasadniony interes (zapewnienie bezpieczeństwa, analiza błędów, ewentualne dochodzenie roszczeń).</li>
                </ul>
            </div>

            <div class="legal-section">
                <h2>5. Komu udostępniamy dane</h2>
                <p>Twoje dane mogą być przekazywane:</p>
                <ul>
                    <li><strong>Dostawcom usług hostingowych</strong> – serwery, na których działa Aplikacja;</li>
                    <li><strong>Dostawcom usług AI (OpenAI, Anthropic)</strong> – wyłącznie w momencie wysłania przez Ciebie zapytania, z wykorzystaniem Twojego klucza API. Nie przechowujemy tych danych ani nie mamy do nich dostępu po stronie dostawców;</li>
                    <li><strong>Organom państwowym</strong> – wyłącznie w przypadkach przewidzianych prawem.</li>
                </ul>
                <p><strong>Nie sprzedajemy Twoich danych reklamodawcom ani innym podmiotom.</strong></p>
            </div>

            <div class="legal-section">
                <h2>6. Pliki cookies</h2>
                <p>Nasza Aplikacja wykorzystuje pliki cookies do:</p>
                <ul>
                    <li>utrzymania sesji logowania (JSESSIONID);</li>
                    <li>zapamiętania Twoich preferencji językowych (lang).</li>
                </ul>
                <p>Pliki cookies są niezbędne do prawidłowego działania Aplikacji. Korzystając z Aplikacji, wyrażasz zgodę na ich stosowanie. Możesz zmienić ustawienia cookies w swojej przeglądarce, ale może to wpłynąć na działanie niektórych funkcji.</p>
            </div>

            <div class="legal-section">
                <h2>7. Jak długo przechowujemy dane</h2>
                <ul>
                    <li><strong>Dane konta:</strong> do momentu usunięcia konta przez Użytkownika.</li>
                    <li><strong>Historia czatów i projekty:</strong> do momentu usunięcia przez Użytkownika lub usunięcia konta.</li>
                    <li><strong>Klucze API:</strong> do momentu usunięcia przez Użytkownika lub usunięcia konta.</li>
                    <li><strong>Dzienniki techniczne:</strong> maksymalnie 90 dni.</li>
                </ul>
            </div>

            <div class="legal-section">
                <h2>8. Twoje prawa</h2>
                <p>W związku z przetwarzaniem danych osobowych przysługują Ci następujące prawa:</p>
                <ul>
                    <li>prawo dostępu do danych (w tym uzyskania kopii);</li>
                    <li>prawo do sprostowania danych;</li>
                    <li>prawo do usunięcia danych („prawo do bycia zapomnianym”);</li>
                    <li>prawo do ograniczenia przetwarzania;</li>
                    <li>prawo do przenoszenia danych;</li>
                    <li>prawo do wniesienia sprzeciwu;</li>
                    <li>prawo do cofnięcia zgody w dowolnym momencie (bez wpływu na zgodność z prawem przetwarzania sprzed jej cofnięcia).</li>
                </ul>
                <p>Aby skorzystać ze swoich praw, skontaktuj się z nami: dominikch19@gmail.com. Masz również prawo wniesienia skargi do Prezesa Urzędu Ochrony Danych Osobowych.</p>
            </div>

            <div class="legal-section">
                <h2>9. Bezpieczeństwo danych</h2>
                <p>Stosujemy odpowiednie środki techniczne i organizacyjne, aby chronić Twoje dane przed nieuprawnionym dostępem, utratą lub zniszczeniem. Klucze API są przechowywane w zaszyfrowanej formie (szyfrowanie AES-256). Dostęp do serwerów mają wyłącznie upoważnione osoby.</p>
            </div>

            <div class="legal-section">
                <h2>10. Zmiany Polityki Prywatności</h2>
                <p>Zastrzegamy sobie prawo do wprowadzania zmian w niniejszej Polityce Prywatności. O wszelkich istotnych zmianach poinformujemy Cię poprzez komunikat w Aplikacji lub drogą mailową.</p>
            </div>

            <div class="legal-section">
                <h2>11. Kontakt</h2>
                <p>We wszystkich sprawach związanych z prywatnością i ochroną danych osobowych prosimy o kontakt: Dominik Chyziak, e-mail: dominikch19@gmail.com.</p>
            </div>
        </div>
    </div>
</div>

<style>
    .legal-container {
        max-width: 800px;
        margin: 80px auto 40px;
        background: rgba(45,45,49,0.95);
        padding: 40px;
        border-radius: 12px;
        color: white;
    }
    .legal-container h1 {
        color: var(--accent);
        margin-bottom: 0.5rem;
    }
    .legal-container .last-updated {
        color: #aaa;
        margin-bottom: 2rem;
        font-style: italic;
    }
    .legal-container h2 {
        color: var(--accent);
        font-size: 1.3rem;
        margin-top: 1.5rem;
        margin-bottom: 1rem;
    }
    .legal-container p, .legal-container li {
        color: #ddd;
        line-height: 1.6;
    }
    .legal-container ul {
        margin-left: 1.5rem;
        margin-bottom: 1rem;
    }
    .legal-section {
        margin-bottom: 2rem;
        border-bottom: 1px solid rgba(255,255,255,0.1);
        padding-bottom: 1rem;
    }
</style>

<#include "/parts/footer.ftl" />
</body>
</html>