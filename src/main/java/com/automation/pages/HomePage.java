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


public class HomePage extends BasePage{
    private WebDriver driver;
    private WebDriverWait wait;

    private By homePageTitle = By.xpath("//title");
    private By viewProfile = By.cssSelector(".view-profile-wrapper a");

    public boolean isDashboardDisplayed(){
        driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        try {
            wait.until(ExpectedConditions.urlContains("mnjuser"));
            logger.info("Dashboard is displayed (URL contains 'mnjuser')");
            takeScreenshot(driver, "DashboardDisplayed");
            return true;
        } catch (Exception e) {
            logger.error("Dashboard not displayed: " + e.getMessage());
            throw new AssertionError("Dashboard not displayed (URL does not contain 'mnjuser')");
        }
    }

    public void clickViewProfile(){
        waitForPageLoad(driver);
        waitForElementToLoad(driver, viewProfile);
        clickOnElementJS(driver, viewProfile);
        logger.info("Clicked on 'View Profile' button");
        waitForPageLoad(driver);
    }
}
