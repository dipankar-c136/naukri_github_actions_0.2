package com.automation.pages;

import com.automation.utils.DriverFactory;
import com.automation.utils.PopupHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


public class HomePage extends BasePage{
    private WebDriver driver;
    private WebDriverWait wait;

    private By homePageTitle = By.xpath("//title");
    private By viewProfile = By.cssSelector(".view-profile-wrapper a");

    // Early Access Roles locators
    private final By naukriLogo = By.xpath("(//a[.//img[@alt='Naukri Logo']])[1]");
    private final By viewAllEarlyAccess = By.xpath("//a[contains(@class,'spc__view-all')]");
    private final By earlyAccessTuple = By.xpath("//div[@class='tlc__tuple']");
    private final By shareInterestBtn = By.xpath(
            "//button[normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))='share interest']");
    private final By interestSharedBtn = By.xpath(
            "//button[normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))='interest shared']");

    public boolean isDashboardDisplayed(){
        driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        try {
            wait.until(ExpectedConditions.urlContains("mnjuser"));
            logger.info("Dashboard is displayed (URL contains 'mnjuser')");
            // Dismiss any post-login promo pop-ups (e.g. Naukri360Pro) before proceeding.
            PopupHandler.dismissAll(driver);
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

    // ============================================================
    //  Early Access Roles Automation
    // ============================================================

    /**
     * Navigate back to the Naukri homepage ({@code mnjuser/homepage}).
     *
     * <p>Wait / fallback strategy (in order):</p>
     * <ol>
     *   <li>Wait for {@code document.readyState=complete} and any pending pop-ups dismissed.</li>
     *   <li>Preferred: click the Naukri logo when it becomes clickable.</li>
     *   <li>Fallback #1: {@code driver.get("...mnjuser/homepage")}.</li>
     *   <li>Fallback #2: navigate via {@code JavaScript window.location.href}
     *       (handles rare cases where {@code driver.get} silently no-ops
     *       because of an intercepting overlay).</li>
     * </ol>
     *
     * <p>Post-conditions verified:</p>
     * <ul>
     *   <li>URL contains {@code mnjuser/homepage} (stricter than the old
     *       {@code mnjuser} match which also passed for the profile page).</li>
     *   <li>Naukri logo is present — a stable homepage landmark.</li>
     * </ul>
     */
    public void navigateToHomepage() {
        driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        waitForPageLoad(driver);
        PopupHandler.dismissAll(driver);

        // 1. Preferred: click the Naukri logo.
        boolean navigated = false;
        try {
            wait.until(ExpectedConditions.elementToBeClickable(naukriLogo)).click();
            logger.info("Clicked on Naukri logo to navigate back to homepage");
            navigated = true;
        } catch (Exception e) {
            logger.warn("Naukri logo click failed, will fall back to direct URL. Reason: " + e.getMessage());
        }

        // 2. Fallback #1: direct URL.
        if (!navigated || !isOnHomepageUrl(3)) {
            try {
                driver.get("https://www.naukri.com/mnjuser/homepage");
                logger.info("Navigated to homepage via driver.get() fallback");
                navigated = true;
            } catch (Exception e) {
                logger.warn("driver.get() fallback also failed: " + e.getMessage());
            }
        }

        // 3. Fallback #2: JS location.href (very last resort).
        if (!isOnHomepageUrl(3)) {
            try {
                ((JavascriptExecutor) driver)
                        .executeScript("window.location.href='https://www.naukri.com/mnjuser/homepage';");
                logger.warn("Navigated to homepage via JS location.href fallback");
            } catch (Exception e) {
                logger.error("JS location.href fallback failed: " + e.getMessage());
            }
        }

        // Post-conditions.
        waitForPageLoad(driver);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.urlContains("mnjuser/homepage"));
        } catch (Exception e) {
            logger.error("URL never settled on mnjuser/homepage. Current URL: " + driver.getCurrentUrl());
            takeScreenshot(driver, "HomepageNavigation_UrlNotSettled");
            throw new AssertionError("Failed to navigate back to Naukri homepage. Current URL: "
                    + driver.getCurrentUrl());
        }
        // Wait for a stable homepage landmark (the Naukri logo) to be present.
        // This confirms the DOM has actually rendered, not just readyState=complete.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.presenceOfElementLocated(naukriLogo));
        } catch (Exception e) {
            logger.warn("Naukri logo not present after homepage nav (continuing anyway): " + e.getMessage());
        }
        PopupHandler.dismissAll(driver);
        takeScreenshot(driver, "BackOnHomepage");
    }

    /** Quick, non-throwing check that we're currently on the homepage URL. */
    private boolean isOnHomepageUrl(int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.urlContains("mnjuser/homepage"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Click the "View all" link on the Early Access Roles widget to open the
     * full list of early-access recommended roles.
     *
     * <p>Behaviour:</p>
     * <ul>
     *   <li>If the "View all" link is not present (i.e. the user has already
     *       shared interest on every available role and Naukri has nothing
     *       left to show at this time), the method logs a clear message and
     *       returns {@code false} without failing the scenario.</li>
     *   <li>Otherwise, retries up to 3 times with per-attempt pop-up dismissal,
     *       scroll-into-view and (from attempt 2 onward) a page refresh, and
     *       returns {@code true} once the click succeeds AND the target list
     *       page has rendered (verified by presence of role tuples).</li>
     * </ul>
     *
     * <p>Wait strategy notes:</p>
     * <ul>
     *   <li>{@code readyState=complete} is checked, but is not sufficient for
     *       lazy-rendered widgets — we also wait for a stable landmark (role
     *       tuple) after clicking to confirm success.</li>
     *   <li>Two clickability strategies are attempted: standard Selenium click,
     *       then a JS-click fallback if the first hits an interception.</li>
     * </ul>
     *
     * @param userKey identifier for the current user (e.g. USER1, USER2) - used in logs.
     * @return {@code true} if the click was performed AND role list rendered;
     *         {@code false} if the widget was absent.
     */
    public boolean clickViewAllEarlyAccessRoles(String userKey) {
        driver = DriverFactory.getDriver();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        final int maxAttempts = 3;
        final Duration probeTimeout = Duration.ofSeconds(8);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            logger.info("'View all' Early Access - attempt " + attempt + " of " + maxAttempts + " for [" + userKey + "]");
            try {
                waitForPageLoad(driver);
                // Naukri often injects promo pop-ups on the homepage between navigations.
                PopupHandler.dismissAll(driver);

                // Scroll roughly to where the Early Access widget lives so lazy-loaded
                // content on this section actually renders before we probe for the link.
                // Use a fluent poll to allow document height to grow (Naukri lazy-loads
                // sections as you scroll).
                scrollAndSettle(js, 0.4, Duration.ofSeconds(3));

                // Presence probe with a short timeout - if absent we don't want to burn 20s.
                WebElement viewAll;
                try {
                    viewAll = new WebDriverWait(driver, probeTimeout)
                            .pollingEvery(Duration.ofMillis(500))
                            .until(ExpectedConditions.presenceOfElementLocated(viewAllEarlyAccess));
                } catch (Exception notPresent) {
                    // Not present on this attempt. If we still have retries, refresh and try again;
                    // otherwise gracefully declare "nothing to do".
                    if (attempt < maxAttempts) {
                        logger.warn("'View all' Early Access not visible on attempt " + attempt
                                + " for [" + userKey + "]. Refreshing page and retrying...");
                        safeRefreshAndSettle();
                        continue;
                    }
                    logger.info("⏭️  At this time no more Early Access roles are present for "
                            + userKey + ", so stopping the execution here !");
                    takeScreenshot(driver, "NoEarlyAccessRoles_" + userKey);
                    return false;
                }

                // Present - centre it and click.
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", viewAll);
                // Small settle so scroll animation completes before we test clickability.
                sleepQuietly(300);

                WebElement clickable;
                try {
                    clickable = new WebDriverWait(driver, Duration.ofSeconds(15))
                            .until(ExpectedConditions.elementToBeClickable(viewAllEarlyAccess));
                    clickable.click();
                } catch (Exception clickIntercept) {
                    // Fallback: JS-click bypasses overlays/animations that intercept.
                    logger.warn("Standard click on 'View all' intercepted, retrying via JS: "
                            + clickIntercept.getMessage());
                    js.executeScript("arguments[0].click();", viewAll);
                }
                logger.info("Clicked on 'View all' for Early Access Roles (attempt " + attempt + ")");

                // Verify the target list actually rendered — this is the true success
                // criterion, not just document.readyState.
                waitForPageLoad(driver);
                PopupHandler.dismissAll(driver);
                boolean listRendered = false;
                try {
                    new WebDriverWait(driver, Duration.ofSeconds(15))
                            .pollingEvery(Duration.ofMillis(500))
                            .until(ExpectedConditions.presenceOfElementLocated(earlyAccessTuple));
                    listRendered = true;
                } catch (Exception noTuples) {
                    logger.warn("Clicked 'View all' but role tuples never rendered on attempt "
                            + attempt + ". Reason: " + noTuples.getMessage());
                }
                if (listRendered) {
                    takeScreenshot(driver, "EarlyAccessRolesList_" + userKey);
                    return true;
                }
                // else fall through to retry after refresh.

            } catch (Exception clickErr) {
                logger.warn("'View all' click attempt " + attempt + " failed for [" + userKey
                        + "]: " + clickErr.getMessage());
                takeScreenshot(driver, "ViewAllEarlyAccess_Failed_Attempt" + attempt + "_" + userKey);
            }

            if (attempt < maxAttempts) {
                safeRefreshAndSettle();
            }
        }

        // All attempts exhausted with the link seemingly present but un-clickable.
        // Treat as "no roles" rather than failing the scenario - this step is
        // downstream of the primary flow (resume upload) which already succeeded.
        logger.warn("Unable to click 'View all' Early Access Roles for " + userKey
                + " after " + maxAttempts + " attempts. Treating as no roles available.");
        return false;
    }

    /**
     * Scroll the window to a given fraction of the page height and wait for the
     * document height to stabilise (i.e. lazy-loaded content has rendered).
     */
    private void scrollAndSettle(JavascriptExecutor js, double heightFraction, Duration maxWait) {
        js.executeScript("window.scrollTo(0, Math.floor(document.body.scrollHeight * arguments[0]));",
                heightFraction);
        long deadline = System.currentTimeMillis() + maxWait.toMillis();
        long lastHeight = -1;
        while (System.currentTimeMillis() < deadline) {
            Number h = (Number) js.executeScript("return document.body.scrollHeight;");
            long height = h == null ? 0 : h.longValue();
            if (height == lastHeight && height > 0) {
                return; // stabilised
            }
            lastHeight = height;
            sleepQuietly(300);
        }
    }

    /** Refresh the page and re-settle (readyState + pop-up sweep). Never throws. */
    private void safeRefreshAndSettle() {
        try {
            driver.navigate().refresh();
            waitForPageLoad(driver);
            PopupHandler.dismissAll(driver);
        } catch (Exception recoverEx) {
            logger.warn("Recovery/refresh failed: " + recoverEx.getMessage());
        }
    }

    /**
     * List all Early Access role titles currently visible on the page and log
     * them. Returns the number of roles found for the caller so downstream
     * steps can short-circuit when there is nothing to do.
     *
     * <p>Wait / render strategy:</p>
     * <ul>
     *   <li>Waits for {@code readyState=complete} first.</li>
     *   <li>Sweeps pop-ups (a promo may render after navigation).</li>
     *   <li>Progressively scrolls the page in halves to force lazy-loaded
     *       tuples to render before we count them.</li>
     *   <li>Uses fluent polling (500 ms) on tuple presence with a bounded
     *       timeout so an empty widget returns quickly rather than blocking.</li>
     * </ul>
     */
    public int listEarlyAccessRoles(String userKey) {
        driver = DriverFactory.getDriver();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        waitForPageLoad(driver);
        PopupHandler.dismissAll(driver);

        // Nudge lazy-loaded tuples into the DOM. Naukri renders tuples in batches
        // as the user scrolls, so we do a couple of gentle scrolls before counting.
        for (double frac : new double[]{0.25, 0.5, 0.85, 1.0}) {
            scrollAndSettle(js, frac, Duration.ofSeconds(2));
        }
        js.executeScript("window.scrollTo(0, 0);");
        sleepQuietly(300);

        List<WebElement> tuples;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .pollingEvery(Duration.ofMillis(500))
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(earlyAccessTuple));
            tuples = driver.findElements(earlyAccessTuple);
        } catch (Exception e) {
            tuples = new ArrayList<>();
        }

        if (tuples.isEmpty()) {
            logger.warn("⚠️  No Early Access roles found for user [" + userKey + "]. Nothing to share interest on.");
            return 0;
        }

        logger.info("================ Early Access Roles for [" + userKey + "] ================");
        int idx = 1;
        for (WebElement tuple : tuples) {
            String title;
            try {
                title = tuple.getText().replaceAll("\\s+", " ").trim();
            } catch (StaleElementReferenceException ignore) {
                title = "<stale element>";
            }
            logger.info(String.format("  %02d. %s", idx++, title));
        }
        logger.info("=========================================================================");
        logger.info("Total Early Access roles listed for [" + userKey + "]: " + tuples.size());

        int shareable = driver.findElements(shareInterestBtn).size();
        logger.info("Roles pending 'Share Interest' click: " + shareable);
        return shareable;
    }

    /**
     * Iterate through all Early Access roles and click their "Share Interest"
     * button one by one.
     *
     * <p>Naukri behaviour handled here:</p>
     * <ul>
     *   <li>The very first click triggers a partial re-render of the widget
     *       (button references go stale — feels like a page reload).</li>
     *   <li>Subsequent clicks update the button in-place to "Interest shared".</li>
     *   <li>A promo pop-up ("thank you"/upgrade nudge) can be injected after
     *       clicks. Handled by a per-iteration {@link PopupHandler} sweep.</li>
     * </ul>
     *
     * <p>Strategy:</p>
     * <ul>
     *   <li>Never cache the button list. Re-query on every iteration.</li>
     *   <li>Scroll the next candidate into view, click, then wait until either
     *       the remaining "Share Interest" count drops OR the widget re-renders
     *       (via clicked element going stale).</li>
     *   <li>JS-click fallback if the native click is intercepted.</li>
     *   <li>Two consecutive failed iterations (no state change) triggers a
     *       page refresh recovery before continuing. Never break out on a
     *       single failure — one bad card must not kill the entire batch.</li>
     *   <li>Hard timeout on the overall loop to prevent test hangs.</li>
     * </ul>
     */
    public void shareInterestForAllEarlyAccessRoles(String userKey) {
        driver = DriverFactory.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        int remaining = driver.findElements(shareInterestBtn).size();
        if (remaining == 0) {
            logger.warn("⚠️  No 'Share Interest' buttons available for user [" + userKey + "]. Skipping.");
            return;
        }

        logger.info("Starting to share interest on " + remaining + " Early Access role(s) for [" + userKey + "]");

        int clickedCount = 0;
        int consecutiveFailures = 0;
        final int maxConsecutiveFailures = 3;
        int safetyCap = remaining * 3 + 5; // generous guard against infinite loops
        int iterations = 0;
        final long hardDeadline = System.currentTimeMillis() + Duration.ofMinutes(5).toMillis();

        while (iterations++ < safetyCap) {
            if (System.currentTimeMillis() > hardDeadline) {
                logger.warn("Hit hard 5-minute deadline for 'Share Interest' loop. Stopping.");
                break;
            }

            // Pop-ups can appear between clicks (e.g. "thank you" nudges). Sweep first.
            PopupHandler.dismissAll(driver);

            List<WebElement> buttons = driver.findElements(shareInterestBtn);
            if (buttons.isEmpty()) {
                logger.info("No more 'Share Interest' buttons visible. Done.");
                break;
            }

            int before = buttons.size();
            WebElement btn = buttons.get(0);

            String roleTitle = extractRoleTitle(js, btn);

            boolean clickAttempted = false;
            try {
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
                // Small settle for scroll animation before we assert clickability.
                sleepQuietly(300);
                wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
                clickAttempted = true;
                logger.info(String.format("  ✅ [%d] Clicked 'Share Interest' on: %s", clickedCount + 1, roleTitle));
            } catch (StaleElementReferenceException stale) {
                logger.info("Button went stale (widget re-rendered). Re-querying...");
                consecutiveFailures = 0; // stale = widget re-rendered, likely from a successful click
                sleepQuietly(800);
                continue;
            } catch (Exception clickErr) {
                logger.warn("Normal click failed, retrying via JS. Reason: " + clickErr.getMessage());
                try {
                    js.executeScript("arguments[0].click();", btn);
                    clickAttempted = true;
                    logger.info(String.format("  ✅ [%d] JS-clicked 'Share Interest' on: %s",
                            clickedCount + 1, roleTitle));
                } catch (Exception jsErr) {
                    logger.error("JS click also failed for role [" + roleTitle + "]: " + jsErr.getMessage());
                    // Do NOT break — attempt to recover and continue with remaining roles.
                    consecutiveFailures++;
                    if (consecutiveFailures >= maxConsecutiveFailures) {
                        logger.error("Reached " + maxConsecutiveFailures + " consecutive click failures. "
                                + "Refreshing page to recover...");
                        safeRefreshAndSettle();
                        consecutiveFailures = 0;
                    }
                    continue;
                }
            }

            // Wait for either: (a) remaining Share Interest buttons to drop, OR
            // (b) the widget to re-render (first click reload case).
            final int beforeFinal = before;
            boolean stateChanged;
            try {
                stateChanged = new WebDriverWait(driver, Duration.ofSeconds(15))
                        .pollingEvery(Duration.ofMillis(400))
                        .until(d -> d.findElements(shareInterestBtn).size() < beforeFinal);
            } catch (Exception waitErr) {
                logger.warn("Timed out waiting for button count to decrease after click. Proceeding.");
                stateChanged = false;
            }

            if (clickAttempted && stateChanged) {
                clickedCount++;
                consecutiveFailures = 0;
            } else if (clickAttempted) {
                // Click didn't cause an observable state change — could be a
                // silent failure. Guard against infinite spinning on the same card.
                consecutiveFailures++;
                logger.warn("Click on [" + roleTitle + "] did not reduce remaining count. "
                        + "Consecutive no-progress iterations: " + consecutiveFailures);
                if (consecutiveFailures >= maxConsecutiveFailures) {
                    logger.error("No progress after " + maxConsecutiveFailures + " clicks. "
                            + "Refreshing page to recover...");
                    safeRefreshAndSettle();
                    consecutiveFailures = 0;
                }
            }

            // Small settle time between clicks (widget animations).
            sleepQuietly(600);
        }

        int sharedNow = driver.findElements(interestSharedBtn).size();
        int leftover = driver.findElements(shareInterestBtn).size();
        logger.info("Finished sharing interest for [" + userKey + "]. Clicked=" + clickedCount
                + ", InterestShared visible=" + sharedNow + ", Remaining Share Interest=" + leftover);
        takeScreenshot(driver, "EarlyAccessRoles_Done_" + userKey);
    }

    /** Extracts the role title from the card containing the given button. Best-effort, never throws. */
    private String extractRoleTitle(JavascriptExecutor js, WebElement btn) {
        try {
            WebElement card = (WebElement) js.executeScript(
                    "return arguments[0].closest(\"div.tlc__tuple\") || arguments[0].closest(\"[class*='tuple']\");",
                    btn);
            if (card != null) {
                String txt = card.getText();
                if (txt != null && !txt.isEmpty()) {
                    return txt.split("\\r?\\n")[0].trim();
                }
            }
        } catch (Exception ignored) {
            // fall through
        }
        return "<unknown role>";
    }
}
