// Shared helpers. All API calls are relative so the pages work behind
// Nginx, inside Docker and on any port.
async function api(method, url, body) {
  const options = { method, headers: {} };
  if (body !== undefined) {
    options.headers["Content-Type"] = "application/json";
    options.body = JSON.stringify(body);
  }
  const response = await fetch(url, options);
  const text = await response.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch (e) { data = text; }
  if (!response.ok) {
    const detail = data && data.errors && data.errors.length ? "\n- " + data.errors.join("\n- ") : "";
    const error = new Error(((data && data.message) || ("HTTP " + response.status)) + detail);
    error.status = response.status;
    throw error;
  }
  return data;
}

function showMessage(id, text, ok) {
  const el = document.getElementById(id);
  el.textContent = text;
  el.className = "msg show " + (ok ? "success" : "error");
}

function escapeHtml(value) {
  return String(value == null ? "" : value).replace(/[&<>"']/g, c =>
    ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}
