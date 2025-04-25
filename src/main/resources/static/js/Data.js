class Data{
    constructor(){
        this.blockedInput = false;
        this.documents = [];
        this.images = [];
    }

    setBlockedInput(value){
        this.blockedInput = value;
        window.inputView.updateView();
    }


    appendImage(image){
        this.images.push(image);
        window.inputView.updateView();
    }

    appendDocument(document){
        this.documents.push(document);
        window.inputView.updateView();

    }

    clearDocumentsAndImages(){
        this.documents = [];
        this.images = [];
        window.inputView.updateView();
    }

    removeDocument(index) {
        if (index >= 0 && index < this.documents.length) {
            this.documents.splice(index, 1);
            window.inputView.updateView();
        }
    }
    removeImage(index) {
        if (index >= 0 && index < this.images.length) {
            this.images.splice(index, 1);
            window.inputView.updateView();
        }
    }

}


window.data = new Data();