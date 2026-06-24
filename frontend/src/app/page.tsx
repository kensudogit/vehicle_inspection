"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";

type Vehicle = { id: number; registrationNumber: string; inspectionExpiry: string };
type Notification = { id: number; status: string; notificationType: string };

export default function DashboardPage() {
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);

  useEffect(() => {
    api<Vehicle[]>("/vehicles").then(setVehicles).catch(() => {});
    api<Notification[]>("/notifications").then(setNotifications).catch(() => {});
  }, []);

  const expiringSoon = vehicles.filter((v) => {
    const days = Math.ceil((new Date(v.inspectionExpiry).getTime() - Date.now()) / 86400000);
    return days >= 0 && days <= 30;
  });

  return (
    <div>
      <h1 className="page-title">ダッシュボード</h1>
      <div className="grid grid-3" style={{ marginBottom: "1.5rem" }}>
        <div className="stat-card">
          <strong>{vehicles.length}</strong>
          <span>登録車両</span>
        </div>
        <div className="stat-card">
          <strong>{expiringSoon.length}</strong>
          <span>30日以内に車検満了</span>
        </div>
        <div className="stat-card">
          <strong>{notifications.filter((n) => n.status === "PENDING").length}</strong>
          <span>未送信通知</span>
        </div>
      </div>

      <div className="card">
        <h2 style={{ fontSize: "1rem", marginBottom: "0.75rem" }}>車検満了間近の車両</h2>
        {expiringSoon.length === 0 ? (
          <p className="empty">該当なし</p>
        ) : (
          <table>
            <thead><tr><th>登録番号</th><th>満了日</th></tr></thead>
            <tbody>
              {expiringSoon.map((v) => (
                <tr key={v.id}>
                  <td>{v.registrationNumber}</td>
                  <td><span className="badge badge-warn">{v.inspectionExpiry}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
