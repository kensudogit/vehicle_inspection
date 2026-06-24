"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";

type Record = {
  id: number;
  vehicleId: number;
  workType: string;
  description: string;
  contentHash: string;
  locked: boolean;
};

export default function MaintenancePage() {
  const [items, setItems] = useState<Record[]>([]);
  const [vehicleId, setVehicleId] = useState("");
  const [workType, setWorkType] = useState("INSPECTION");
  const [description, setDescription] = useState("");

  const load = () => {
    if (!vehicleId) return;
    api<Record[]>(`/maintenance/vehicle/${vehicleId}`).then(setItems);
  };

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    await api("/maintenance", {
      method: "POST",
      body: JSON.stringify({
        vehicleId: Number(vehicleId),
        performedAt: new Date().toISOString(),
        workType,
        description,
      }),
    });
    setDescription("");
    load();
  };

  const lock = async (id: number) => {
    await api(`/maintenance/${id}/lock`, { method: "POST" });
    load();
  };

  const verify = async (id: number) => {
    const res = await api<{ valid: boolean }>(`/maintenance/${id}/verify`);
    alert(res.valid ? "改ざんなし（ハッシュ検証OK）" : "改ざんの可能性あり");
  };

  return (
    <div>
      <h1 className="page-title">整備記録（改ざん防止）</h1>
      <p style={{ color: "var(--muted)", marginBottom: "1rem", fontSize: "0.9rem" }}>
        各記録は SHA-256 ハッシュチェーンで保護。ロック後は編集不可。
      </p>
      <form className="card" onSubmit={submit} style={{ marginBottom: "1rem" }}>
        <div className="grid grid-2">
          <div className="form-group"><label>車両ID</label><input value={vehicleId} onChange={(e) => setVehicleId(e.target.value)} required /></div>
          <div className="form-group">
            <label>作業種別</label>
            <select value={workType} onChange={(e) => setWorkType(e.target.value)}>
              <option value="INSPECTION">車検</option>
              <option value="OIL_CHANGE">オイル交換</option>
              <option value="BRAKE">ブレーキ</option>
            </select>
          </div>
        </div>
        <div className="form-group"><label>作業内容</label><textarea rows={3} value={description} onChange={(e) => setDescription(e.target.value)} required /></div>
        <div style={{ display: "flex", gap: "0.5rem" }}>
          <button type="submit" className="btn btn-primary">記録追加</button>
          <button type="button" className="btn" onClick={load}>一覧表示</button>
        </div>
      </form>
      <div className="card">
        <table>
          <thead><tr><th>ID</th><th>種別</th><th>内容</th><th>ハッシュ</th><th>操作</th></tr></thead>
          <tbody>
            {items.map((r) => (
              <tr key={r.id}>
                <td>{r.id}</td>
                <td>{r.workType}</td>
                <td>{r.description.slice(0, 40)}</td>
                <td style={{ fontFamily: "monospace", fontSize: "0.7rem" }}>{r.contentHash.slice(0, 12)}…</td>
                <td>
                  {!r.locked && <button type="button" className="btn" onClick={() => lock(r.id)}>ロック</button>}
                  <button type="button" className="btn" onClick={() => verify(r.id)}>検証</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
