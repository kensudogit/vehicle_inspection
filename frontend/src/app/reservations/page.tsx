"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";

type Reservation = {
  id: number;
  vehicleId: number;
  customerId: number;
  reservedAt: string;
  status: string;
  serviceType: string;
};

type Vehicle = { id: number; registrationNumber: string };
type Customer = { id: number; name: string };

export default function ReservationsPage() {
  const [items, setItems] = useState<Reservation[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [form, setForm] = useState({ vehicleId: "", customerId: "", reservedAt: "", serviceType: "INSPECTION" });

  const load = () => api<Reservation[]>("/reservations").then(setItems);
  useEffect(() => {
    load();
    api<Vehicle[]>("/vehicles").then(setVehicles);
    api<Customer[]>("/customers").then(setCustomers);
  }, []);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    await api("/reservations", {
      method: "POST",
      body: JSON.stringify({
        vehicleId: Number(form.vehicleId),
        customerId: Number(form.customerId),
        reservedAt: new Date(form.reservedAt).toISOString(),
        serviceType: form.serviceType,
      }),
    });
    load();
  };

  return (
    <div>
      <h1 className="page-title">予約管理</h1>
      <form className="card" onSubmit={submit} style={{ marginBottom: "1rem" }}>
        <div className="grid grid-2">
          <div className="form-group">
            <label>車両</label>
            <select value={form.vehicleId} onChange={(e) => setForm({ ...form, vehicleId: e.target.value })} required>
              <option value="">選択</option>
              {vehicles.map((v) => <option key={v.id} value={v.id}>{v.registrationNumber}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>顧客</label>
            <select value={form.customerId} onChange={(e) => setForm({ ...form, customerId: e.target.value })} required>
              <option value="">選択</option>
              {customers.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>予約日時</label>
            <input type="datetime-local" value={form.reservedAt} onChange={(e) => setForm({ ...form, reservedAt: e.target.value })} required />
          </div>
          <div className="form-group">
            <label>サービス種別</label>
            <select value={form.serviceType} onChange={(e) => setForm({ ...form, serviceType: e.target.value })}>
              <option value="INSPECTION">車検</option>
              <option value="MAINTENANCE">点検整備</option>
            </select>
          </div>
        </div>
        <button type="submit" className="btn btn-primary">予約登録</button>
      </form>
      <div className="card">
        <table>
          <thead><tr><th>ID</th><th>日時</th><th>種別</th><th>状態</th></tr></thead>
          <tbody>
            {items.map((r) => (
              <tr key={r.id}>
                <td>{r.id}</td>
                <td>{new Date(r.reservedAt).toLocaleString("ja-JP")}</td>
                <td>{r.serviceType}</td>
                <td><span className="badge badge-ok">{r.status}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
