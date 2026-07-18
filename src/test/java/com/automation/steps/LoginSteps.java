package com.automation.steps;

import com.automation.config.ConfigReader;
import com.automation.pages.LoginPage;
import com.automation.utils.GmailUtils;
import io.cucumber.java.en.*;
import org.testng.Assert;

public class LoginSteps {
    LoginPage loginPage = new LoginPage();

    @Given("I am on the Naukri login page")
    public void i_am_on_naukri_login() {
        loginPage.navigateToLogin();
    }

    /*
    @When("I enter valid credentials")
    public void i_enter_valid_credentials() {
        // Fetching via ConfigReader (Handles Local vs CI automatically)
        String u = ConfigReader.get("NAUKRI_EMAIL");
        String p = ConfigReader.get("NAUKRI_PASSWORD");

        loginPage.enterCredentials(u, p);
    }
    */
    // NEW: accepts a userKey (USER1 / USER2) from the feature file
    @When("I enter valid credentials for {string}")
    public void i_enter_valid_credentials(String userKey) {
        String u = ConfigReader.get(userKey + "_EMAIL");
        String p = ConfigReader.get(userKey + "_PASSWORD");
        if (u == null || p == null) {
            throw new IllegalStateException(
                    "Missing credentials for " + userKey +
                            ". Check config.properties (local) or GitHub Secrets (CI).");
        }
        loginPage.enterCredentials(u, p);
    }

    @And("I click the login button")
    public void i_click_login() {
        loginPage.clickLogin();
    }

    /*
    @Then("I should handle OTP verification if requested")
    public void i_handle_otp() throws Exception {
        if (loginPage.isOtpRequested()) {
            System.out.println("OTP Requested. Fetching from Gmail...");

            // Fetching via ConfigReader
            String gUser = ConfigReader.get("GMAIL_USERNAME");
            String gPass = ConfigReader.get("GMAIL_APP_PASSWORD");

            String otp = GmailUtils.getOtp(gUser, gPass);
            System.out.println("OTP Received: " + otp);
            loginPage.enterOtp(otp);
            loginPage.clickOnVerify();
        }
    }
    */
    // NEW: uses per-user Gmail credentials to fetch OTP from the correct mailbox
    @Then("I should handle OTP verification if requested for {string}")
    public void i_handle_otp(String userKey) throws Exception {
        if (loginPage.isOtpRequested()) {
            System.out.println("OTP Requested for " + userKey + ". Fetching from Gmail...");

            String gUser = ConfigReader.get(userKey + "_GMAIL_USERNAME");
            String gPass = ConfigReader.get(userKey + "_GMAIL_APP_PASSWORD");

            String otp = GmailUtils.getOtp(gUser, gPass);
            System.out.println("OTP Received: " + otp);
            loginPage.enterOtp(otp);
            loginPage.clickOnVerify();
        }
    }

}