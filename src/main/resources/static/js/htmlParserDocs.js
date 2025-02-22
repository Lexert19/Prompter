class HtmlParser{
    constructor(){
        this.elements = [];
        //element jest objektym ktory może mieć typ header, strong, listElement, code, normalText
    }


    parse(textFragment){
        //funckja ta parsuje fragment tekstu na odpowiedni element, wysylane fragmenty sa strumieniowe, przekazywane sa fragmenty tekstu z ktorych zbudjujszsz liste elemntow 

        // jesli znajdzie znak "###" dodaje do elementow element header ktory ma swoj tekst kazdy nastepny
        //  fragment tekstu bedzie dodawany do header dopoki nie znajdzie znaku \n, element header ma parametr text

        //jesli znjadzie tekst "\n-" wtedy to jest element listy dodawane  sa dalsze fragmenty tekstu to prametru text dopoki nie znjadzie \n
        //prawdopodnbie nastepny element jest tez jest listą 
        
        //elementu kodu zaczynja sie na "```language\n" i konczą na "```\n" tekst zawarty pomiędzy tymi elemnetami zostanie dodany do objektu code

        //fragment tekstu ktory ma tylko na poczatku "\n\n" i na końcu "\n\n" jest to normalText i dodajesz do atrubutu jego text tekst pomiedzy tymi gragmentami

        // fragment ktory zaczyna się na "**" i kończy na "**" to jest element strong, dodajesz do jego fragmenty tekstu dopoki sie nie zakonczy


    }

    toHTML(){
        //zamiania ta funkcja elements do html, wszystkie paramtry text muszą być umieszoczone w <pre><code></code></pre>
    }
}