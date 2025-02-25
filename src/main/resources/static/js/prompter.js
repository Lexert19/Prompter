class Settings{
    constructor(){
        this.memory = false;
        this.longDocument = false;
        this.cache = false;
        this.chainOfThoughts = false;
        this.programmer = false;
        this.manySolutions = false;
    }

  
    getPrompts(){
        let instruction = "";
        if (this.longDocument) {
            instruction += "- quote the relevant sections from the document.\n"
        }
        if (this.chainOfThoughts) {
            instruction += "- Think through it step by step before giving final answer.\n"

        }
        if(this.manySolutions){
            instruction += `- Write down shortly 12 possible solutions for the given problem and choose the best one.\n`;
        }

        if(this.programmer){
            instruction += `
            - You are programmer.  Your final answer is code\n`;
        }

        if(instruction != ""){
            instruction = "INSTRUCTIONS START "+instruction+" INSTRUCTIONS END"

        }
        // if(this.programmer){
        //     contentText += `<instruction>
        //     1.You are programmer 
        //     2.Think through it step by step before giving final answer: 
        //     - Write how should it works 
        //     -
        //     - Provide a full list of required info to help properly understand the task.
        //     3. Your final answer is code</instruction>`;
        // }

       

            //contentText += "<instruction>Think through it step by step before giving final answer</instruction>"

      
        // if (this.programmer) {
        //     contentText += `<instruction>You are programmer. Don't use too old classes and functions. Your code should be:
        //     - Code that the user is likely to modify, iterate on, or take ownership of
        //     - Self-contained, complex code that can be understood on its own, without context from the conversation
        //     - Code intended for eventual use outside the conversation (e.g., reports, emails, presentations)
        //     - Code likely to be referenced or reused multiple times</instruction>`
        // }
        return instruction;
    }
}