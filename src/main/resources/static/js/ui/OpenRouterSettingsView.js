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

    this.render();
    this.attachEvents();
    this.loadSettings();
  }

  render() {
    const settings = Settings.instance();
    this.container.innerHTML = `
            <div class="instruction-field">
                <label for="openRouterProviderOrder" data-i18n="settings.openRouterProviderOrder.label">
                    OpenRouter – kolejność dostawców
                </label>
                <input type="text" id="openRouterProviderOrder" name="openRouterProviderOrder"
                       class="form-control"
                       data-i18n-placeholder="settings.openRouterProviderOrder.placeholder"
                       placeholder="${t.t('settings.openRouterProviderOrder.placeholder')}"
                       value="${settings.openRouterProviderOrder || ''}">
            </div>
            <div class="instruction-field checkbox-container">
                <label class="checkbox-container">
                  <input type="checkbox" id="openRouterAllowFallbacks" name="openRouterAllowFallbacks"
                         ${settings.openRouterAllowFallbacks ? 'checked' : ''}>
                  <label for="openRouterAllowFallbacks" class="custom-checkbox"></label>
                  <span>${t.t('settings.openRouterAllowFallbacks.label')}</span>
                </label>
            </div>
        `;
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