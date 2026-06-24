"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";

type Notification = {
  id: number;
  channel: string;
  notificationType: string;
  recipient: string;
  status: string;
  subject?: string;
  sentAt?: string;
};

export default function NotificationsPage() {
  const [items, setItems] = useState<Notification[]>([]);

  const load = () => api<Notification[]>("/notifications").then(setItems);
  useEffect(() => { load(); }, []);

  const runCheck = async () => {
    await api("/notifications/check-expiry", { method: "POST" });
    await api("/notifications/process", { method: "POST" });
    load();
  };

  return (
    <div>
      <h1 className="page-title">通知（メール・SMS）</h1>
      <p style={{ color: "var(--muted)", marginBottom: "1rem", fontSize: "0.9rem" }}>
        車検満了日の30/14/7/1日前に自動通知（毎朝8時スケジュール + 手動実行可）
      </p>
      <button type="button" className="btn btn-primary" style={{ marginBottom: "1rem" }} onClick={runCheck}>
        満了チェック＆送信実行
      </button>
      <div className="card">
        <table>
          <thead><tr><th>チャネル</th><th>種別</th><th>宛先</th><th>状態</th><th>送信日時</th></tr></thead>
          <tbody>
            {items.map((n) => (
              <tr key={n.id}>
                <td>{n.channel}</td>
                <td>{n.notificationType}</td>
                <td>{n.recipient}</td>
                <td><span className={`badge ${n.status === "SENT" ? "badge-ok" : n.status === "FAILED" ? "badge-danger" : "badge-warn"}`}>{n.status}</span></td>
                <td>{n.sentAt ? new Date(n.sentAt).toLocaleString("ja-JP") : "—"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
