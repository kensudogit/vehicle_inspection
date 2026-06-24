"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";

type Customer = {
  id: number;
  customerCode: string;
  name: string;
  email?: string;
  phone?: string;
};

export default function CustomersPage() {
  const [items, setItems] = useState<Customer[]>([]);
  const [form, setForm] = useState({ name: "", email: "", phone: "" });
  const [error, setError] = useState<string | null>(null);

  const load = () => api<Customer[]>("/customers").then(setItems).catch((e) => setError(e.message));
  useEffect(() => { load(); }, []);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    await api("/customers", { method: "POST", body: JSON.stringify({ ...form, consentMarketing: false }) });
    setForm({ name: "", email: "", phone: "" });
    load();
  };

  return (
    <div>
      <h1 className="page-title">顧客管理</h1>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="grid grid-2">
        <form className="card" onSubmit={submit}>
          <h2 style={{ fontSize: "1rem", marginBottom: "0.75rem" }}>新規顧客</h2>
          <div className="form-group"><label>氏名</label><input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required /></div>
          <div className="form-group"><label>メール</label><input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></div>
          <div className="form-group"><label>電話</label><input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} /></div>
          <button type="submit" className="btn btn-primary">登録</button>
        </form>
        <div className="card">
          <table>
            <thead><tr><th>コード</th><th>氏名</th><th>連絡先</th></tr></thead>
            <tbody>
              {items.map((c) => (
                <tr key={c.id}><td>{c.customerCode}</td><td>{c.name}</td><td>{c.phone || c.email || "—"}</td></tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
