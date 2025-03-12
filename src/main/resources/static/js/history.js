class History {
  constructor() {
    this.baseUrl = "/api/history";
    this.history = document.getElementById("history");
  }

  async loadHistory() {
    const chats = await this.loadChats();
    this.history.innerHTML = "";
    chats.forEach((chat) => {
      this.addHtmlHistoryIndex(chat);
    });
  }

  async createChatSession() {
    if (window.settings.activeHistory) {
      const chat = await this.createChat();
      return chat.id;
    }
    return "";
  }

  async createChat() {
    try {
      const response = await fetch(`${this.baseUrl}/chats`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error("Error creating chat:", error);
      throw error;
    }
  }

  async saveMessage(chatId, message) {
    try {
      let duration = 0;
      if (message.end !== null) {
        duration = message.end - message.start;
      }
      const messageBody = {
        chatId: chatId,
        text: message.text,
        documents: message.documents,
        images: message.images,
        duration: duration,
        cache: message.cache,
        role: message.role
      };

      const response = await fetch(`${this.baseUrl}/messages`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(messageBody),
        credentials: "include",
      });

      if (!response.ok) {
        if (response.status === 400) {
          throw new Error("Bad Request: Invalid message data.");
        } else if (response.status === 403) {
          throw new Error("Forbidden: Not authorized to save message.");
        } else {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
      }

      return await response.json();
    } catch (error) {
      console.error("Error saving message:", error);
      throw error;
    }
  }

  async getChatHistory(chatId) {
    try {
      const response = await fetch(`${this.baseUrl}/chats/${chatId}/messages`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const rawMessages = await response.json();
      return rawMessages.map(msgData => {
        const message = new Message(
          msgData.role || '',
          msgData.text || '',
          msgData.images || [],
          msgData.documents || [],
          msgData.context || []
        );
  
        message.id = msgData.id || message.id; 
        message.time = msgData.time || Date.now();
        message.start = msgData.start || message.start;
        message.end = msgData.end || null;
        message.cache = msgData.cache || false;

  
        return message;
      });
    } catch (error) {
      console.error("Error getting chat history:", error);
      throw error;
    }
  }

  async getRequestBuilderForChat(chatId) {
    try {
      const messages = await this.getChatHistory(chatId);
      
      const builder = new RequestBuilder();
      
      messages.forEach(msg => {
        builder.addMessage(msg);
      });
      
      return builder;
    } catch(err) {
      console.error("Error creating RequestBuilder from history", err);
      throw err;
    }
  }

  addHtmlHistoryIndex(historyIndex) {
    let index = `<button onclick="window.chat.loadChat('${historyIndex.id}')">${historyIndex.id}: </button>`;
    this.history.innerHTML += index;
  }

  async loadChats() {
    try {
      const response = await fetch(`${this.baseUrl}/chats`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const chats = await response.json();
      return chats;
    } catch (error) {
      console.error("Error loading chats:", error);
      throw error;
    }
  }
}

window.chatHistory = new History();
