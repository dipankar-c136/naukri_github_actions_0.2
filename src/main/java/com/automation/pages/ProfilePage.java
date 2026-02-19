package com.automation.pages;

import com.automation.utils.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.List;


public class ProfilePage extends BasePage {
    private WebDriver driver;
    private WebDriverWait wait;
    

    private By profilePAgeTitle = By.xpath("//title");
    private By deleteResumeButton = By.xpath("//span[@data-title='delete-resume']");
    private By deleteResumeConfirmationMessage = By.xpath("//p[contains(text(), 'Are you sure you want to delete the resume?')]");
    private By deleteResumeConfirmationButton = By.xpath("(//button[contains(text(), 'Delete')])[3]");
    private By updateResumeButton = By.xpath("//input[@value='Update resume']");
    private By uploadResumeButton = By.xpath("//input[@type='file' and @id='attachCV']");
    private By successMessage = By.xpath("//*[contains(text(), 'Resume has been successfully uploaded')]");

    public ProfilePage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void isProfilePageDisplayed() {
        driver = DriverFactory.getDriver();
        try {
            wait.until(ExpectedConditions.urlContains("profile"));
            logger.info("Profile page is displayed (URL contains 'profile')");
            takeScreenshot(driver, "ProfilePageDisplayed");
        } catch (Exception e) {
            logger.error("Profile page not displayed: " + e.getMessage());
            throw new AssertionError("Profile page not displayed (URL does not contain 'viewprofile')");
        }
    }

    public void scrollToResumeSection() {
        waitForPageLoad(driver);
        waitForElementToLoad(driver, updateResumeButton);
        scrollToElement(driver, updateResumeButton);
        logger.info("Scrolled to 'Update Resume' button");
    }

    public void deleteResume(){
        waitForElementToLoad(driver, deleteResumeButton);
        clickOnElementJS(driver, deleteResumeButton);
        logger.info("Successfully Clicked on 'Delete Resume' button");
        logger.info("Waiting for delete confirmation message to appear...");
        logger.info("Delete confirmation message: " + driver.findElement(deleteResumeConfirmationMessage).getText());
        logger.info("Clicking on delete confirmation button...");
        takeScreenshot(driver, "DeleteResumeConfirmation");
        clickOnElementJS(driver, deleteResumeConfirmationButton);
        waitForPageLoad(driver);
    }

    public void uploadResume(String fileName) {
        // 1. Get the project root directory dynamically
        String projectPath = System.getProperty("user.dir");

        // 2. Construct the universal path.
        // File.separator ensures it uses '\' on Windows (Local) and '/' on Linux (GitHub Actions)
        String filePath = projectPath + File.separator + "src" + File.separator + "test"
                + File.separator + "resources" + File.separator + fileName;
        logger.info("Constructed file path for resume: " + filePath);
        // 3. Pre-check if file exists (gives a better error than Selenium crashing)
        File fileToUpload = new File(filePath);
        if (!fileToUpload.exists()) {
            throw new RuntimeException("❌ Resume file not found at: " + filePath);
        }

        logger.info("Uploading resume from: " + filePath);

        // 4. Send the file path directly to the input tag (Bypasses OS popups completely)
        wait.until(ExpectedConditions.presenceOfElementLocated(uploadResumeButton)).sendKeys(filePath);
    }

    public void isUploadSuccessful() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
            logger.info("Resume upload successful (Success message is visible)");
            logger.info("Success message text: " + driver.findElement(successMessage).getText());
            takeScreenshot(driver, "ResumeUploadSuccess");
        } catch (Exception e) {
            logger.error("Resume upload failed: " + e.getMessage());
            throw new AssertionError("Resume upload failed (Success message not visible)");
        }
    }

}
