"use client";

import { useCallback, useEffect, useRef, useState } from "react";

const STORAGE_KEY = "vi-usage-guide-v1";
const PANEL_WIDTH = 440;

type GuideStep = { title: string; body: string; items?: readonly string[] };
type FeaturedBlock = {
  badge: string;
  title: string;
  body: string;
  items?: readonly string[];
  variant?: "architecture" | "workflow" | "security" | "default";
};

const architectureFeatured: FeaturedBlock = {
  badge: "Architecture",
  title: "Next.js + Spring Boot 統合構成",
  body:
    "Railway 上では Next.js が公開 PORT で待受け、/api/* を内部 Spring Boot にプロキシします。PostgreSQL に業務データを永続化し、帳票は PDF / Excel で出力します。",
  variant: "architecture",
  items: [
    "Next.js — 業務画面（顧客・車両・予約・見積・整備・請求 等）",
    "Spring Boot :8081 — REST API · JWT 認証 · Flyway マイグレーション",
    "PostgreSQL — 顧客 / 車両 / 整備記録 / 請求 / 監査ログ",
    "/api/health — 生存確認（Railway ヘルスチェック）",
  ],
};

const workflowFeatured: FeaturedBlock = {
  badge: "Workflow",
  title: "標準業務フロー（受付〜請求）",
  body: "車検・整備の一連の流れ。左メニューから各画面へ遷移し、下記の順序でデータを登録していきます。",
  variant: "workflow",
  items: [
    "① 顧客 — 新規顧客を登録（氏名・連絡先）",
    "② 車両 — 登録番号・車台番号を入力（形式検証あり）",
    "③ 予約 — 車検・点検の予約日時を登録",
    "④ 見積 — 作業内容と金額を見積作成",
    "⑤ 整備記録 — 作業完了後に記録（改ざん防止ハッシュ付き）",
    "⑥ 請求 — 見積から請求書作成 → PDF/Excel → 入金登録",
  ],
};

const archDiagram = `Browser（工場スタッフ）
    │ HTTPS
    ▼
Next.js :PORT（Railway 公開）
    ├─ /              ダッシュボード
    ├─ /customers     顧客管理
    ├─ /vehicles      車両管理
    ├─ /reservations  予約
    ├─ /estimates     見積
    ├─ /maintenance   整備記録
    ├─ /invoices      請求・帳票
    ├─ /notifications 満了通知
    ├─ /electronic    電子車検証
    ├─ /audit         操作履歴
    └─ /api/* ──proxy──► Spring Boot :8081
              └─ PostgreSQL`;

const guideSections: { label: string; steps: readonly GuideStep[] }[] = [
  {
    label: "はじめに",
    steps: [
      {
        title: "ログイン",
        body: "初回ログイン情報（本番では必ず変更してください）。",
        items: [
          "メール: admin@vehicle-inspection.local",
          "パスワード: admin123",
          "MFA 有効時 — 6 桁コード入力後にダッシュボードへ",
        ],
      },
      {
        title: "画面の見方",
        body: "画面上部のナビゲーションで各機能に移動します。本パネルは全画面で表示されます。",
        items: [
          "ヘッダー左 — 「車検管理」ロゴ（ダッシュボードへ）",
          "ヘッダー右 — 各メニュー + ログアウト",
          "本パネル — ドラッグで移動 · ▼▲ で開閉 · 位置は自動保存",
          "閉じたとき — 右下の「利用手順」ボタンで再表示",
        ],
      },
    ],
  },
  {
    label: "画面別操作",
    steps: [
      {
        title: "ダッシュボード（/）",
        body: "業務の概要を一目で確認します。",
        items: [
          "登録車両数 · 30 日以内車検満了 · 未送信通知の件数",
          "満了間近車両テーブル — 優先対応が必要な車両を確認",
        ],
      },
      {
        title: "顧客管理（/customers）",
        body: "左フォームで新規登録、右テーブルで一覧確認。",
        items: ["氏名（必須）· メール · 電話を入力 →「登録」", "顧客コードは自動採番"],
      },
      {
        title: "車両管理（/vehicles）",
        body: "登録番号・車台番号は形式検証と重複チェックが行われます。",
        items: [
          "登録番号 — 例: 品川 500 あ 12-34 形式",
          "車台番号 — 17 桁 VIN 等",
          "車検満了日 — 通知スケジュールの基準日",
        ],
      },
      {
        title: "予約・見積",
        body: "予約登録後、見積画面で作業内容と金額を入力します。",
        items: [
          "/reservations — 車両・日時・作業種別を登録",
          "/estimates — 見積明細を作成（後続の請求の元データ）",
        ],
      },
      {
        title: "整備記録（/maintenance）",
        body: "作業内容を記録。SHA-256 ハッシュチェーンで改ざんを検知します。",
        items: [
          "記録作成後はロック可能（編集制限）",
          "監査・コンプライアンス対応の証跡として利用",
        ],
      },
      {
        title: "請求・帳票（/invoices）",
        body: "見積から生成した請求書の一覧。帳票出力と入金登録を行います。",
        items: [
          "PDF — 請求書を PDF ダウンロード",
          "Excel — 帳票を Excel ダウンロード",
          "入金 — 未払い請求に「入金」ボタンで支払い登録",
          "データが空の場合 — 先に見積 → 請求作成フローを実施",
        ],
      },
      {
        title: "通知・電子車検証・監査",
        body: "付帯機能の概要です。",
        items: [
          "/notifications — 車検満了 30/14/7/1 日前の通知管理",
          "/electronic — 電子車検証データの取込・照合",
          "/audit — 操作履歴（誰が・いつ・何をしたか）",
        ],
      },
    ],
  },
  {
    label: "運用・トラブルシュート",
    steps: [
      {
        title: "本番環境（Railway）",
        body: "デプロイ済み URL での確認手順です。",
        items: [
          "DATABASE_URL — Postgres の Reference 変数を設定",
          "JWT_SECRET — 32 文字以上のランダム文字列",
          "/api/health — 200 OK を確認",
          "ログインできない — 環境変数・DB マイグレーションを確認",
        ],
      },
      {
        title: "よくある質問",
        body: "画面が空・エラーになる場合の確認ポイント。",
        items: [
          "請求一覧が空 — 見積から請求を作成していない",
          "401 エラー — 再ログイン（トークン期限切れ）",
          "車両登録エラー — 登録番号形式または重複を確認",
        ],
      },
    ],
  },
];

