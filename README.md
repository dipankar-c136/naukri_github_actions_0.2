# 🚀 Naukri Profile Automation Framework (CI/CD Pipeline)

An enterprise-grade, fully automated test framework designed to maintain an active profile presence on the Naukri job portal. This project leverages **Java**, **Selenium WebDriver**, and **Cucumber BDD** to simulate human interactions, bypass automated OTP challenges, and schedule daily resume updates via **GitHub Actions**.

## 🎯 Business Objective
Recruiters actively look for candidates with recently updated profiles. This automation bot ensures the candidate's profile remains at the top of search results by logging in, navigating the dashboard, and re-uploading the latest resume on a daily scheduled CRON job—completely hands-free.

## ✨ Key Engineering Highlights

* **Intelligent OTP Bypass:** Implemented a secure IMAP integration using `JavaMail API` to fetch and parse dynamic 6-digit OTPs from Gmail in real-time, bypassing 2FA restrictions.
* **Anti-Bot Evasion:** Configured advanced `ChromeOptions` (User-Agent spoofing, headless flags, disabling navigator properties) to consistently bypass bot-detection algorithms.
* **CI/CD Integration:** Containerized the execution using **GitHub Actions**, allowing headless browser tests to run daily on an `ubuntu-latest` runner without manual intervention.
* **Secure Credential Management:** Enforced strict security practices by routing all sensitive data (Passwords, App Passwords, Emails) through dynamic `ConfigReader` logic, seamlessly switching between local `.properties` files and **GitHub Secrets**.
* **Dynamic File Handling:** Built OS-agnostic file path resolution (`user.dir`) for dynamic PDF resume uploads across diverse environments (Windows/Linux/Mac).
* **Robust Error Handling & Logging:** Integrated **Log4j2** for comprehensive test execution tracking and implemented "Soft Handling" logic to manage unpredictable, dynamic DOM popups (like Chatbots/Ads) without failing tests.
* **Multi-User (Data-Driven) Execution:** A Cucumber `Scenario Outline` runs the entire login → re-upload → early-access flow **once per user** (`USER1`, `USER2`), each with its own credentials, Gmail mailbox, and resume file.
* **Early Access Roles Automation:** Automatically opens the "Early Access Roles" widget, lists every recommended role, and clicks **Share Interest** on each — with stale-element re-querying, JS-click fallbacks, refresh-based recovery, and graceful skipping when none are available.
* **Resilient Pop-up Handling (`PopupHandler`):** A registry-driven, never-throwing dismisser that closes known Naukri pop-ups (Naukri360Pro, chatbot drawer/overlay, generic widgets, and the **NPS survey**) and is swept at every interaction point. Adding a new pop-up is a one-line change.
* **Automated Screenshots & HTML Email Reports:** Numbered screenshots are captured at each step, uploaded as CI artifacts (30-day retention), and rich HTML success/failure emails are sent with run metadata and artifact links.

## 🛠️ Tech Stack & Architecture

* **Core Language:** Java 17
* **Automation Tool:** Selenium WebDriver 4.28.1 with WebDriverManager 5.9.2 (auto driver provisioning)
* **Behavior-Driven Development (BDD):** Cucumber 7.15.0 (`cucumber-java` + `cucumber-testng`, Gherkin syntax)
* **Test Runner & Assertions:** TestNG 7.9.0
* **Build Management:** Apache Maven
* **OTP Retrieval:** JavaMail / `javax.mail` 1.6.2 (IMAP over Gmail)
* **Design Pattern:** Page Object Model (POM) for high maintainability and code reusability
* **Logging:** Log4j2 2.22.1
* **CI/CD Pipeline:** GitHub Actions (`.yml` workflows)

## 📁 Project Structure

The framework is strictly structured following Maven and POM standards:

```text
naukri_github_actions_0.2/
├── .github/
│   └── workflows/
│       └── naukri-job.yml                    # CI/CD pipeline: daily CRON, artifacts, HTML email alerts
├── .mvn/                                     # Maven wrapper support files
├── src/
│   ├── main/
│   │   ├── java/com/automation/
│   │   │   ├── config/
│   │   │   │   └── ConfigReader.java         # Local (.properties) vs CI (env/Secrets) resolver
│   │   │   ├── pages/                        # Page Object Model
│   │   │   │   ├── BasePage.java             # Shared waits, JS click, scroll, screenshots
│   │   │   │   ├── LoginPage.java            # Login + OTP entry
│   │   │   │   ├── HomePage.java             # Dashboard + Early Access Roles automation
│   │   │   │   └── ProfilePage.java          # Resume delete / re-upload flow
│   │   │   └── utils/
│   │   │       ├── DriverFactory.java        # ThreadLocal ChromeDriver + anti-bot options
│   │   │       ├── GmailUtils.java           # IMAP OTP fetcher (JavaMail)
│   │   │       └── PopupHandler.java         # Registry-based pop-up dismisser
│   │   └── resources/
│   │       └── log4j2.xml                    # Console logging configuration
│   └── test/
│       ├── java/com/automation/
│       │   ├── runners/
│       │   │   └── TestRunner.java           # Cucumber + TestNG runner (@login tag)
│       │   └── steps/
│       │       ├── Hooks.java                # @Before/@After: driver init, failure screenshot
│       │       ├── LoginSteps.java           # Login + OTP step defs
│       │       ├── HomePageSteps.java        # Dashboard + Early Access step defs
│       │       └── ProfilePageSteps.java     # Resume management step defs
│       └── resources/
│           ├── features/
│           │   └── Login.feature             # Gherkin scenario (data-driven: USER1 / USER2)
│           ├── config.properties             # Local credentials (git-ignored)
│           ├── testng.xml                    # TestNG suite
│           ├── CV_Dipankar_Chakraborty_2026.pdf
│           └── Resume_Dipankar.pdf
├── pom.xml
└── README.md
```

