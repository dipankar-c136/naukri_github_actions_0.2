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

## 🛠️ Tech Stack & Architecture

* **Core Language:** Java 17
* **Automation Tool:** Selenium WebDriver (v4.x) with WebDriverManager
* **Behavior-Driven Development (BDD):** Cucumber (Gherkin syntax for business-readable tests)
* **Test Runner & Assertions:** TestNG
* **Build Management:** Apache Maven
* **Design Pattern:** Page Object Model (POM) for high maintainability and code reusability
* **Logging:** Log4j2
* **CI/CD Pipeline:** GitHub Actions (.yml workflows)

## 📁 Framework Structure

The framework is strictly structured following Maven and POM standards:

```text
├── src/main/java/com/automation/
│   ├── pages/          # Encapsulated WebElements and page-specific logic (BasePage, LoginPage, etc.)
│   ├── utils/          # Core utilities (DriverFactory, GmailUtils for IMAP)
│   └── config/         # Environment setup (ConfigReader for Local vs CI execution)
├── src/test/java/com/automation/
│   ├── steps/          # Cucumber Step Definitions & Hooks (Setup/Teardown)
│   └── runners/        # TestNG Test Runner
├── src/test/resources/
│   ├── features/       # Gherkin feature files (.feature)
│   └── testng.xml      # Test suite configuration
└── .github/workflows/  # CI/CD Pipeline configuration (Cron scheduled jobs)