type SavedState = { x: number; y: number; expanded: boolean };

function defaultPosition() {
  if (typeof window === "undefined") return { x: 24, y: 24 };
  return {
    x: Math.max(16, window.innerWidth - PANEL_WIDTH - 24),
    y: Math.max(72, window.innerHeight - 520),
  };
}

function clampPosition(x: number, y: number, width: number, height: number) {
  const maxX = Math.max(8, window.innerWidth - width - 8);
  const maxY = Math.max(8, window.innerHeight - height - 8);
  return {
    x: Math.min(Math.max(8, x), maxX),
    y: Math.min(Math.max(8, y), maxY),
  };
}

function FeaturedSection({ block }: { block: FeaturedBlock }) {
  const variant = block.variant ?? "default";
  return (
    <section className={`usage-guide-featured usage-guide-featured--${variant}`} aria-label={block.title}>
      <div className="usage-guide-featured-head">
        <span className="usage-guide-featured-badge">{block.badge}</span>
        <strong>{block.title}</strong>
      </div>
      <p>{block.body}</p>
      {block.items?.length ? (
        <ul className="usage-guide-items">
          {block.items.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      ) : null}
    </section>
  );
}

export function UsageGuidePanel() {
  const panelRef = useRef<HTMLDivElement>(null);
  const dragRef = useRef<{
    pointerId: number;
    startX: number;
    startY: number;
    originX: number;
    originY: number;
  } | null>(null);

  const [ready, setReady] = useState(false);
  const [expanded, setExpanded] = useState(true);
  const [pos, setPos] = useState({ x: 24, y: 24 });
  const [dragging, setDragging] = useState(false);

  useEffect(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      try {
        const parsed = JSON.parse(saved) as SavedState;
        setPos({ x: parsed.x, y: parsed.y });
        setExpanded(parsed.expanded);
      } catch {
        setPos(defaultPosition());
      }
    } else {
      setPos(defaultPosition());
    }
    setReady(true);
  }, []);

  useEffect(() => {
    if (!ready) return;
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ ...pos, expanded }));
  }, [pos, expanded, ready]);

  useEffect(() => {
    if (!ready) return;
    const onResize = () => {
      const el = panelRef.current;
      if (!el) return;
      setPos((c) => clampPosition(c.x, c.y, el.offsetWidth, el.offsetHeight));
    };
    window.addEventListener("resize", onResize);
    return () => window.removeEventListener("resize", onResize);
  }, [ready]);

  const onHeaderPointerDown = useCallback(
    (e: React.PointerEvent<HTMLElement>) => {
      if ((e.target as HTMLElement).closest(".usage-guide-toggle")) return;
      dragRef.current = {
        pointerId: e.pointerId,
        startX: e.clientX,
        startY: e.clientY,
        originX: pos.x,
        originY: pos.y,
      };
      setDragging(true);
      e.currentTarget.setPointerCapture(e.pointerId);
    },
    [pos.x, pos.y],
  );

  const onHeaderPointerMove = useCallback((e: React.PointerEvent<HTMLElement>) => {
    const drag = dragRef.current;
    if (!drag || drag.pointerId !== e.pointerId) return;
    const el = panelRef.current;
    setPos(
      clampPosition(
        drag.originX + (e.clientX - drag.startX),
        drag.originY + (e.clientY - drag.startY),
        el?.offsetWidth ?? PANEL_WIDTH,
        el?.offsetHeight ?? 120,
      ),
    );
  }, []);

  const onHeaderPointerUp = useCallback((e: React.PointerEvent<HTMLElement>) => {
    const drag = dragRef.current;
    if (!drag || drag.pointerId !== e.pointerId) return;
    dragRef.current = null;
    setDragging(false);
    e.currentTarget.releasePointerCapture(e.pointerId);
  }, []);

  if (!ready) return null;

  return (
    <>
      {!expanded && (
        <button
          type="button"
          className="usage-guide-launcher"
          onClick={() => setExpanded(true)}
          aria-label="利用手順を開く"
        >
          利用手順
        </button>
      )}

      <div
        ref={panelRef}
        className={`usage-guide-panel${expanded ? " is-expanded" : " is-collapsed"}${dragging ? " is-dragging" : ""}`}
        style={{ left: pos.x, top: pos.y, width: PANEL_WIDTH, display: expanded ? undefined : "none" }}
        role="dialog"
        aria-label="利用手順"
        aria-modal="false"
      >
        <header
          className="usage-guide-header"
          onPointerDown={onHeaderPointerDown}
          onPointerMove={onHeaderPointerMove}
          onPointerUp={onHeaderPointerUp}
          onPointerCancel={onHeaderPointerUp}
        >
          <div className="usage-guide-header-text">
            <span className="usage-guide-drag-icon" aria-hidden>☰</span>
            <div className="usage-guide-header-titles">
              <strong>利用手順</strong>
              <span className="usage-guide-header-sub">User Guide</span>
            </div>
            <span className="usage-guide-drag-hint">ドラッグで移動</span>
          </div>
          <button
            type="button"
            className="usage-guide-toggle"
            aria-label="閉じる"
            aria-expanded={expanded}
            onClick={() => setExpanded(false)}
          >
            ▼
          </button>
        </header>

        <div className="usage-guide-body">
          <div className="usage-guide-hero">
            <p className="usage-guide-hero-kicker">Vehicle Inspection System</p>
            <h2 className="usage-guide-hero-title">車検・整備管理システム</h2>
            <p className="usage-guide-hero-lead">
              顧客・車両・予約・見積・整備・請求・通知を一元管理。コンプライアンス対応の監査ログ付き。
            </p>
            <div className="usage-guide-stack">
              {["Java 21 · Spring Boot", "Next.js 15", "PostgreSQL", "JWT + MFA", "PDF / Excel", "Railway"].map(
                (tag) => (
                  <span key={tag} className="usage-guide-stack-pill">{tag}</span>
                ),
              )}
            </div>
          </div>

          <FeaturedSection block={architectureFeatured} />

          <figure className="usage-guide-diagram" aria-label="Service topology">
            <figcaption>Service topology</figcaption>
            <pre>{archDiagram}</pre>
          </figure>

          <FeaturedSection block={workflowFeatured} />

          <p className="usage-guide-scroll-hint">↓ 画面別の詳細手順は下へ</p>

          {guideSections.map((section) => (
            <div key={section.label} className="usage-guide-section">
              <p className="usage-guide-section-label">{section.label}</p>
              <ol className="usage-guide-steps">
                {section.steps.map((step) => (
                  <li key={step.title}>
                    <strong>{step.title}</strong>
                    <p>{step.body}</p>
                    {step.items?.length ? (
                      <ul className="usage-guide-items">
                        {step.items.map((item) => (
                          <li key={item}>{item}</li>
                        ))}
                      </ul>
                    ) : null}
                  </li>
                ))}
              </ol>
            </div>
          ))}

          <p className="usage-guide-footer">
            ▼ で閉じる · ヘッダーをドラッグして移動 · 表示位置は自動保存されます。
          </p>
        </div>
      </div>
    </>
  );
}