## 🔄 Automated Workflow

The end-to-end scenario (`Login.feature`) runs **once per user** via a Cucumber `Scenario Outline`:

1. Navigate to the Naukri login page.
2. Enter per-user credentials (`<userKey>_EMAIL` / `<userKey>_PASSWORD`).
3. Click login and, if an OTP challenge appears, fetch the 6-digit code from Gmail over IMAP and submit it.
4. Verify the dashboard (`URL contains mnjuser`) and dismiss any post-login pop-ups.
5. Open **View Profile** and confirm the profile page.
6. Navigate to the resume section, delete the existing resume (if present), and re-upload the user's PDF (with 3× retry).
7. Assert the "Resume has been successfully uploaded" message.
8. Return to the homepage, open **Early Access Roles → View all**, list every role, and click **Share Interest** on each (gracefully skipping when none are available).

Every step captures a numbered screenshot (e.g. `03_DashboardDisplayed.png`) for traceability.

## 🧩 Core Components

| Component | Responsibility |
|-----------|----------------|
| `ConfigReader` | Resolves config in priority order: **CI env vars → local env vars → `config.properties`**. Throws a clear error if a key is missing. |
| `DriverFactory` | ThreadLocal `ChromeDriver` with anti-detection `ChromeOptions`; auto-enables `--headless=new` when `CI=true`. |
| `GmailUtils` | Connects to `imap.gmail.com`, polls for the Naukri OTP email (60s window), and extracts the 6-digit code via regex. |
| `PopupHandler` | Best-effort, never-throwing dismisser for known Naukri pop-ups (see below). |
| `BasePage` | Shared helpers: explicit waits, JS click, scroll, presence/visibility probes, numbered screenshots. |
| `HomePage` / `ProfilePage` / `LoginPage` | Page Objects encapsulating locators and page-specific actions. |

## 🛡️ Pop-up Handling (`PopupHandler`)

Naukri injects unpredictable promotional / survey pop-ups that can intercept clicks. `PopupHandler` maintains a **registry** of known pop-ups (container + close-button CSS) and is swept at every interaction point. It never throws, JS-clicks the close control, and falls back to removing the node from the DOM.

Currently handled:
- Naukri360Pro post-purchase pop-up
- Generic `*-popup-wdgt-container` widget pop-ups
- Chatbot drawer + overlay ("Welcome to Naukri")
- **NPS survey** ("How likely are you to recommend our Power Profile service?")

**Extending:** add one `PopupSpec` entry to `KNOWN_POPUPS` — no other code changes required.

## ⚙️ Prerequisites

- **Java 17** (JDK)
- **Apache Maven 3.6+**
- **Google Chrome** (WebDriverManager auto-provisions the matching driver)
- A **Gmail App Password** per account (Google Account → Security → 2-Step Verification → App Passwords) for IMAP OTP retrieval

## 🔐 Configuration

Credentials resolve from `config.properties` locally and from **GitHub Secrets** in CI. The local file is **git-ignored** and must never be committed.

Create `src/test/resources/config.properties` with the following keys (values are examples — use your own):

```properties
USER1_EMAIL=you@example.com
USER1_PASSWORD=yourPassword
USER1_RESUME=YourResume.pdf
USER1_GMAIL_USERNAME=you@example.com
USER1_GMAIL_APP_PASSWORD=xxxx xxxx xxxx xxxx

# Repeat with USER2_* keys to run the flow for a second account
```

Place each user's resume PDF in `src/test/resources/` using the exact `*_RESUME` file name.

For CI, add these repository **Secrets**: the `USER1_*` / `USER2_*` keys above, plus `GMAIL_USERNAME`, `GMAIL_APP_PASSWORD`, and `NOTIFICATION_EMAIL` (used by the email-notification steps).

## ▶️ Running Locally

```bash
# Run the full suite (visible browser)
mvn test

# Run headless (as CI does)
mvn test -Dheadless=true
```

The runner is tag-filtered to `@login`. Reports and screenshots are generated under the project root and `target/`.

## 🤖 CI/CD Pipeline (GitHub Actions)

Defined in `.github/workflows/naukri-job.yml`:

- **Schedule:** daily CRON at `01:30 UTC` (7:00 AM IST); also runnable on-demand via **workflow_dispatch**.
- **Runner:** `ubuntu-latest` with **Temurin JDK 17**; tests run headless (`CI=true`).
- **Secrets:** all credentials are injected as environment variables — nothing is hard-coded.
- **Artifacts:** screenshots (`*.png`) + Surefire/Cucumber reports uploaded and retained **30 days**.
- **Notifications:** rich **HTML email** on success ✅ and failure ❌ (run number, duration, commit info, artifact links).

## 📊 Reports & Screenshots

- **Cucumber HTML:** `target/cucumber-reports.html`
- **Surefire / TestNG:** `target/surefire-reports/`
- **Step screenshots:** numbered PNGs in the project root (e.g. `01_LoginPage.png` … `20_EarlyAccessRoles_Done_USER2.png`)
- On failure, a screenshot is auto-attached to the Cucumber scenario via `Hooks`.

## ⚠️ Disclaimer

This project is for **educational and personal productivity** purposes. Automating interactions with Naukri.com may be subject to their Terms of Service — use responsibly and at your own risk. Never commit real credentials; keep them in `config.properties` (git-ignored) or GitHub Secrets.