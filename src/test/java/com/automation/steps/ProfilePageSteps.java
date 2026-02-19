package com.automation.steps;

import com.automation.config.ConfigReader;
import com.automation.pages.HomePage;
import com.automation.pages.LoginPage;
import com.automation.pages.ProfilePage;
import com.automation.utils.GmailUtils;
import io.cucumber.java.en.*;
import org.testng.Assert;

public class ProfilePageSteps {
    ProfilePage profilePage = new ProfilePage();

    @Then("I should see my profile page")
    public void iShouldSeeMyProfilePage() throws Throwable{
        profilePage.isProfilePageDisplayed();
    }

    @And("I navigate to the resume management section")
    public void iNavigateToTheResumeManagementSection() throws Throwable {
        profilePage.scrollToResumeSection();
    }

    @When("I remove the already uploaded resume")
    public void iRemoveTheAlreadyUploadedResume() throws Throwable {
        profilePage.deleteResume();
    }

    @And("I upload my resume again")
    public void iUploadMyResumeAgain() throws Throwable {
        profilePage.uploadResume("CV_Dipankar_Chakraborty_2026.pdf");
    }

    @Then("I should see a success message")
    public void i_should_see_success_message() {
        profilePage.isUploadSuccessful();
    }
}
