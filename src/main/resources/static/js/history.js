class History{
    constructor() {
        this.baseUrl = '/api/history';
        this.history = document.getElementById("history");

    }

    async loadHistory(){
        const chats = await this.loadChats();
        this.history.innerHTML = '';
        chats.forEach(chat => {
            this.addHtmlHistoryIndex(chat); 
        });
    }

    async createChatSession(){
        if(window.showSettings.activeHistory){
            const chat = await this.createChat();
             return chat.id;
        }
        return "";
    }

    async createChat() {
        try {
            const response = await fetch(`${this.baseUrl}/chats`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json(); 
        } catch (error) {
            console.error('Error creating chat:', error);
            throw error; 
        }
    }

    async saveMessage(chatId, content) {
        try {
            const messageBody = {
                chatId: chatId,
                content: content,
            };

            const response = await fetch(`${this.baseUrl}/messages`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(messageBody),
                credentials: 'include', 
            });

            if (!response.ok) {
                if (response.status === 400) {
                    throw new Error('Bad Request: Invalid message data.');
                } else if (response.status === 403) {
                    throw new Error('Forbidden: Not authorized to save message.');
                } else {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
            }

            return await response.json(); 
        } catch (error) {
            console.error('Error saving message:', error);
            throw error;
        }
    }

    async getChatHistory(chatId) {
        try {
            const response = await fetch(`${this.baseUrl}/chats/${chatId}/messages`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Error getting chat history:', error);
            throw error;
        }
    }

    addHtmlHistoryIndex(historyIndex) {
        let index = `<button onclick="window.chat.loadChat('${historyIndex.id}')">${historyIndex.id}: </button>`;
        this.history.innerHTML += index;
    }

    async loadChats() {
        try {
            const response = await fetch(`${this.baseUrl}/chats`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const chats = await response.json();
            return chats; 
        } catch (error) {
            console.error('Error loading chats:', error);
            throw error;
        }
    }
}

window.history = new History();