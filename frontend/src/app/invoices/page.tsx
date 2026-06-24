"use client";

import { useEffect, useState } from "react";
import { api, getToken } from "@/lib/api";

type Invoice = {
  id: number;
  invoiceNumber: string;
  totalAmount: number;
  status: string;
};

export default function InvoicesPage() {
  const [items, setItems] = useState<Invoice[]>([]);

  const load = () => api<Invoice[]>("/invoices").then(setItems);
  useEffect(() => { load(); }, []);

  const download = async (id: number, type: "pdf" | "excel") => {
    const token = getToken();
    const res = await fetch(`/api/invoices/${id}/${type}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `invoice-${id}.${type === "pdf" ? "pdf" : "xlsx"}`;
    a.click();
  };

  const pay = async (id: number, amount: number) => {
    await api(`/invoices/${id}/payments`, {
      method: "POST",
      body: JSON.stringify({ amount, paymentMethod: "CASH" }),
    });
    load();
  };

  return (
    <div>
      <h1 className="page-title">請求・帳票</h1>
      <div className="card">
        <table>
          <thead><tr><th>請求番号</th><th>合計</th><th>状態</th><th>帳票</th><th>入金</th></tr></thead>
          <tbody>
            {items.map((inv) => (
              <tr key={inv.id}>
                <td>{inv.invoiceNumber}</td>
                <td>{Number(inv.totalAmount).toLocaleString()} 円</td>
                <td><span className={`badge ${inv.status === "PAID" ? "badge-ok" : "badge-warn"}`}>{inv.status}</span></td>
                <td>
                  <button type="button" className="btn" onClick={() => download(inv.id, "pdf")}>PDF</button>
                  <button type="button" className="btn" onClick={() => download(inv.id, "excel")}>Excel</button>
                </td>
                <td>
                  {inv.status !== "PAID" && (
                    <button type="button" className="btn btn-primary" onClick={() => pay(inv.id, Number(inv.totalAmount))}>入金</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
