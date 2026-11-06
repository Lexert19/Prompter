class History {
    static _instance = null;

    static instance() {
        if (!History._instance) {
            History._instance = new History();
        }
        return History._instance;
    }

    constructor() {
        if (History._instance) {
            return History._instance;
        }
        History._instance = this;

        this.baseUrl = "/api/history";
        this.history = document.getElementById("history");
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

        const response = await fetchWithAuth('/api/files/upload', {
            method: 'POST',
            body: formData,
            credentials: 'include'
        });

        if (!response.ok) throw new Error(`Upload failed: ${response.status}`);
        const uploaded = await response.json();
        return uploaded.id;
    }


    addHtmlHistoryIndex(historyIndex) {
        const date = new Date(historyIndex.createdAt || Date.now());
        const options = { hour: '2-digit', minute: '2-digit' };
        const formatted = `${date.toLocaleDateString()} ${date.toLocaleTimeString([], options)}`;
        const uuid = historyIndex.uuid;
        let index = `
      <div style="display: flex; align-items: center;">
        <button class="rounded-1" onclick="Chat.instance().loadChat('${uuid}')">${formatted}</button>
        <button class="d-flex rounded-1 justify-content-end" onclick="History.instance().deleteChat('${uuid}')" style="margin-left: 10px; background-color: transparent; border: none; cursor: pointer;">
          <i class="fa fa-trash" aria-hidden="true"></i>
        </button>
      </div>
    `;
        this.history.insertAdjacentHTML('beforeend', index);
    }

    async deleteChat(chatId) {
        try {
            const response = await fetchWithAuth(`${this.baseUrl}/chats/${chatId}`, {
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

    async loadChats(page = 0, size = 20) {
        const response = await fetchWithAuth(
            `${this.baseUrl}/chats?page=${page}&size=${size}`,
            { method: "GET", credentials: "include" }
        );
        if (!response.ok) {
            throw new Error(`HTTP ${response.status} loading chats`);
        }
        const data = await response.json();
        return data.content || [];
    }

    async loadHistory() {
        if (!this.history) return;
        try {
            const chats = await this.loadChats(0, 20);
            chats.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
            this.history.innerHTML = "";
            if (chats.length > 0) {
                const clearAllBtn = document.createElement('button');
                clearAllBtn.className = 'rounded-1 mb-2';
                clearAllBtn.style.cssText =
                    'width:100%;background-color:#7a1f1f;color:#fff;' +
                    'border:none;padding:6px 8px;cursor:pointer;font-size:0.9em;';
                clearAllBtn.innerHTML =
                    `<i class="fa fa-trash" aria-hidden="true"></i> ` +
                    `${t.t("deleteAllHistory")}`;
                clearAllBtn.onclick = () => this.deleteAllHistory();
                this.history.appendChild(clearAllBtn);
            }
            chats.forEach(chat => this.addHtmlHistoryIndex(chat));
        } catch (e) {
            console.error("[History] loadHistory failed", e);
        }
    }

    async deleteAllHistory() {
        const msg = t.t("confirmDeleteAllHistory");
        if (!confirm(msg)) return;

        try {
            const response = await fetchWithAuth(`${this.baseUrl}/chats`, {
                method: "DELETE",
                credentials: "include",
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            if (window.location.pathname !== '/chat') {
                window.history.pushState({}, document.title, '/chat');
            }
            if (window.Chat && Chat._instance) {
                Chat.instance().startNewChat();
            }

            this.loadHistory();
        } catch (error) {
            console.error("Error deleting all history:", error);
            alert(t.t("deleteAllHistoryFailed"));
        }
    }


}
History.instance();

document.addEventListener('DOMContentLoaded', () => {
    History.instance().loadHistory();
});