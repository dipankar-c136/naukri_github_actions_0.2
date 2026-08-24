package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.List;

/**
 * Best-effort dismisser for Naukri promotional / post-purchase pop-ups.
 *
 * <p>Design:</p>
 * <ul>
 *   <li>Maintains a registry of known pop-ups (container CSS + close-button CSS).</li>
 *   <li>Never throws - a missing pop-up is normal and must not fail a scenario.</li>
 *   <li>Uses JS click (avoids overlay/z-index/animation issues) and, as a
 *       fallback, removes the container node from the DOM.</li>
 *   <li>Sweeps twice - closing one pop-up can reveal another queued behind it.</li>
 * </ul>
 *
 * <p>Adding a new pop-up = adding one line to {@link #KNOWN_POPUPS}.</p>
 */
public final class PopupHandler {

    private static final Logger logger = LogManager.getLogger(PopupHandler.class);

    private PopupHandler() { /* utility */ }

    /**
     * Registry of known Naukri pop-ups.
     * <p>
     * The generic entry leverages Naukri's own naming convention -
     * every widget pop-up container ends in {@code -popup-wdgt-container}
     * and its close control is a child element with class {@code cross-icon}.
     * This is targeted enough to avoid false positives on real UI.
     */
    private static final List<PopupSpec> KNOWN_POPUPS = Arrays.asList(
            // 1. Specific: Naukri360Pro post-purchase pop-up (confirmed sample).
            new PopupSpec(
                    "Naukri360Pro post-purchase",
                    ".n360pro-post-purchase-popup-wdgt-container",
                    ".n360pro-post-purchase-popup-wdgt-container .cross-icon"),

            // 2. Generic Naukri widget pop-ups following the *-popup-wdgt-container convention.
            new PopupSpec(
                    "Naukri generic widget pop-up",
                    "[class*='-popup-wdgt-container']",
                    "[class*='-popup-wdgt-container'] .cross-icon"),

            // 3. Naukri chatbot drawer (right-side "Welcome to Naukri" assistant).
            //    IDs are dynamically generated (e.g. _b8om3ozmwDrawer), so we key
            //    off the stable class names. Close control lives inside .chatbot_Nav.
            new PopupSpec(
                    "Naukri chatbot drawer",
                    ".chatbot_Drawer",
                    ".chatbot_Drawer .chatbot_Nav .chatBot-ic-cross"),

            // 4. Naukri chatbot overlay (dark backdrop shown behind the drawer).
            //    Sometimes lingers and blocks clicks even after the drawer is gone.
            new PopupSpec(
                    "Naukri chatbot overlay",
                    ".chatbot_Overlay.show",
                    ".chatbot_Overlay.show")
    );

    /**
     * Best-effort: try to dismiss every known pop-up currently on the page.
     * Safe to call at any time. Never throws.
     */
    public static void dismissAll(WebDriver driver) {
        if (driver == null) return;

        for (int pass = 1; pass <= 2; pass++) {
            boolean dismissedSomething = false;
            for (PopupSpec spec : KNOWN_POPUPS) {
                if (tryDismiss(driver, spec)) {
                    dismissedSomething = true;
                }
            }
            if (!dismissedSomething) {
                break; // nothing left to close
            }
        }
    }

    private static boolean tryDismiss(WebDriver driver, PopupSpec spec) {
        try {
            List<WebElement> containers = driver.findElements(By.cssSelector(spec.containerCss));
            if (containers.isEmpty()) {
                return false;
            }

            logger.info("Detected pop-up [{}] - attempting to dismiss ({} container(s))",
                    spec.name, containers.size());

            // 1. Preferred path: JS-click the close control.
            List<WebElement> closes = driver.findElements(By.cssSelector(spec.closeCss));
            for (WebElement close : closes) {
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", close);
                    logger.info("Pop-up [{}] dismissed via close button", spec.name);
                    return true;
                } catch (Exception ignored) {
                    // try next close element / fall through to DOM removal
                }
            }

            // 2. Fallback: remove the container node(s) from the DOM.
            for (WebElement container : containers) {
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].remove();", container);
                    logger.warn("Pop-up [{}] close button not usable - removed container from DOM",
                            spec.name);
                    return true;
                } catch (Exception ignored) {
                    // try next container
                }
            }
        } catch (Exception e) {
            // Never let pop-up handling break the test flow.
            logger.debug("PopupHandler swallowed error for [{}]: {}", spec.name, e.getMessage());
        }
        return false;
    }

    /** Immutable descriptor for one known pop-up. */
    private static final class PopupSpec {
        final String name;
        final String containerCss;
        final String closeCss;

        PopupSpec(String name, String containerCss, String closeCss) {
            this.name = name;
            this.containerCss = containerCss;
            this.closeCss = closeCss;
        }
    }
}
