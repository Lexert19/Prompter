class AdvancedOptionsView {
  static _instance = null;

  static instance(containerId = 'advancedOptionsContainer') {
    if (!AdvancedOptionsView._instance) {
      AdvancedOptionsView._instance = new AdvancedOptionsView(containerId);
    }
    return AdvancedOptionsView._instance;
  }

  constructor(containerId) {
    if (AdvancedOptionsView._instance) {
      return AdvancedOptionsView._instance;
    }
    AdvancedOptionsView._instance = this;

    this.container = document.getElementById(containerId);
    if (!this.container) return;

    this.render();
    this.attachEvents();
  }

  render() {
    const enabled = Settings.instance().includeAdvancedParams || false;

    this.container.innerHTML = `
            <div class="instruction-field">
                <label class="checkbox-container">
                    <input type="checkbox" id="includeAdvancedParams" ${enabled ? 'checked' : ''}>
                    <span class="custom-checkbox"></span>
                    <span>${t.t('advancedParams')}</span>
                </label>
            </div>
        `;
  }

  attachEvents() {
    const checkbox = document.getElementById('includeAdvancedParams');
    if (!checkbox) return;

    checkbox.addEventListener('change', (e) => {
      Settings.instance().includeAdvancedParams = e.target.checked;
      Settings.instance().save();
    });
  }
}

document.addEventListener('i18n:ready', () => {
  AdvancedOptionsView.instance('advancedOptionsContainer');
});