"use client";

import { useState } from "react";
import { api } from "@/lib/api";

type Inspection = {
  id: number;
  inspectionDate: string;
  expiryDate: string;
  result: string;
  electronicCertId?: string;
};

export default function ElectronicPage() {
  const [vehicleId, setVehicleId] = useState("");
  const [certId, setCertId] = useState("CERT-2025-001");
  const [items, setItems] = useState<Inspection[]>([]);
  const [message, setMessage] = useState<string | null>(null);

  const load = () => {
    if (!vehicleId) return;
    api<Inspection[]>(`/electronic-inspections/vehicle/${vehicleId}`).then(setItems);
  };

  const importCert = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api("/electronic-inspections/import", {
        method: "POST",
        body: JSON.stringify({ vehicleId: Number(vehicleId), certId }),
      });
      setMessage("電子車検証データを取り込みました（登録番号・車台番号の照合済み）");
      load();
    } catch (err) {
      setMessage(err instanceof Error ? err.message : "取り込み失敗");
    }
  };

  return (
    <div>
      <h1 className="page-title">電子車検証連携</h1>
      <p style={{ color: "var(--muted)", marginBottom: "1rem", fontSize: "0.9rem" }}>
        電子車検証APIからデータ取得し、登録番号・車台番号の一致を検証して取り込みます。
      </p>
      {message && <div className="alert" style={{ background: "#dbeafe", marginBottom: "1rem" }}>{message}</div>}
      <form className="card" onSubmit={importCert} style={{ marginBottom: "1rem" }}>
        <div className="grid grid-2">
          <div className="form-group"><label>車両ID</label><input value={vehicleId} onChange={(e) => setVehicleId(e.target.value)} required /></div>
          <div className="form-group"><label>電子車検証ID</label><input value={certId} onChange={(e) => setCertId(e.target.value)} required /></div>
        </div>
        <div style={{ display: "flex", gap: "0.5rem" }}>
          <button type="submit" className="btn btn-primary">取り込み</button>
          <button type="button" className="btn" onClick={load}>履歴表示</button>
        </div>
      </form>
      <div className="card">
        <table>
          <thead><tr><th>検査日</th><th>満了日</th><th>結果</th><th>証明書ID</th></tr></thead>
          <tbody>
            {items.map((i) => (
              <tr key={i.id}>
                <td>{i.inspectionDate}</td>
                <td>{i.expiryDate}</td>
                <td>{i.result}</td>
                <td>{i.electronicCertId || "—"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
