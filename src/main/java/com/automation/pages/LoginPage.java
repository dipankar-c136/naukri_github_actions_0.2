package com.automation.pages;

import com.automation.utils.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;



public class LoginPage extends BasePage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators (By Class Strategy)
    private By usernameField = By.id("usernameField");
    private By passwordField = By.id("passwordField");
    private By loginButton = By.cssSelector("button.blue-btn");
    private By otpInput = By.cssSelector("input[type='tel']");
    private By verifyButton = By.xpath("//button[contains(text(),'Verify')]");

    public LoginPage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void navigateToLogin() {
        driver.get("https://www.naukri.com/nlogin/login");
        logger.info("Navigated to Naukri login page");
        takeScreenshot(driver, "LoginPage");
    }

    public void enterCredentials(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField)).sendKeys(username);
        driver.findElement(passwordField).sendKeys(password);
        logger.info("Entered credentials for user: " + username);
        logger.info("Password entered: " + "*".repeat(password.length()));
        takeScreenshot(driver, "CredentialsEntered");
    }

    public void clickLogin() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        // JS Click to avoid interception
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public boolean isOtpRequested() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(otpInput));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void enterOtp(String otp) {
        char[] digits = otp.toCharArray();
        var inputs = driver.findElements(otpInput);
        for (int i = 0; i < inputs.size(); i++) {
            inputs.get(i).sendKeys(String.valueOf(digits[i]));
        }
    }

    // Add logic to click 'Verify' similar to clickLogin...
    public void clickOnVerify() {
        try {
            List<WebElement> buttons = driver.findElements(By.tagName("button"));
            boolean clicked = false;
            for (WebElement btn : buttons) {
                if (btn.getText().toLowerCase().contains("verify")) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                    clicked = true;
                    break;
                }
            }
            if (!clicked) {
                WebElement submitBtn = driver.findElement(By.cssSelector("button[type='submit']"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);
            }
        } catch (Exception e) {
            logger.info("Verify click skipped (maybe auto-submitted).");
        }
    }
}