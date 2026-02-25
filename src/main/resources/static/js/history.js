class History {
    constructor() {
        this.baseUrl = "/api/history";
        this.history = document.getElementById("history");
    }

    async loadHistory() {
        const chats = await this.loadChats();
        const sortedChats = chats.sort(
            (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
        );
        this.history.innerHTML = "";
        sortedChats.forEach((chat) => {
            this.addHtmlHistoryIndex(chat);
        });
    }



    async createChatSession(content) {
        if (window.settings.activeHistory) {
            const chat = await this.createChat();
            //this.saveMessage(chat.id, content);

            const newPath = `/chat/${chat.id}`;

            if (window.history && typeof window.history.pushState === "function") {
                window.history.pushState({ chatId: chat.id }, document.title, newPath);
            } else {
                console.warn("History API not supported");
            }
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
            const images = [];
            for (const img of message.images) {
                if (typeof img === 'string' && img.startsWith('data:image')) {
                    const fileId = await this.uploadImageBase64(img);
                    images.push(fileId);
                } else {
                    images.push(img);
                }
            }

            const messageBody = {
                chatId: chatId,
                text: message.text,
                documents: message.documents,
                images: images,
                end: message.end,
                start: message.start,
                cache: message.cache,
                role: message.role
            };

            const response = await fetch(`${this.baseUrl}/messages`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(messageBody),
                credentials: "include",
            });

            if (!response.ok) {
                if (response.status === 400) throw new Error("Bad Request: Invalid message data.");
                if (response.status === 403) throw new Error("Forbidden: Not authorized to save message.");
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error("Error saving message:", error);
            throw error;
        }
    }

    async uploadImageBase64(base64String) {
        const arr = base64String.split(',');
        const mime = arr[0].match(/:(.*?);/)[1];
        const bstr = atob(arr[1]);
        let n = bstr.length;
        const u8arr = new Uint8Array(n);
        while (n--) u8arr[n] = bstr.charCodeAt(n);
        const file = new File([u8arr], `image_${Date.now()}.png`, { type: mime });

        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch('/api/files/upload', {
            method: 'POST',
            body: formData,
            credentials: 'include'
        });

        if (!response.ok) throw new Error(`Upload failed: ${response.status}`);
        const uploaded = await response.json();
        return uploaded.id;
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
            }).sort((a, b) => {
                if (a.start === undefined || a.start === null) return 1;
                if (b.start === undefined || b.start === null) return -1;

                return a.start - b.start;
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
        const date = new Date(historyIndex.createdAt || Date.now());
        const options = { hour: '2-digit', minute: '2-digit' };
        const formatted = `${date.toLocaleDateString()} ${date.toLocaleTimeString([], options)}`;
        let index = `
      <div style="display: flex; align-items: center;">
        <button onclick="window.chat.loadChat('${historyIndex.id}')">${formatted}</button>
        <button onclick="window.chatHistory.deleteChat('${historyIndex.id}')" style="margin-left: 10px; background-color: transparent; border: none; cursor: pointer;">
          <i class="fa fa-trash" aria-hidden="true"></i>
        </button>
      </div>
    `;
        this.history.innerHTML += index;
    }

    async deleteChat(chatId) {
        try {
            const response = await fetch(`${this.baseUrl}/chats/${chatId}`, {
                method: "DELETE",
                credentials: "include",
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            this.loadHistory();
        } catch (error) {
            console.error("Error deleting chat:", error);
        }
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
