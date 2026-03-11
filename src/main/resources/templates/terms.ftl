<#import "/spring.ftl" as spring />
<!DOCTYPE html>
<html lang="pl">
    <head>
        <#include "/parts/links.ftl" />
        <title>Regulamin - Prompter</title>
    </head>
    <body class="background">
        <#include "/parts/navbar.ftl" />
        <div class="container">
            <div class="content-wrapper px-2">
                <div class="legal-container">
                    <h1>Regulamin korzystania z Prompter</h1>
                    <p class="last-updated">Data ostatniej aktualizacji: 10 marca 2026</p>
                    <div class="legal-section">
                        <h2>1. Postanowienia ogólne</h2>
                        <p>
                            1.1. Niniejszy Regulamin określa zasady korzystania z aplikacji Prompter (zwanej dalej "Aplikacją"), dostępnej pod adresem dominik-chyziak.pl.
                        </p>
                        <p>
                            1.2. Właścicielem i administratorem Aplikacji jest Dominik Chyziak, z adresem e-mail: dominikch19@gmail.com (zwany dalej "Usługodawcą").
                        </p>
                        <p>
                            1.3. Korzystanie z Aplikacji oznacza akceptację niniejszego Regulaminu. Osoby nieakceptujące Regulaminu nie powinny korzystać z Aplikacji.
                        </p>
                    </div>
                    <div class="legal-section">
                        <h2>2. Definicje</h2>
                        <p>
                            <strong>Aplikacja / Serwis</strong> – platforma Prompter dostępna online, umożliwiająca interakcję z modelami językowymi AI.
                        </p>
                        <p>
                            <strong>Użytkownik</strong> – osoba fizyczna korzystająca z Aplikacji, która zaakceptowała niniejszy Regulamin.
                        </p>
                        <p>
                            <strong>Konto</strong> – wydzielona część Aplikacji przypisana do Użytkownika, umożliwiająca mu korzystanie z funkcji takich jak historia czatów, projekty czy przechowywanie kluczy API.
                        </p>
                        <p>
                            <strong>Treści Użytkownika (Input)</strong> – wszelkie dane, teksty, obrazy, pliki i zapytania wprowadzane do Aplikacji przez Użytkownika.
                        </p>
                        <p>
                            <strong>Treści Wygenerowane (Output)</strong> – odpowiedzi, teksty, obrazy i inne dane wygenerowane przez modele AI na podstawie Treści Użytkownika.
                        </p>
                        <p>
                            <strong>Klucze API</strong> – poufne dane uwierzytelniające dostarczone przez Użytkownika, umożliwiające korzystanie z usług zewnętrznych dostawców AI (np. OpenAI, Anthropic).
                        </p>
                    </div>
                    <div class="legal-section">
                        <h2>3. Rejestracja i korzystanie z konta</h2>
                        <p>3.1. Rejestracja w Aplikacji odbywa się poprzez uwierzytelnienie z wykorzystaniem konta Google.</p>
                        <p>3.2. Użytkownik zobowiązuje się do podania prawdziwych danych podczas procesu rejestracji.</p>
                        <p>
                            3.3. Użytkownik jest odpowiedzialny za zachowanie poufności swoich danych logowania i wszelkie działania wykonywane na swoim koncie.
                        </p>
                        <p>
                            3.4. Użytkownik ma prawo w każdej chwili usunąć swoje konto, kontaktując się z Usługodawcą pod adresem dominikch19@gmail.com.
                        </p>
                    </div>
                    <div class="legal-section">
                        <h2>4. Klucze API i usługi zewnętrzne</h2>
                        <p>4.1. Aplikacja umożliwia Użytkownikowi wprowadzenie własnych kluczy API do zewnętrznych dostawców usług AI.</p>
                        <p>
                            4.2. Klucze API są przechowywane w zaszyfrowanej formie i wykorzystywane wyłącznie do komunikacji z zewnętrznymi usługami na żądanie Użytkownika.
                        </p>
                        <p>
                            4.3. Usługodawca nie ponosi odpowiedzialności za koszty związane z wykorzystaniem kluczy API przez Użytkownika ani za działanie, dostępność czy politykę prywatności zewnętrznych dostawców AI.
                        </p>
                        <p>
                            4.4. Użytkownik ponosi pełną odpowiedzialność za zgodność swojego korzystania z zewnętrznych usług AI z regulaminami tych usług.
                        </p>
                    </div>
                    <div class="legal-section">
                        <h2>5. Treści generowane przez AI</h2>
                        <p>
                            5.1. Treści Wygenerowane przez modele AI są tworzone automatycznie na podstawie algorytmów i danych treningowych dostawców zewnętrznych.
                        </p>
                        <p>
                            5.2. Usługodawca nie ponosi odpowiedzialności za dokładność, kompletność, aktualność ani przydatność Treści Wygenerowanych.
                        </p>
                        <p>
                            5.3. Użytkownik jest świadomy, że Treści Wygenerowane mogą zawierać błędy, nieścisłości lub treści nieodpowiednie, i zobowiązuje się do ich weryfikacji przed wykorzystaniem.
                        </p>
                        <p>
                            5.4. Aplikacja nie powinna być wykorzystywana do podejmowania decyzji medycznych, prawnych, finansowych ani innych, które mogą mieć istotny wpływ na życie lub zdrowie Użytkownika.
                        </p>
                    </div>
                    <div class="legal-section">
                        <h2>6. Prawa własności intelektualnej</h2>
                        <p>
                            6.1. Wszelkie prawa własności intelektualnej do kodu Aplikacji, jej wyglądu i funkcjonalności przysługują Usługodawcy.
                        </p>
                        <p>6.2. Użytkownik zachowuje wszelkie prawa do Treści Użytkownika wprowadzonych do Aplikacji.</p>
                        <p>
                            6.3. Użytkownik udziela Usługodawcy niewyłącznej, nieodpłatnej licencji na przechowywanie i przetwarzanie Treści Użytkownika w celu świadczenia usług Aplikacji.
                        </p>
                    </div>
                    <div class="legal-section">
                        <h2>7. Ograniczenie odpowiedzialności</h2>
                        <p>
                            7.1. Aplikacja jest dostarczana w stanie "tak jak jest" ("as is") i Usługodawca nie udziela żadnych gwarancji, że będzie działać bez przerw i błędów.
                        </p>
                        <p>
                            7.2. Usługodawca nie ponosi odpowiedzialności za szkody wynikłe z wykorzystania Aplikacji, w szczególności za utratę danych, utracone korzyści czy szkody spowodowane przez treści wygenerowane przez AI.
                        </p>
                        <p>
                            7.3. Odpowiedzialność Usługodawcy w stosunku do Użytkownika, bez względu na podstawę prawną, jest ograniczona do kwoty 100 PLN.
                        </p>
                    </div>
                    <div class="legal-section">
                        <h2>8. Zasady akceptowalnego korzystania</h2>
                        <p>8.1. Użytkownik zobowiązuje się do korzystania z Aplikacji w sposób zgodny z prawem i dobrymi obyczajami.</p>
                        <p>8.2. Zabrania się wykorzystywania Aplikacji do:</p>
                        <ul>
                            <li>generowania treści nielegalnych, obraźliwych, naruszających prawa osób trzecich;</li>
                            <li>podejmowania prób włamania, dekompilacji lub ingerencji w kod Aplikacji;</li>
                            <li>przeciążania serwerów lub zakłócania działania Aplikacji innym użytkownikom;</li>
                            <li>wysyłania spamu lub nieuzasadnionych żądań.</li>
                        </ul>
                        <p>
                            8.3. Usługodawca ma prawo odmówić dostępu do Aplikacji lub usunąć konto Użytkownika naruszającego powyższe zasady.
                        </p>
                    </div>
                    <div class="legal-section">
                        <h2>9. Zmiany Regulaminu</h2>
                        <p>9.1. Usługodawca zastrzega sobie prawo do zmiany Regulaminu w dowolnym momencie.</p>
                        <p>9.2. O istotnych zmianach Użytkownicy zostaną powiadomieni drogą mailową lub poprzez komunikat w Aplikacji.</p>
                        <p>9.3. Korzystanie z Aplikacji po wprowadzeniu zmian oznacza ich akceptację.</p>
                    </div>
                    <div class="legal-section">
                        <h2>10. Postanowienia końcowe</h2>
                        <p>10.1. Regulamin podlega prawu polskiemu.</p>
                        <p>10.2. W przypadku sporów właściwe są sądy powszechne w Polsce.</p>
                        <p>
                            10.3. W sprawach nieuregulowanych niniejszym Regulaminem zastosowanie mają przepisy Kodeksu Cywilnego oraz ustawy o świadczeniu usług drogą elektroniczną.
                        </p>
                        <p>10.4. Wszelkie zapytania dotyczące Regulaminu należy kierować na adres: dominikch19@gmail.com.</p>
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
