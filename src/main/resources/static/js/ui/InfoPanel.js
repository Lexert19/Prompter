class InfoPanel {
  constructor() {
    if (InfoPanel.instance) {
      return InfoPanel.instance;
    }
    InfoPanel.instance = this;

    this.panel = document.getElementById('InfoPanel');
    this.toggleBtn = document.getElementById('toggle-info-button');
    this.closeBtn = document.getElementById('closeInfoBtn');
    this._statsTimer = null;

    this.init();
  }

  static getInstance() {
    if (!InfoPanel.instance) {
      new InfoPanel();
    }
    return InfoPanel.instance;
  }

  init() {
    if (this.toggleBtn) {
      this.toggleBtn.addEventListener('click', () => this.toggle());
    }
    if (this.closeBtn) {
      this.closeBtn.addEventListener('click', () => this.hide());
    }

    this.handleResize();
    window.addEventListener('resize', () => this.handleResize());
    this.updateStats();
  }

  isOpen() {
    return this.panel.classList.contains('info-panel-show');
  }

  show() {
    this.panel.classList.add('info-panel-show');
    this.updateButtonZIndex();
    this.updateStats();
    this.startStatsTimer();
  }

  hide() {
    this.panel.classList.remove('info-panel-show');
    this.updateButtonZIndex();
    this.stopStatsTimer();
  }

  toggle() {
    this.panel.classList.toggle('info-panel-show');
    this.updateButtonZIndex();

    if (this.isOpen()) {
      this.updateStats();
      this.startStatsTimer();
    } else {
      this.stopStatsTimer();
    }
  }

  startStatsTimer() {
    this.stopStatsTimer();
    this._statsTimer = setInterval(() => {
      if (this.isOpen()) {
        this.updateStats();
      }
    }, 500);
  }

  stopStatsTimer() {
    if (this._statsTimer) {
      clearInterval(this._statsTimer);
      this._statsTimer = null;
    }
  }

  updateStats() {
    const container = document.getElementById('request-stats');
    if (!container) return;

    const chat = Chat._instance;
    if (!chat || !chat.requestBuilder) {
      container.innerHTML = '<div class="info-row">Lack of data</div>';
      return;
    }

    const rb = chat.requestBuilder;
    const stats = typeof rb.getStats === 'function'
      ? rb.getStats()
      : {
      total: rb.messages?.length || 0,
      user: rb.messages?.filter(m => m.role === 'user').length || 0,
      assistant: rb.messages?.filter(m => m.role === 'assistant').length || 0,
      textLength: rb.messages?.reduce((s, m) => s + (m.text?.length || 0), 0) || 0,
      images: rb.messages?.reduce((s, m) => s + (m.images?.length || 0), 0) || 0,
      documents: rb.messages?.reduce((s, m) => s + (m.documents?.length || 0), 0) || 0,
      contextSize: rb.calculateContextSize ? rb.calculateContextSize() : 0,
      estimatedTokens: 0,
      estimatedContextTokens: 0
    };

    const settings = Settings._instance;

    let html = `
      <div class="info-row"><span>${t.t("info.messages")}:</span><span>${stats.total}</span></div>
      <div class="info-row"><span>${t.t("info.userRequests")}:</span><span>${stats.user}</span></div>
      <div class="info-row"><span>${t.t("info.assistantReplies")}:</span><span>${stats.assistant}</span></div>
      <div class="info-row"><span>${t.t("info.textLength")}:</span><span>${stats.textLength.toLocaleString()} ${t.t("info.chars")}</span></div>
      <div class="info-row"><span>${t.t("info.estimatedTokens")}:</span><span>${stats.estimatedTokens.toLocaleString()}</span></div>
      <div class="info-row"><span>${t.t("info.context")}:</span><span>${stats.contextSize.toLocaleString()} ${t.t("info.chars")}</span></div>
      <div class="info-row"><span>${t.t("info.estimatedContextTokens")}:</span><span>${stats.estimatedContextTokens.toLocaleString()}</span></div>
      <div class="info-row"><span>${t.t("info.images")}:</span><span>${stats.images}</span></div>
      <div class="info-row"><span>${t.t("info.documents")}:</span><span>${stats.documents}</span></div>
    `;

    if (settings) {
      html += `
        <hr>
        <div class="info-row"><span>${t.t("settings.model")}:</span><span>${settings.model || '-'}</span></div>
        <div class="info-row"><span>${t.t("provider")}:</span><span>${settings.provider || '-'}</span></div>
        <div class="info-row"><span>${t.t("maxTokens")}:</span><span>${settings.maxTokens || '-'}</span></div>
        <div class="info-row"><span>${t.t("temperature")}:</span><span>${settings.temperature ?? '-'}</span></div>
      `;
    }

    container.innerHTML = html;
  }

  updateButtonZIndex() {
    this.toggleBtn.classList.toggle('panel-open', this.isOpen());
  }

  onMouseMove(e) {
    const half = window.innerWidth / 2;
    if (window.innerWidth - e.clientX < 400 && e.clientX > half) {
      this.show();
    } else {
      this.hide();
    }
  }

  handleResize() {
    if (window.innerWidth > 768) {
      document.addEventListener('mousemove', (e) => this.onMouseMove(e));
    } else {
      document.removeEventListener('mousemove', this.mouseMoveHandler);
      this.hide();
    }
  }
}

document.addEventListener('DOMContentLoaded', () => {
  InfoPanel.getInstance();
});