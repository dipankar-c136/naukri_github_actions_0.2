package com.automation.steps;

import com.automation.config.ConfigReader;
import com.automation.pages.HomePage;
import com.automation.pages.LoginPage;
import com.automation.utils.GmailUtils;
import io.cucumber.java.en.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

public class HomePageSteps {
    private static final Logger logger = LogManager.getLogger(HomePageSteps.class);

    HomePage homePage = new HomePage();

    // Shared flag between "list" step and "share interest" step so we can
    // gracefully skip clicking when no early-access roles are available.
    private int earlyAccessAvailableCount = -1;
    // Set to false when the "View all" widget is absent (user already applied to
    // every available role) - later steps then short-circuit gracefully.
    private boolean earlyAccessSectionOpened = true;


    @And("I should see the user dashboard")
    public void i_check_dashboard() {
        Assert.assertTrue(homePage.isDashboardDisplayed(), "Dashboard not displayed (URL does not contain 'mnjuser')");
    }

    @And("I clicked on the <View Profile> button")
    public void iClickedOnTheViewProfileButton(){
        homePage.clickViewProfile();
    }

    @When("I navigate back to the homepage")
    public void iNavigateBackToTheHomepage() {
        homePage.navigateToHomepage();
    }

    @And("I click on the View all button to view all the Early access roles for {string}")
    public void iClickOnTheViewAllButtonForEarlyAccessRoles(String userKey) {
        earlyAccessSectionOpened = homePage.clickViewAllEarlyAccessRoles(userKey);
        if (!earlyAccessSectionOpened) {
            earlyAccessAvailableCount = 0;
        }
    }

    @And("I list down all the Early access roles available for {string}")
    public void iListDownAllEarlyAccessRolesFor(String userKey) {
        if (!earlyAccessSectionOpened) {
            logger.info("⏭️  Skipping listing of Early Access roles for [{}] — 'View all' widget was not present.",
                    userKey);
            return;
        }
        earlyAccessAvailableCount = homePage.listEarlyAccessRoles(userKey);
    }

    @And("I click on the Share interest for each of the Early access roles available for {string}")
    public void iClickShareInterestForEachEarlyAccessRole(String userKey) {
        if (!earlyAccessSectionOpened || earlyAccessAvailableCount == 0) {
            logger.info("⏭️  Skipping 'Share Interest' clicks for [{}] — no Early Access roles were available.",
                    userKey);
            return;
        }
        homePage.shareInterestForAllEarlyAccessRoles(userKey);
    }

}
