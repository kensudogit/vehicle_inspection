const API_BASE = typeof window !== "undefined" ? "" : process.env.API_URL || "http://localhost:8080";

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("vi_token");
}

export function setToken(token: string) {
  localStorage.setItem("vi_token", token);
}

export function clearToken() {
  localStorage.removeItem("vi_token");
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string>),
  };
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}/api${path}`, { ...options, headers });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message || "API error");
  }
  if (res.status === 204) return undefined as T;
  return res.json();
}

export type AuthResponse = {
  token?: string;
  userId?: number;
  email?: string;
  fullName?: string;
  roles?: string[];
  mfaRequired?: boolean;
};

export async function login(email: string, password: string, mfaCode?: string) {
  return api<AuthResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password, mfaCode }),
  });
}
