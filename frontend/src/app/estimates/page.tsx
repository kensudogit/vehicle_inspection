"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";

type Estimate = {
  id: number;
  estimateNumber: string;
  totalAmount: number;
  status: string;
};

export default function EstimatesPage() {
  const [items, setItems] = useState<Estimate[]>([]);
  const [vehicleId, setVehicleId] = useState("");
  const [customerId, setCustomerId] = useState("");
  const [description, setDescription] = useState("車検基本料金");
  const [amount, setAmount] = useState("50000");

  const load = () => api<Estimate[]>("/estimates").then(setItems);
  useEffect(() => { load(); }, []);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    await api("/estimates", {
      method: "POST",
      body: JSON.stringify({
        vehicleId: Number(vehicleId),
        customerId: Number(customerId),
        items: [{ itemType: "LABOR", description, quantity: 1, unitPrice: Number(amount) }],
      }),
    });
    load();
  };

  return (
    <div>
      <h1 className="page-title">見積管理</h1>
      <form className="card" onSubmit={submit} style={{ marginBottom: "1rem" }}>
        <div className="grid grid-2">
          <div className="form-group"><label>車両ID</label><input value={vehicleId} onChange={(e) => setVehicleId(e.target.value)} required /></div>
          <div className="form-group"><label>顧客ID</label><input value={customerId} onChange={(e) => setCustomerId(e.target.value)} required /></div>
          <div className="form-group"><label>項目</label><input value={description} onChange={(e) => setDescription(e.target.value)} /></div>
          <div className="form-group"><label>金額（税抜）</label><input type="number" value={amount} onChange={(e) => setAmount(e.target.value)} /></div>
        </div>
        <button type="submit" className="btn btn-primary">見積作成</button>
      </form>
      <div className="card">
        <table>
          <thead><tr><th>見積番号</th><th>合計</th><th>状態</th><th></th></tr></thead>
          <tbody>
            {items.map((e) => (
              <tr key={e.id}>
                <td>{e.estimateNumber}</td>
                <td>{Number(e.totalAmount).toLocaleString()} 円</td>
                <td>{e.status}</td>
                <td>
                  <button type="button" className="btn" onClick={async () => {
                    await api(`/invoices/from-estimate/${e.id}`, { method: "POST" });
                    alert("請求書を作成しました");
                  }}>請求化</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
