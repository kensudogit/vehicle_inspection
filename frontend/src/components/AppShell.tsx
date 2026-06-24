"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { clearToken, getToken } from "@/lib/api";
import { UsageGuidePanel } from "@/components/UsageGuidePanel";

const NAV = [
  { href: "/", label: "ダッシュボード" },
  { href: "/customers", label: "顧客" },
  { href: "/vehicles", label: "車両" },
  { href: "/reservations", label: "予約" },
  { href: "/estimates", label: "見積" },
  { href: "/maintenance", label: "整備記録" },
  { href: "/invoices", label: "請求" },
  { href: "/notifications", label: "通知" },
  { href: "/electronic", label: "電子車検証" },
  { href: "/audit", label: "操作履歴" },
];

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const [ready, setReady] = useState(false);
  const isLogin = pathname === "/login";

  useEffect(() => {
    const token = getToken();
    if (!token && !isLogin) router.replace("/login");
    else setReady(true);
  }, [isLogin, router]);

  if (isLogin) return <>{children}</>;
  if (!ready) return null;

  return (
    <>
      <header className="app-header">
        <div className="container inner">
          <Link href="/" className="logo">車検管理</Link>
          <nav className="nav">
            {NAV.map((n) => (
              <Link key={n.href} href={n.href} className={pathname === n.href ? "active" : ""}>
                {n.label}
              </Link>
            ))}
            <button
              type="button"
              className="btn"
              onClick={() => { clearToken(); router.push("/login"); }}
            >
              ログアウト
            </button>
          </nav>
        </div>
      </header>
      <main><div className="container">{children}</div></main>
      <UsageGuidePanel />
    </>
  );
}
