#@login
#Feature: Naukri Resume Re-upload with Auto-OTP Bypass
#
#  Scenario: Login successfully with Auto-OTP Bypass
#    Given I am on the Naukri login page
#    When I enter valid credentials
#    And I click the login button
#    Then I should handle OTP verification if requested
#    And I should see the user dashboard
#    And I clicked on the <View Profile> button
#    Then I should see my profile page
#    And I navigate to the resume management section
#    When I remove the already uploaded resume
#    And I upload my resume again
#    Then I should see a success message

@login
Feature: Naukri Resume Re-upload with Auto-OTP Bypass

  # Data-driven: this scenario runs once per row in the Examples table.
  # <userKey> maps to USER1_* / USER2_* entries in config.properties
  Scenario Outline: Login and re-upload resume for <userKey>
    Given I am on the Naukri login page
    When I enter valid credentials for "<userKey>"
    And I click the login button
    Then I should handle OTP verification if requested for "<userKey>"
    And I should see the user dashboard
    And I clicked on the <View Profile> button
    Then I should see my profile page
    And I navigate to the resume management section
    When I remove the already uploaded resume
    And I upload my resume again for "<userKey>"
    Then I should see a success message

    Examples:
      | userKey |
      | USER1   |
      | USER2   |