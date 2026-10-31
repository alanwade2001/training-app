Feature: Programme upload validation

  Scenario: Correct an invalid nested exercise recovery

    Given Programme YAML has exercise recovery of 14 seconds

    When I upload the Programme

    Then the Programme upload is rejected
    And validation feedback is returned
    And the invalid Programme is not persisted

    When I change the exercise recovery to 15 seconds
    And I upload the Programme

    Then the Programme upload succeeds
    And the Programme is persisted
    And its complete hierarchy is persisted