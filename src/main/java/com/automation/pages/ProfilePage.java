package com.automation.pages;

import com.automation.utils.DriverFactory;
import com.automation.utils.PopupHandler;
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
            // Dismiss any promo pop-ups that may have appeared on navigation.
            PopupHandler.dismissAll(driver);
            takeScreenshot(driver, "ProfilePageDisplayed");
        } catch (Exception e) {
            logger.error("Profile page not displayed: " + e.getMessage());
            throw new AssertionError("Profile page not displayed (URL does not contain 'viewprofile')");
        }
    }

    public void scrollToResumeSection() {
        waitForPageLoad(driver);
        // Sweep once more here - a delayed pop-up can render after the profile
        // page URL check but before the resume section is interacted with.
        PopupHandler.dismissAll(driver);
        // Prefer the 'Update resume' button (present when a resume already exists).
        // If it isn't present (previous upload failed / no resume on profile),
        // fall back to scrolling to the file-upload input so the section is still visible.
        // NOTE: the fallback element (#attachCV) is a hidden <input type="file">, so we
        // only check for DOM *presence* here - a visibility wait would never succeed
        // and would fail the whole scenario before the actual upload step even runs.
        if (isElementPresent(updateResumeButton, 10)) {
            scrollToElement(driver, updateResumeButton);
            logger.info("Scrolled to 'Update Resume' button");
        } else if (isElementPresent(uploadResumeButton, 5)) {
            logger.warn("'Update Resume' button not found - assuming no resume is currently uploaded. " +
                    "Scrolling to resume upload input instead.");
            scrollToElement(driver, uploadResumeButton);
        } else {
            // Non-fatal: uploadResume() will do its own presence-wait and retry.
            logger.warn("Neither 'Update Resume' button nor resume upload input was found on the page. " +
                    "Skipping scroll and letting uploadResume() handle it.");
        }
    }

    public void deleteResume() {
        // Guard: if no resume is currently uploaded on the profile, there is nothing
        // to delete. Detect this by the absence of the 'delete-resume' control and
        // skip the delete flow entirely so we can proceed straight to upload.
        if (!isResumeAlreadyUploaded()) {
            logger.info("No existing resume detected on profile (delete button not present). " +
                    "Skipping deleteResume() and proceeding directly to upload.");
            return;
        }

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

    /**
     * Returns true if a resume is currently present on the profile (i.e. the
     * delete-resume control is available). Uses a short timeout so callers can
     * cheaply branch on the state of the profile.
     */
    public boolean isResumeAlreadyUploaded() {
        return isElementPresent(deleteResumeButton, 5);
    }

    private boolean isElementPresent(By locator, int timeoutSeconds) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            shortWait.until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
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

        // 4. Retry the upload up to 3 times, verifying success after each attempt.
        final int maxAttempts = 3;
        Throwable lastError = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            logger.info("Resume upload attempt " + attempt + " of " + maxAttempts);
            try {
                // Send the file path directly to the input tag (bypasses OS popups completely)
                wait.until(ExpectedConditions.presenceOfElementLocated(uploadResumeButton))
                        .sendKeys(filePath);

                // Verify the upload actually succeeded by reusing the existing check.
                isUploadSuccessful();

                logger.info("✅ Resume upload succeeded on attempt " + attempt);
                return;
            } catch (Exception | AssertionError e) {
                lastError = e;
                logger.warn("Resume upload attempt " + attempt + " failed: " + e.getMessage());
                takeScreenshot(driver, "ResumeUploadFailed_Attempt" + attempt);

                if (attempt < maxAttempts) {
                    try {
                        // Small back-off + refresh state before retrying so a stale
                        // error banner or half-completed request doesn't poison the next try.
                        Thread.sleep(2000);
                        driver.navigate().refresh();
                        waitForPageLoad(driver);
                        scrollToResumeSection();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    } catch (Exception recoverEx) {
                        logger.warn("Recovery before retry failed: " + recoverEx.getMessage());
                    }
                }
            }
        }

        String msg = "❌ Resume upload failed after " + maxAttempts + " attempts";
        logger.error(msg + (lastError != null ? " - last error: " + lastError.getMessage() : ""));
        throw new AssertionError(msg);
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
