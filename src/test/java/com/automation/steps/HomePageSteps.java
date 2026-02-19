package com.automation.steps;

import com.automation.config.ConfigReader;
import com.automation.pages.HomePage;
import com.automation.pages.LoginPage;
import com.automation.utils.GmailUtils;
import io.cucumber.java.en.*;
import org.testng.Assert;

public class HomePageSteps {
    HomePage homePage = new HomePage();


    @And("I should see the user dashboard")
    public void i_check_dashboard() {
        Assert.assertTrue(homePage.isDashboardDisplayed(), "Dashboard not displayed (URL does not contain 'mnjuser')");
    }

    @And("I clicked on the <View Profile> button")
    public void iClickedOnTheViewProfileButton(){
        homePage.clickViewProfile();
    }

}
