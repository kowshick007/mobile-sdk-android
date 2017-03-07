Feature: Video Ad Display
  A Video Ad should be displayed.

  Scenario: Video Ad is displayed
    Given I wait for the "MainActivity" screen to appear
    Then I wait up to 30 seconds for "onAdLoaded" to appear
    And I press "play_button"
    Given I have a webview available
