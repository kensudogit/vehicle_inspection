"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";

type AuditLog = {
  id: number;
  action: string;
  entityType?: string;
  entityId?: number;
  createdAt: string;
};

export default function AuditPage() {
  const [items, setItems] = useState<AuditLog[]>([]);

  useEffect(() => {
    api<AuditLog[]>("/audit-logs").then(setItems).catch(() => {});
  }, []);

  return (
    <div>
      <h1 className="page-title">操作履歴（監査ログ）</h1>
      <p style={{ color: "var(--muted)", marginBottom: "1rem", fontSize: "0.9rem" }}>
        全CRUD操作・ログイン・電子車検証取込・文書アップロードを記録。CloudWatch / Grafana 連携想定。
      </p>
      <div className="card">
        <table>
          <thead><tr><th>日時</th><th>操作</th><th>対象</th><th>ID</th></tr></thead>
          <tbody>
            {items.map((log) => (
              <tr key={log.id}>
                <td>{new Date(log.createdAt).toLocaleString("ja-JP")}</td>
                <td><span className="badge badge-ok">{log.action}</span></td>
                <td>{log.entityType || "—"}</td>
                <td>{log.entityId ?? "—"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
