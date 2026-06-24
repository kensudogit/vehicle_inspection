"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";

type Customer = { id: number; name: string };
type Vehicle = {
  id: number;
  customerId: number;
  registrationNumber: string;
  chassisNumber: string;
  maker?: string;
  model?: string;
  inspectionExpiry: string;
};

export default function VehiclesPage() {
  const [items, setItems] = useState<Vehicle[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [form, setForm] = useState({
    customerId: "",
    registrationNumber: "",
    chassisNumber: "",
    maker: "",
    model: "",
    inspectionExpiry: "",
  });
  const [validation, setValidation] = useState<{ registrationValid?: boolean; chassisValid?: boolean }>({});

  const load = () => api<Vehicle[]>("/vehicles").then(setItems);
  useEffect(() => {
    load();
    api<Customer[]>("/customers").then(setCustomers);
  }, []);

  const validate = async () => {
    const res = await api<{ registrationValid: boolean; chassisValid: boolean }>("/vehicles/validate", {
      method: "POST",
      body: JSON.stringify({
        customerId: Number(form.customerId) || 1,
        registrationNumber: form.registrationNumber,
        chassisNumber: form.chassisNumber,
        inspectionExpiry: form.inspectionExpiry || "2026-12-31",
      }),
    });
    setValidation(res);
  };

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    await api("/vehicles", {
      method: "POST",
      body: JSON.stringify({
        ...form,
        customerId: Number(form.customerId),
        mileage: 0,
      }),
    });
    setForm({ customerId: "", registrationNumber: "", chassisNumber: "", maker: "", model: "", inspectionExpiry: "" });
    load();
  };

  return (
    <div>
      <h1 className="page-title">車両管理</h1>
      <div className="grid grid-2">
        <form className="card" onSubmit={submit}>
          <h2 style={{ fontSize: "1rem", marginBottom: "0.75rem" }}>車両登録</h2>
          <div className="form-group">
            <label>顧客</label>
            <select value={form.customerId} onChange={(e) => setForm({ ...form, customerId: e.target.value })} required>
              <option value="">選択</option>
              {customers.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>登録番号</label>
            <input value={form.registrationNumber} onChange={(e) => setForm({ ...form, registrationNumber: e.target.value })} placeholder="品川500あ1234" required />
            {validation.registrationValid === false && <span className="badge badge-danger">形式エラー</span>}
            {validation.registrationValid === true && <span className="badge badge-ok">OK</span>}
          </div>
          <div className="form-group">
            <label>車台番号（17桁）</label>
            <input value={form.chassisNumber} onChange={(e) => setForm({ ...form, chassisNumber: e.target.value })} required />
            {validation.chassisValid === false && <span className="badge badge-danger">形式エラー</span>}
            {validation.chassisValid === true && <span className="badge badge-ok">OK</span>}
          </div>
          <div className="form-group"><label>メーカー</label><input value={form.maker} onChange={(e) => setForm({ ...form, maker: e.target.value })} /></div>
          <div className="form-group"><label>車種</label><input value={form.model} onChange={(e) => setForm({ ...form, model: e.target.value })} /></div>
          <div className="form-group"><label>車検満了日</label><input type="date" value={form.inspectionExpiry} onChange={(e) => setForm({ ...form, inspectionExpiry: e.target.value })} required /></div>
          <div style={{ display: "flex", gap: "0.5rem" }}>
            <button type="button" className="btn" onClick={validate}>入力チェック</button>
            <button type="submit" className="btn btn-primary">登録</button>
          </div>
        </form>
        <div className="card">
          <table>
            <thead><tr><th>登録番号</th><th>車台番号</th><th>満了日</th></tr></thead>
            <tbody>
              {items.map((v) => (
                <tr key={v.id}>
                  <td>{v.registrationNumber}</td>
                  <td style={{ fontFamily: "monospace", fontSize: "0.8rem" }}>{v.chassisNumber}</td>
                  <td>{v.inspectionExpiry}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
