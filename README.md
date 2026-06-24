# 車検・整備管理システム

Java (Spring Boot) + React (Next.js) + PostgreSQL による車検・整備業務向け管理システムです。

## 機能概要

| 領域 | 機能 |
|------|------|
| 顧客 | 顧客マスタ・個人情報管理 |
| 車両 | 登録番号・車台番号の形式検証・重複防止 |
| 予約 | 車検・点検整備の予約管理 |
| 見積 | 見積作成・OSS連携通知 |
| 整備記録 | SHA-256 ハッシュチェーンによる改ざん防止・ロック |
| 請求 | 見積から請求書作成・入金管理 |
| 帳票 | PDF / Excel 出力 |
| 通知 | 車検満了日のメール・SMS（30/14/7/1日前） |
| 電子車検証 | 電子車検証データ取込・照合 |
| 文書 | S3 / ローカルストレージ（PDF・写真） |
| 監査 | 操作履歴ログ（audit_logs） |
| 認証 | JWT + Spring Security + MFA (TOTP) |

## 技術スタック

- **Backend:** Java 21, Spring Boot 3.2, Spring Security, JPA, Flyway
- **Frontend:** Next.js 15, React 19, TypeScript
- **DB:** PostgreSQL 16
- **Storage:** AWS S3（本番）/ ローカル（開発）
- **帳票:** OpenHTMLtoPDF, Apache POI
- **インフラ:** Docker, GitHub Actions
- **監視:** Actuator (Prometheus), 構造化ログ → CloudWatch / Grafana 連携想定

## クイックスタート（Docker）

```bash
cd vehicle_inspection
docker compose up -d --build
```

| サービス | URL |
|---------|-----|
| フロント | http://localhost:3000 |
| API | http://localhost:8080/api |
| ヘルスチェック | http://localhost:8080/api/health |
| MailHog UI | http://localhost:8025 |

**初期ログイン:** `admin@vehicle-inspection.local` / `admin123`

初回起動時、顧客テーブルが空の場合は **サンプルデータ**（顧客3件・車両3件・見積・請求・予約・通知など）が自動投入されます。無効化する場合は `SEED_SAMPLE_DATA=false` を設定してください。

## ローカル開発

### 前提
- Java 21, Maven 3.9+
- Node.js 22+
- PostgreSQL（port 5434 推奨 — docker compose の postgres を利用可）

### Backend

```bash
cd backend
# DB起動: docker compose up -d postgres mailhog
export DB_HOST=localhost DB_PORT=5434 DB_USER=vi_user DB_PASSWORD=vi_pass DB_NAME=vehicle_inspection
export JWT_SECRET=dev-jwt-secret-key-minimum-32-characters-long!!
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
# http://localhost:3000
```

## 環境変数（主要）

| 変数 | 説明 |
|------|------|
| `DATABASE_URL` / `DB_*` | PostgreSQL 接続 |
| `JWT_SECRET` | JWT 署名鍵（32文字以上） |
| `STORAGE_PROVIDER` | `local` または `s3` |
| `S3_BUCKET`, `AWS_REGION` | S3 設定 |
| `ELECTRONIC_INSPECTION_ENABLED` | 電子車検証 API 有効化 |
| `OSS_ENABLED`, `OSS_BASE_URL` | OSS 連携 |
| `SMS_ENABLED` | SMS 送信 |
| `EXPIRY_NOTIFY_DAYS` | 満了通知日（例: `30,14,7,1`） |

## API エンドポイント（抜粋）

```
POST   /api/auth/login
GET    /api/health
GET    /api/customers
POST   /api/vehicles          # 登録番号・車台番号検証
POST   /api/vehicles/validate
GET    /api/reservations
POST   /api/estimates
POST   /api/invoices/from-estimate/{id}
GET    /api/invoices/{id}/pdf
GET    /api/invoices/{id}/excel
POST   /api/maintenance       # ハッシュチェーン記録
POST   /api/maintenance/{id}/lock
POST   /api/notifications/check-expiry
POST   /api/electronic-inspections/import
POST   /api/documents/upload
GET    /api/audit-logs
POST   /api/mfa/setup
```

## DB スキーマ

Flyway マイグレーション: `backend/src/main/resources/db/migration/V1__init_schema.sql`

主要テーブル: `customers`, `vehicles`, `vehicle_inspections`, `inspection_reservations`, `inspection_items`, `maintenance_records`, `parts`, `estimates`, `invoices`, `payments`, `documents`, `notifications`, `audit_logs`, `users`, `roles`

## セキュリティ・コンプライアンス

- **車検満了通知:** スケジューラ + 手動トリガーで通知漏れ防止
- **入力ミス防止:** 登録番号・車台番号の正規表現検証 + DB ユニーク制約
- **改ざん防止:** 整備記録の SHA-256 ハッシュチェーン + ロック機能
- **個人情報:** 認証必須 API、監査ログ、マーケティング同意フラグ
- **操作履歴:** 全主要操作を `audit_logs` に JSON 保存

## Railway デプロイ

ログ `couldn't locate the dockerfile at path Dockerfile` はリポジトリルートに `Dockerfile` が無い場合に発生します。以下を追加済みです。

- `Dockerfile` — Next.js + Spring Boot 統合イメージ
- `railway.toml` — ビルド設定・ヘルスチェック `/api/health`
- `start.sh` — 起動スクリプト
- `application-railway.yml` — PostgreSQL 接続（Railway Postgres プラグイン）

### Railway 環境変数（必須）

1. Railway プロジェクトに **PostgreSQL** サービスを追加
2. アプリサービスの **Variables** で **Add Reference** → Postgres → `DATABASE_URL` を選択
3. `JWT_SECRET` に 32 文字以上のランダム文字列を設定
4. **Redeploy**（Clear build cache 推奨）

| 変数 | 値 |
|------|-----|
| `DATABASE_URL` | `${{Postgres.DATABASE_URL}}`（必須） |
| `JWT_SECRET` | 32文字以上のランダム文字列（必須） |

`Connection to localhost:5432 refused` は **Postgres 未連携** のときに出ます。上記 Reference 変数を設定してください。

個別指定する場合は `PGHOST` / `PGPORT` / `PGUSER` / `PGPASSWORD` / `PGDATABASE` を Postgres から Reference しても構いません。

