// API client for backend REST.
//
// Важно для dev в Docker:
// - браузер НЕ знает DNS-имя docker-сервиса "app"
// - поэтому из браузера нужно ходить относительным URL (/orders),
//   а Vite dev-server проксирует запросы на backend.
//
// Для этого клиент ниже всегда использует относительные пути.

async function request(path, options = {}) {
  const res = await fetch(path, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    },
    ...options
  });

  const text = await res.text();
  let body;
  try { body = text ? JSON.parse(text) : null; } catch { body = text; }

  if (!res.ok) {
    const message = body?.message || body?.error || (typeof body === 'string' ? body : 'Request failed');
    const err = new Error(message);
    err.status = res.status;
    err.body = body;
    throw err;
  }
  return body;
}

export const OrdersApi = {
  listOrders: () => request('/orders', { method: 'GET' }),
  createOrder: (payload) => request('/orders', { method: 'POST', body: JSON.stringify(payload) }),
  getOrder: (id) => request(`/orders/${id}`, { method: 'GET' }),
  changeStatus: (id, status) => request(`/orders/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),
};
