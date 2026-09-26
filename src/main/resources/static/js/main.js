function switchCacheField() {
  const element = document.getElementById("cacheField");

  if (element.style.display === "none") {
    element.style.display = "block";
  } else {
    element.style.display = "none";
  }
}

function includeHTML(url, elementId) {
  fetchWithAuth(url)
    .then((response) => response.text())
    .then((data) => {
      document.getElementById(elementId).innerHTML = data;
    });
}

function createDateFromComponents(components) {
  return new Date(
    components[0],
    components[1] - 1,
    components[2],
    components[3],
    components[4],
    components[5],
    components[6] / 1000000
  );
}

function getMediaType(base64String) {
  if (base64String.startsWith("data:")) {
    const mediaType = base64String.split(";")[0].split(":")[1];
    return mediaType;
  } else {
    throw new Error("Invalid Base64 string");
  }
}

function getImageData(base64String) {
  return base64String.replace(/^data:image\/[a-zA-Z]+;base64,/, "");
}

function escapeHtml(content) {
  try {
    return content
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  } catch (error) {
    return "";
  }
}

const chatMessages = document.getElementById('chatMessages');
let shouldAutoScroll = true;




function base64ToFile(base64String, filename) {
  const arr = base64String.split(',');
  const mime = arr[0].match(/:(.*?);/)[1];
  const bstr = atob(arr[1]);
  let n = bstr.length;
  const u8arr = new Uint8Array(n);
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n);
  }
  return new File([u8arr], filename, { type: mime });
}

async function listSharedKeys() {
  try {
    const res = await fetchWithAuth('/api/admin/shared-keys');
    if (!res.ok) throw new Error(`Błąd ${res.status}`);
    const keys = await res.json();
    if (keys.length === 0) return console.log('Brak kluczy.');
    console.table(keys, ['id', 'provider', 'working', 'usageCount']);
  } catch (e) {
    console.error('Nie udało się pobrać listy:', e);
  }
}

async function deleteSharedKey(id) {
  if (!confirm(`Usunąć klucz ${id}?`)) return;
  try {
    const res = await fetchWithAuth(`/api/admin/shared-keys/${id}`, { method: 'DELETE' });
    if (res.status === 204) console.log(`Klucz ${id} usunięty.`);
    else if (res.status === 404) console.log(`Klucz ${id} nie istnieje.`);
    else throw new Error(`Błąd ${res.status}`);
  } catch (e) {
    console.error('Błąd usuwania:', e);
  }
}

async function deleteSharedKeyInteractive() {
  await listSharedKeys();
  const id = prompt('Podaj ID klucza do usunięcia:');
  if (id) await deleteSharedKey(parseInt(id));
}
