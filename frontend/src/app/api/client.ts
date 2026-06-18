// Cliente HTTP central do OdontoHub.
// Todas as chamadas usam o caminho relativo "/api/...", encaminhado pelo proxy do
// Vite para o backend Spring Boot (http://localhost:8080) — ver vite.config.ts.

const BASE = "/api";

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: body !== undefined ? { "Content-Type": "application/json" } : undefined,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    const texto = await res.text().catch(() => "");
    throw new Error(`API ${method} ${path} falhou (${res.status}): ${texto}`);
  }

  // Endpoints que retornam 204/202 sem corpo.
  if (res.status === 204 || res.status === 202) {
    return undefined as T;
  }
  const texto = await res.text();
  return texto ? (JSON.parse(texto) as T) : (undefined as T);
}

export const api = {
  get: <T>(path: string) => request<T>("GET", path),
  post: <T>(path: string, body?: unknown) => request<T>("POST", path, body),
  put: <T>(path: string, body?: unknown) => request<T>("PUT", path, body),
  del: <T>(path: string) => request<T>("DELETE", path),
};
