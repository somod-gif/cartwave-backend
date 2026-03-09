# CartWave — Environment Variables Guide (Beginner-Friendly)

This guide explains every variable in the `.env` file: **what it is**, **where to get it**, **an example value**, and **whether it's required**.

---

## How the `.env` file works

CartWave uses `spring-dotenv` to automatically read the `.env` file when the server starts.  
You do **not** need to run `export` or `set` in your terminal — just edit the file and restart.

```
Project root/
 ├── .env          ← your secrets live here (never commit this!)
 └── src/
```

---

## 1. Database — PostgreSQL via Neon

| Variable | Required | Example |
|---|---|---|
| `DB_URL` | ✅ Yes | `jdbc:postgresql://ep-xxx.neon.tech/neondb?sslmode=require` |
| `DB_USERNAME` | ✅ Yes | `neondb_owner` |
| `DB_PASSWORD` | ✅ Yes | `npg_aBcDeFgH` |

### What is it?
These three variables tell the app which PostgreSQL database to connect to and with what credentials.

### How to get it (free, takes ~2 minutes)

1. Go to **[https://neon.tech](https://neon.tech)** and sign up for a free account.
2. Click **"New Project"** → give it a name → click **Create**.
3. In the project dashboard click **"Connection Details"** (top right).
4. Under the dropdown select **"Connection string"** — paste that into `DB_URL`.
5. Copy the **User** field (`neondb_owner`) into `DB_USERNAME`.
6. Copy the **Password** field (long random string) into `DB_PASSWORD`.

> **Tip:** Make sure `?sslmode=require` is appended to the URL — Neon requires SSL.

---

## 2. JWT Authentication

| Variable | Required | Example |
|---|---|---|
| `JWT_SECRET` | ✅ Yes | `cW92F7kXpL3m...` (64+ random chars) |
| `JWT_ACCESS_EXPIRATION_MS` | Optional | `900000` (15 minutes) |
| `JWT_REFRESH_EXPIRATION_MS` | Optional | `604800000` (7 days) |

### What is it?
`JWT_SECRET` is a secret key used to sign and verify login tokens. If someone steals it they can forge login sessions — **keep it private**.

### How to generate it

**Option A — Online (quick):**
1. Go to **[https://generate-secret.vercel.app/64](https://generate-secret.vercel.app/64)**
2. Copy the generated string and paste it as `JWT_SECRET`.

**Option B — Terminal:**
```bash
# macOS / Linux
openssl rand -hex 64

# Windows PowerShell
-join ((65..90) + (97..122) + (48..57) | Get-Random -Count 64 | ForEach-Object {[char]$_})
```

### Expiration times
- `JWT_ACCESS_EXPIRATION_MS` = how long a login token lasts (milliseconds).  
  `900000` = 15 minutes. `3600000` = 1 hour.
- `JWT_REFRESH_EXPIRATION_MS` = how long a refresh token lasts.  
  `604800000` = 7 days.

---

## 3. Server

| Variable | Required | Example |
|---|---|---|
| `PORT` | Optional | `8080` |
| `CORS_ALLOWED_ORIGINS` | Optional | `http://localhost:3000` |

### What is it?
- `PORT` — the network port the server listens on. Default `8080`.
- `CORS_ALLOWED_ORIGINS` — which websites can call the API from a browser.  
  During development `*` (allow all) is fine. In production set your frontend URL, e.g.:
  ```
  CORS_ALLOWED_ORIGINS=https://myshop.com,https://admin.myshop.com
  ```

---

## 4. Email (SMTP)

| Variable | Required | Example |
|---|---|---|
| `SMTP_HOST` | Optional | `smtp.gmail.com` |
| `SMTP_PORT` | Optional | `587` |
| `SMTP_USERNAME` | Optional | `you@gmail.com` |
| `SMTP_PASSWORD` | Optional | `abcd efgh ijkl mnop` |
| `SMTP_AUTH` | Optional | `true` |
| `SMTP_STARTTLS` | Optional | `true` |

### What is it?
SMTP settings let the app send real emails (order confirmations, password resets, etc.). If left blank the app still runs — emails are queued but fail to send.

### Option A — Local dev (no real email, recommended for testing)

Install MailHog, a fake inbox that catches all outgoing mail:
```bash
# macOS
brew install mailhog
mailhog

# Windows — download from https://github.com/mailhog/MailHog/releases
# then run MailHog.exe
```
Then set:
```
SMTP_HOST=localhost
SMTP_PORT=1025
SMTP_AUTH=false
SMTP_STARTTLS=false
```
Open **[http://localhost:8025](http://localhost:8025)** to see all caught emails.

### Option B — Real Gmail SMTP

1. Go to your Google Account → **Security** → **2-Step Verification** → enable it.
2. Then go to **Security** → **App passwords** → select "Mail" + your device → click **Generate**.
3. Copy the 16-character password (with spaces) into `SMTP_PASSWORD`.
4. Set:
   ```
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USERNAME=your.email@gmail.com
   SMTP_PASSWORD=abcd efgh ijkl mnop
   SMTP_AUTH=true
   SMTP_STARTTLS=true
   ```

> **Never use your actual Gmail password.** The App Password is a separate credential.

### Option C — Free transactional email (production)

- **[Resend](https://resend.com)** — generous free tier, SMTP compatible.
- **[Brevo (Sendinblue)](https://brevo.com)** — 300 emails/day free.
- **[Mailgun](https://mailgun.com)** — 100 emails/day free.

Sign up → go to SMTP settings → copy host/port/username/password.

---

## 5. AWS S3 (File / Image Uploads)

| Variable | Required | Example |
|---|---|---|
| `AWS_ACCESS_KEY_ID` | Optional* | `AKIAIOSFODNN7EXAMPLE` |
| `AWS_SECRET_ACCESS_KEY` | Optional* | `wJalrXUtnFEMI/K7MDENG...` |
| `AWS_REGION` | Optional | `eu-west-1` |
| `AWS_S3_BUCKET_NAME` | Optional | `cartwave-assets` |
| `AWS_S3_BASE_URL` | Optional | `https://cartwave-assets.s3.eu-west-1.amazonaws.com` |

*Optional = app boots without them, but product image upload endpoints will fail.

### What is it?
Amazon S3 is cloud storage. CartWave uses it to store product images and store logos. If you leave these blank, image upload will throw an error at runtime but everything else works.

### How to set up (free tier available)

1. **Create an AWS account** at **[https://aws.amazon.com](https://aws.amazon.com)** (requires a credit card but S3 free tier = 5 GB free).

2. **Create an S3 bucket:**
   - Go to **Services → S3 → Create bucket**.
   - Pick a globally unique name (e.g. `my-cartwave-assets`).
   - Choose your region (e.g. `eu-west-1`).
   - Uncheck "Block all public access" if you want images publicly accessible.
   - Click **Create**.

3. **Create an IAM user with S3 access:**
   - Go to **Services → IAM → Users → Add users**.
   - Give it a name (e.g. `cartwave-s3-user`).
   - Choose **"Attach policies directly"** → search for **AmazonS3FullAccess** → attach.
   - After creation, click the user → **Security credentials → Create access key**.
   - Download / copy `Access key ID` → paste into `AWS_ACCESS_KEY_ID`.
   - Copy `Secret access key` → paste into `AWS_SECRET_ACCESS_KEY`.

4. **Build the base URL:**
   ```
   https://<bucket-name>.s3.<region>.amazonaws.com
   ```
   Example: `https://my-cartwave-assets.s3.eu-west-1.amazonaws.com`

> **Security tip:** Create an IAM policy that restricts the user to only that one bucket instead of using `AmazonS3FullAccess` in production.

### Free S3 alternative — Cloudflare R2

[Cloudflare R2](https://developers.cloudflare.com/r2/) is S3-compatible, has zero egress fees, and a generous free tier (10 GB storage, 10M reads/month). It works with the exact same AWS SDK — just change the endpoint URL.

---

## 6. Super Admin Seed

| Variable | Required | Example |
|---|---|---|
| `SUPERADMIN_EMAIL` | Optional | `superadmin@yourcompany.com` |
| `SUPERADMIN_PASSWORD` | Optional | `ChangeMe123!` |

### What is it?
On first startup the app automatically creates one super admin account. These variables control its email and password. If not set, defaults to `superadmin@cartwave.local` / `Password123!`.

### How to use it

1. Set a strong password before deploying to production:
   ```
   SUPERADMIN_EMAIL=superadmin@yourcompany.com
   SUPERADMIN_PASSWORD=MyStr0ng!Pass
   ```
2. After first run the account is created in the database. Changing these env vars afterwards does **not** update the password (the seeder checks if the account already exists).

3. Log in with:
   ```
   POST /api/v1/auth/login
   {
     "email": "superadmin@yourcompany.com",
     "password": "MyStr0ng!Pass"
   }
   ```
4. Use the returned JWT token in the `Authorization: Bearer <token>` header to access `/api/v1/super-admin/**` endpoints.

---

## Quick-Start Checklist

```
[ ] Signed up at neon.tech and copied DB_URL, DB_USERNAME, DB_PASSWORD
[ ] Generated JWT_SECRET (64 random chars)
[ ] Chose SMTP option: MailHog (local) OR Gmail app password (real)
[ ] (Optional) Created AWS S3 bucket + IAM user for image uploads
[ ] Set SUPERADMIN_EMAIL and SUPERADMIN_PASSWORD to something secure
[ ] Verified .env is in .gitignore (never commit secrets!)
```

---

## .gitignore check

Open `.gitignore` and make sure this line exists:
```
.env
```

If it doesn't, add it immediately before you `git push` anything.

---

## Full `.env` template

```properties
# ─── Database (Neon PostgreSQL) ──────────────────────────────────────────────
DB_URL=jdbc:postgresql://<your-neon-host>/neondb?sslmode=require&connectTimeout=10&socketTimeout=30&tcpKeepAlive=true
DB_USERNAME=neondb_owner
DB_PASSWORD=<your-neon-password>

# ─── JWT ─────────────────────────────────────────────────────────────────────
JWT_SECRET=<64-char-random-string>
JWT_ACCESS_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000

# ─── Server ──────────────────────────────────────────────────────────────────
PORT=8080
CORS_ALLOWED_ORIGINS=*

# ─── Email (SMTP) ────────────────────────────────────────────────────────────
SMTP_HOST=localhost
SMTP_PORT=1025
SMTP_USERNAME=
SMTP_PASSWORD=
SMTP_AUTH=false
SMTP_STARTTLS=false

# ─── AWS S3 (image storage) ──────────────────────────────────────────────────
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_REGION=eu-west-1
AWS_S3_BUCKET_NAME=cartwave-assets
AWS_S3_BASE_URL=https://cartwave-assets.s3.eu-west-1.amazonaws.com

# ─── Super Admin seed account ────────────────────────────────────────────────
SUPERADMIN_EMAIL=superadmin@cartwave.local
SUPERADMIN_PASSWORD=Password123!
```
