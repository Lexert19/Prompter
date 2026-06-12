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
                <label>Nazwa węzła (Node Name)</label>
                <input name="nodeName" id="nodeName" class="mb-05" placeholder="np. GPU-4090" required>

                <label>Nazwa modelu (Model Name)</label>
                <input name="modelName" id="modelName" class="mb-05" placeholder="np. mistral-7b-instruct" required>

                <label>Rodzina modelu (Model Family)</label>
                <input name="modelFamily" id="modelFamily" value="community" class="mb-05" required>

                <label class="checkbox-container mb-2">
                    <input type="checkbox" name="allowPublicUse" id="allowPublicUse" checked>
                    <span class="custom-checkbox"></span>
                    <span>Zezwól na użycie publiczne (Allow Public Use)</span>
                </label>

                <button class="ms-auto rounded-1" type="submit">Zarejestruj węzeł</button>
            </form>
        `;
    Modal.instance().open("Zarejestruj Community Node", html, (formData) => this.addCommunityNode(formData));
  }

  addCommunityNode(formData) {
    const dto = {
      nodeName: formData.get('nodeName'),
      modelName: formData.get('modelName'),
      modelFamily: formData.get('modelFamily'),
      allowPublicUse: formData.get('allowPublicUse') === 'on' || document.getElementById('allowPublicUse')?.checked === true
    };

    fetchWithCsrf('/api/nodes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dto)
    })
      .then(res => {
      if (!res.ok) throw new Error('Błąd rejestracji');
      return res.json();
    })
      .then(nodeData => {
      Modal.instance().close();
      this.showNodeTokenModal(nodeData);
      this.loadAllNodes();
    })
      .catch(error => {
      console.error(error);
      alert("Nie udało się zarejestrować węzła.");
    });
  }

  showNodeTokenModal(nodeData) {
    const html = `
            <div class="d-flex flex-column">
                <p class="text-success" style="font-weight: bold;">Węzeł zarejestrowany pomyślnie!</p>
                <p>Zapisz wygenerowany token autoryzacyjny (nie zostanie wyświetlony ponownie):</p>
                <input type="text" id="nodeTokenInput" value="${nodeData.authToken}" readonly class="mb-2 p-1"
                       style="background: #222; color: #fff; border: 1px solid #444; border-radius: 4px; width: 100%;">
                <button class="ms-auto rounded-1" type="button"
                        onclick="navigator.clipboard.writeText(document.getElementById('nodeTokenInput').value); alert('Skopiowano!');">
                    Skopiuj token
                </button>
            </div>
        `;
    Modal.instance().open("Zarejestrowano węzeł", html, null);
  }

  async loadAllNodes() {
    await Promise.all([this.loadMyNodes(), this.loadPublicNodes()]);
  }

  async loadMyNodes() {
    try {
      const response = await fetchWithCsrf('/api/nodes', { credentials: 'include' });
      if (!response.ok) throw new Error();
      this.nodes = await response.json();
      this.renderNodes();
    } catch (error) {
      console.error(error);
    }
  }

  async loadPublicNodes() {
    try {
      const response = await fetchWithCsrf('/api/nodes/public', { credentials: 'include' });
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
      html = '<p class="text-secondary">Brak zarejestrowanych węzłów.</p>';
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
                            Status: <span class="${statusClass}" style="font-weight: bold;">${node.status}</span> | Publiczny: ${node.allowPublicUse ? 'Tak' : 'Nie'}
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
      html = '<p class="text-secondary">Brak dostępnych węzłów publicznych.</p>';
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
                            Status: <span class="${statusClass}" style="font-weight: bold;">${node.status}</span> | Rodzina: ${node.modelFamily}
                        </div>
                    </div>
                </div>
            `;
    });
    publicNodesDiv.innerHTML = html;
  }

  async deleteNode(id) {
    if (!confirm("Czy usunąć ten węzeł?")) return;
    try {
      const response = await fetchWithCsrf(`/api/nodes/${id}`, { method: 'DELETE' });
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