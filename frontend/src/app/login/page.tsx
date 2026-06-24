"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { login, setToken } from "@/lib/api";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("admin@vehicle-inspection.local");
  const [password, setPassword] = useState("admin123");
  const [mfaCode, setMfaCode] = useState("");
  const [mfaRequired, setMfaRequired] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await login(email, password, mfaCode || undefined);
      if (res.mfaRequired) {
        setMfaRequired(true);
        return;
      }
      if (res.token) {
        setToken(res.token);
        router.push("/");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "ログインに失敗しました");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <form className="login-card" onSubmit={handleSubmit}>
        <h1>車検・整備管理システム</h1>
        {error && <div className="alert alert-error">{error}</div>}
        <div className="form-group">
          <label>メールアドレス</label>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </div>
        <div className="form-group">
          <label>パスワード</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </div>
        {mfaRequired && (
          <div className="form-group">
            <label>MFA コード</label>
            <input value={mfaCode} onChange={(e) => setMfaCode(e.target.value)} placeholder="6桁" />
          </div>
        )}
        <button type="submit" className="btn btn-primary" style={{ width: "100%", marginTop: "0.5rem" }} disabled={loading}>
          {loading ? "認証中..." : "ログイン"}
        </button>
      </form>
    </div>
  );
}
