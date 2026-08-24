package com.automation.pages;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;

public class BasePage {
    protected Logger logger = LogManager.getLogger(this.getClass());
    private static int snapCounter = 1;

    public static void log(String msg) {
        System.out.println("➡️ " + msg);
    }

    public static void takeScreenshot(WebDriver driver, String name) {
        try {
            String fileName = String.format("%02d_%s.png", snapCounter++, name);
            FileUtils.copyFile(((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE), new File(fileName));
            log("📸 Screenshot saved: " + fileName);
        } catch (IOException e) {
            System.err.println("Screenshot failed: " + e.getMessage());
        }
    }

    public void waitForPageLoad(WebDriver driver) {
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(30)).until(
                webDriver -> ((org.openqa.selenium.JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
        logger.info("✅ Page loaded successfully");
    }

    public void waitForElementToLoad(WebDriver driver, org.openqa.selenium.By locator) {
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(20)).until(
                org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(locator));
        logger.info("✅ Element is visible: " + locator.toString());
    }

    public void scrollToElement(WebDriver driver, org.openqa.selenium.By locator) {
        org.openqa.selenium.WebElement element = driver.findElement(locator);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        logger.info("🔍 Scrolled to element: " + locator.toString());
    }

    public void clickOnElementJS(WebDriver driver, org.openqa.selenium.By locator) {
        org.openqa.selenium.WebElement element = driver.findElement(locator);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        logger.info("🖱️ Clicked on element using JS: " + locator.toString());
    }

    /**
     * Short, non-throwing presence probe. Returns true if the element is
     * present in the DOM within the timeout, false otherwise. Handy for
     * cheap branching without blowing the default 20s wait budget.
     */
    public boolean isElementPresent(WebDriver driver, org.openqa.selenium.By locator, int timeoutSeconds) {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(timeoutSeconds))
                    .until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Non-throwing visibility probe. Returns true if the element is
     * visible within the timeout, false otherwise.
     */
    public boolean isElementVisible(WebDriver driver, org.openqa.selenium.By locator, int timeoutSeconds) {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(timeoutSeconds))
                    .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Uninterruptable-safe sleep helper. Prefer explicit waits over this;
     * only use for tiny settle windows where a wait-condition doesn't apply.
     */
    protected void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
