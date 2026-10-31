Feature: Delete a standalone Workout with historical training

  Scenario: Delete a standalone Workout after completing it
    Given a standalone Workout exists
    And I have completed a Session for the standalone Workout

    When I delete the standalone Workout

    Then the Workout is no longer available in Workouts

    When I open History

    Then the historical Session remains available
    And the historical Session retains its workout name
    And the Session Detail is readable
    And the historical exercise snapshot is present