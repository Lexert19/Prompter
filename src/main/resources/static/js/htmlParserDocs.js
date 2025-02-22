class HtmlParser{
    constructor(){
        this.elements = [];
        //element jest objektym ktory może mieć typ header, strong, olElement, code, normalText, ulElement
    }


    parse(textFragment){
        //funckja otrzymuje strumieniowany tekst, czytaj linia po linii, cala linia nie jest przykazywana w fragmencie, 
        // textFragment nie jest cala linia tylko fragmenty ktory moze zawierac aktualną i nastepna linie 
        


        //jesli linia zaczyna sie normalnym tekstem, wtedy tworzysz element normalText i dodajesz jej tekst do parametru
        //jesli linia zaczyna się "```language" wtedy tworzysz element code i dodajesz do jego tekstu linie dopki nie wysapi fragment "```"
        //jeli linia zacyzna się na "###" wtedzy to jest header
        //jesli linia zaczyna się na "x. **" (x jest liczbą), to jest olElement, przyklad takiej lini "1. **header**: tekst", ten element ma parmetr header i text
        // jesli linia zacyzna się na - wtedy też to jest ulElement
    }

    toHTML(){
        //zamiania ta funkcja elements do html, wszystkie paramtry text muszą być umieszoczone w <pre><code></code></pre>
    }
}