class OpenRouterSettingsView {
  static _instance = null;

  static instance(containerId = 'openRouterSettingsContainer') {
    if (!OpenRouterSettingsView._instance) {
      OpenRouterSettingsView._instance = new OpenRouterSettingsView(containerId);
    }
    return OpenRouterSettingsView._instance;
  }

  constructor(containerId) {
    if (OpenRouterSettingsView._instance) {
      return OpenRouterSettingsView._instance;
    }
    OpenRouterSettingsView._instance = this;

    this.container = document.getElementById(containerId);
    if (!this.container) return;

    this.attachEvents();
    this.loadSettings();
  }

  attachEvents() {
    const orderInput = document.getElementById('openRouterProviderOrder');
    const fallbackCheck = document.getElementById('openRouterAllowFallbacks');

    if (orderInput) {
      orderInput.addEventListener('input', (e) => {
        Settings.instance().openRouterProviderOrder = e.target.value;
        Settings.instance().save();
      });
    }

    if (fallbackCheck) {
      fallbackCheck.addEventListener('change', (e) => {
        Settings.instance().openRouterAllowFallbacks = e.target.checked;
        Settings.instance().save();
      });
    }
  }

  loadSettings() {
    const settings = Settings.instance();
    const orderInput = document.getElementById('openRouterProviderOrder');
    const fallbackCheck = document.getElementById('openRouterAllowFallbacks');

    if (orderInput) orderInput.value = settings.openRouterProviderOrder || '';
    if (fallbackCheck) fallbackCheck.checked = settings.openRouterAllowFallbacks !== false;
  }
}

document.addEventListener('i18n:ready', () => {
  OpenRouterSettingsView.instance('openRouterSettingsContainer');
});