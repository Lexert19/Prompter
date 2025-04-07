class Message {
  constructor(role = "", text = "", images = [], documents = [], context = []) {
    this.role = role;
    this.content = [];
    this.id = Math.random().toString(36);
    this.cache = false;
    this.documents = documents;
    this.context = context;
    this.text = text;
    this.time = Date.now();
    this.start = Date.now();
    this.end = null;
    this.images = images;
  }

  appendText(text) {
    this.text += text;
  }

  getText() {
    return this.text;
  }

   buildContent() {
      const content = [];

      if (this.text) {
        let textContent = "";
        this.context.forEach(
          (context) => (textContent += `<context>${context}</context>`)
        );
        this.documents.forEach(
          (doc) => (textContent += "<document>" + doc + "</document>")
        );

        content.push({
          type: "text",
          text: textContent + this.text,
          cache: this.cache,
        });
      }

      this.images.forEach(img => {
        const parts = img.split(';');
        const mediaType = parts[0].split(':')[1];
        const data = parts[1].split(',')[1];

        content.push({
          type: "image",
          mediaType: mediaType,
          data: data,
          cache: this.cache
        });
      });

      return content;
    }



 getHtmlImages() {
   return this.images.map((img) => {
     const parts = img.split(';');
     const mediaType = parts[0].split(':')[1];
     const data = parts[1].split(',')[1];

     return `<img class="image" src="data:${mediaType};base64,${data}" alt="Inline image">`;
   }).join("");
 }

  getHtmlFiles() {
    let documentsText = "";
    this.documents.forEach((document, index) => {
      documentsText += "Dokument: " + document.length + " ";
    });
    return documentsText;
  }
}

class RequestBuilder {
  constructor() {
    this.messages = [];
    this.stream = true;
  }

  getSystem() {
    if (document.getElementById("systemSwitch").checked) {
      return window.settings.system;
    }
    return "";
  }

  getType() {
    return window.settings.type;
  }

  getProvider() {
    return window.settings.provider;
  }

  getModel() {
    return window.settings.model;
  }

  getUrl() {
    return window.settings.url;
  }

  getMaxTokens() {
    return window.settings.maxTokens;
  }

  getTemperature() {
    return window.settings.temperature;
  }

  getKey() {
    return window.settings.key;
  }

  toRequestJSON() {
    let messagesToInclude;
    if (window.settings.memory) {
      messagesToInclude = [...this.messages];
    } else {
      messagesToInclude = [
        ...this.messages.filter((message) => message.cache === true),
        this.messages[this.messages.length - 1],
      ];
    }

    return JSON.stringify({
      model: this.getModel(),
      url: this.getUrl(),
      provider: this.getProvider(),
      messages: messagesToInclude.map((message) => ({
        role: message.role,
        content: message.buildContent()
      })),
      maxTokens: this.getMaxTokens(),
      temperature: this.getTemperature(),
      stream: this.stream,
      type: this.getType(),
      key: this.getKey(),
      system: this.getSystem(),
    });
  }

  mapContent(content) {
    const baseContent = {
      type: content.type,
      text: content.text || "",
      cache: content.cache || false,
    };

    switch (content.type) {
      case "image_url":
        return {
          ...baseContent,
          url: content.url,
          mediaType: content.mediaType,
        };

      case "image":
        return {
          ...baseContent,
          mediaType: content.mediaType,
          data: content.data,
        };

      default:
        return baseContent;
    }
  }

  addMessage(message) {
    this.messages.push(message);
    return this;
  }
}
