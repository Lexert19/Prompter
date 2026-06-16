class HostedNodesView {
  static _instance = null;

  static instance() {
    if (!HostedNodesView._instance) {
      HostedNodesView._instance = new HostedNodesView();
    }
    return HostedNodesView._instance;
  }

  constructor() {
    if (HostedNodesView._instance) {
      return HostedNodesView._instance;
    }
    HostedNodesView._instance = this;
    this.nodes = [];
    this.publicNodes = [];
  }

  openRegisterNodeModal() {
    const html = `
            <form class="d-flex flex-column" id="communityNodeForm">
                <label>${t.t('nodes.nodeName')}</label>
                <input name="nodeName" id="nodeName" class="mb-05" placeholder="${t.t('nodes.nodeNamePlaceholder')}" required>

                <label>${t.t('nodes.modelName')}</label>
                <input name="modelName" id="modelName" class="mb-05" placeholder="${t.t('nodes.modelNamePlaceholder')}" required>

                <label>${t.t('nodes.modelFamily')}</label>
                <input name="modelFamily" id="modelFamily" value="community" class="mb-05" required>

                <label class="checkbox-container mb-2">
                    <input type="checkbox" name="allowPublicUse" id="allowPublicUse" checked>
                    <span class="custom-checkbox"></span>
                    <span>${t.t('nodes.allowPublicUse')}</span>
                </label>

                <button class="ms-auto rounded-1" type="submit">${t.t('nodes.registerNode')}</button>
            </form>
        `;
    Modal.instance().open(t.t('nodes.registerTitle'), html, (formData) => this.addCommunityNode(formData));
  }

  addCommunityNode(formData) {
    const dto = {
      nodeName: formData.get('nodeName'),
      modelName: formData.get('modelName'),
      modelFamily: formData.get('modelFamily'),
      allowPublicUse: formData.get('allowPublicUse') === 'on' || document.getElementById('allowPublicUse')?.checked === true
    };

    fetchWithAuth('/api/nodes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dto)
    })
      .then(res => {
      if (!res.ok) throw new Error(t.t('nodes.registerError'));
      return res.json();
    })
      .then(nodeData => {
      Modal.instance().close();
      this.showNodeTokenModal(nodeData);
      this.loadAllNodes();
    })
      .catch(error => {
      console.error(error);
      alert(t.t('nodes.registerFailed'));
    });
  }

  showNodeTokenModal(nodeData) {
    const html = `
            <div class="d-flex flex-column">
                <p class="text-success" style="font-weight: bold;">${t.t('nodes.registeredSuccess')}</p>
                <p>${t.t('nodes.saveToken')}</p>
                <input type="text" id="nodeTokenInput" value="${nodeData.authToken}" readonly class="mb-2 p-1"
                       style="background: #222; color: #fff; border: 1px solid #444; border-radius: 4px; width: 100%;">
                <button class="ms-auto rounded-1" type="button"
                        onclick="navigator.clipboard.writeText(document.getElementById('nodeTokenInput').value); alert(t.t('nodes.copied'));">
                    ${t.t('nodes.copyToken')}
                </button>
            </div>
        `;
    Modal.instance().open(t.t('nodes.registeredTitle'), html, null);

    setTimeout(() => {
      document.getElementById('copyNodeTokenBtn')?.addEventListener('click', () => {
        navigator.clipboard.writeText(nodeData.authToken);
        alert(t.t('nodes.copied'));
      });
    }, 0);
  }

  async loadAllNodes() {
    await Promise.all([this.loadMyNodes(), this.loadPublicNodes()]);
  }

  async loadMyNodes() {
    try {
      const response = await fetchWithAuth('/api/nodes', { credentials: 'include' });
      if (!response.ok) throw new Error();
      this.nodes = await response.json();
      this.renderNodes();
    } catch (error) {
      console.error(error);
    }
  }

  async loadPublicNodes() {
    try {
      const response = await fetchWithAuth('/api/nodes/public', { credentials: 'include' });
      if (!response.ok) throw new Error();
      this.publicNodes = await response.json();
      this.renderPublicNodes();
    } catch (error) {
      console.error(error);
    }
  }

  renderNodes() {
    const nodesDiv = document.getElementById('my-nodes-list');
    if (!nodesDiv) return;

    let html = '';
    if (this.nodes.length === 0) {
      html = `<p class="text-secondary">${t.t('nodes.noMyNodes')}</p>`;
      nodesDiv.innerHTML = html;
      return;
    }

    this.nodes.forEach(node => {
      const statusClass = node.status === 'ONLINE' ? 'text-success' : 'text-danger';
      html += `
                <div class="model-item d-flex justify-content-between align-items-center" style="margin-bottom: 10px; padding: 10px; border: 1px solid #444; border-radius: 5px;">
                    <div>
                        <span class="model-name"><strong>${node.nodeName}</strong></span> <small class="text-secondary">(${node.modelName})</small>
                        <div style="font-size: 0.8rem; margin-top: 5px;">
                            ${t.t('nodes.status')}: <span class="${statusClass}" style="font-weight: bold;">${node.status}</span> | ${t.t('nodes.public')}: ${node.allowPublicUse ? t.t('yes') : t.t('no')}
                       </div>
                        </div>
                    <div class="model-actions">
                        <button class="btn-delete" onclick="HostedNodesView.instance().deleteNode(${node.id})">
                            <i class="fas fa-trash-alt"></i>
                        </button>
                    </div>
                </div>
            `;
    });
    nodesDiv.innerHTML = html;
  }

  renderPublicNodes() {
    const publicNodesDiv = document.getElementById('public-nodes-list');
    if (!publicNodesDiv) return;

    let html = '';
    if (this.publicNodes.length === 0) {
      html = `<p class="text-secondary">${t.t('nodes.noPublicNodes')}</p>`;
      publicNodesDiv.innerHTML = html;
      return;
    }

    this.publicNodes.forEach(node => {
      const statusClass = node.status === 'ONLINE' ? 'text-success' : 'text-danger';
      html += `
                <div class="model-item d-flex justify-content-between align-items-center" style="margin-bottom: 10px; padding: 10px; border: 1px solid #444; border-radius: 5px; background-color: rgba(255, 255, 255, 0.02);">
                    <div>
                        <span class="model-name"><strong>${node.nodeName}</strong></span> <small class="text-secondary">(${node.modelName})</small>
                        <div style="font-size: 0.8rem; margin-top: 5px;">
                            ${t.t('nodes.status')}: <span class="${statusClass}" style="font-weight: bold;">${node.status}</span> | ${t.t('nodes.family')}: ${node.modelFamily}
                        </div>
                    </div>
                </div>
            `;
    });
    publicNodesDiv.innerHTML = html;
  }


  openNodesListModal() {
    const html = `
        <div class="nodes-container">
            <h4>${t.t('nodes.myNodes')}</h4>
            <div id="my-nodes-list">
                <p class="text-secondary">${t.t('nodes.loading')}</p>
            </div>

            <hr class="my-3" style="border-color: #444;">

            <h4>${t.t('nodes.publicNodes')}</h4>
            <div id="public-nodes-list">
                <p class="text-secondary">${t.t('nodes.loading')}</p>
            </div>
        </div>
    `;

    Modal.instance().open(t.t('nodes.listTitle'), html, null);

    this.loadAllNodes();
  }

  async deleteNode(id) {
    if (!confirm(t.t('nodes.confirmDelete'))) return;
    try {
      const response = await fetchWithAuth(`/api/nodes/${id}`, { method: 'DELETE' });
      if (response.ok) this.loadAllNodes();
    } catch (error) {
      console.error(error);
    }
  }
}

document.addEventListener('DOMContentLoaded', function() {
  const hnv = HostedNodesView.instance();
  hnv.loadAllNodes();
});