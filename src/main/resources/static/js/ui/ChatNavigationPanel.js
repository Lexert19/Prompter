class ChatNavigationPanel {
  constructor() {
    if (ChatNavigationPanel.instance) {
      return ChatNavigationPanel.instance;
    }
    ChatNavigationPanel.instance = this;

    this.toggleBtn = document.getElementById('toggle-nav-button');
    this.chatNav = document.getElementById('ChatNavigation');
    this.closeNavBtn = document.getElementById('closeNavBtn');
    this.pages = document.getElementById('pages');

    this.mouseMoveHandler = (e) => this.onMouseMove(e);

    this.init();
  }

  static getInstance() {
    if (!ChatNavigationPanel.instance) {
      new ChatNavigationPanel();
    }
    return ChatNavigationPanel.instance;
  }

  init() {
    if (this.toggleBtn && this.chatNav) {
      this.toggleBtn.addEventListener('click', () => this.toggle());
    }

    if (this.closeNavBtn && this.chatNav) {
      this.closeNavBtn.addEventListener('click', () => this.hide());
    }

    if (this.chatNav) {
      this.chatNav.addEventListener('click', (e) => {
        const btn = e.target.closest('button[data-panel]');
        if (!btn) return;
        this.openPanel(btn.dataset.panel);
      });
    }

    this.handleResize();
    window.addEventListener('resize', () => this.handleResize());
    this.updateButtonZIndex();

    this.openPanel('chatSettings');
  }

  isOpen() {
    return this.chatNav.classList.contains('chat-navigation-show');
  }

  show() {
    this.chatNav.classList.add('chat-navigation-show');
    this.updateButtonZIndex();
  }

  hide() {
    this.chatNav.classList.remove('chat-navigation-show');
    this.updateButtonZIndex();
  }

  toggle() {
    this.chatNav.classList.toggle('chat-navigation-show');
    this.updateButtonZIndex();
  }

  updateButtonZIndex() {
    if (this.toggleBtn) {
      if (this.isOpen()) {
        this.toggleBtn.classList.add('nav-open');
      } else {
        this.toggleBtn.classList.remove('nav-open');
      }
    }
  }

  hidePages() {
    if (!this.pages) return;
    const children = this.pages.children;
    for (let i = 0; i < children.length; i++) {
      children[i].classList.remove('active');
    }
  }

  openPanel(panelId) {
    if (!panelId) return;
    this.hidePages();
    const target = document.getElementById(panelId);
    if (target) {
      target.classList.add('active');
    }

    if (this.chatNav) {
      this.chatNav.querySelectorAll('button[data-panel]').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.panel === panelId);
      });
    }
  }

  onMouseMove(e) {
    if (e.clientX < 400) {
      this.chatNav.classList.add('chat-navigation-show');
    } else {
      this.chatNav.classList.remove('chat-navigation-show');
    }
    this.updateButtonZIndex();
  }

  handleResize() {
    if (window.innerWidth > 768) {
      document.addEventListener('mousemove', this.mouseMoveHandler);
    } else {
      document.removeEventListener('mousemove', this.mouseMoveHandler);
      this.chatNav.classList.remove('chat-navigation-show');
      this.updateButtonZIndex();
    }
  }
}

document.addEventListener('DOMContentLoaded', () => {
  ChatNavigationPanel.getInstance();
});