Feature: Delete a Programme with historical training

  Scenario: Delete a Programme after completing one of its Workouts
    Given a Programme exists with a planned Workout
    And I have completed a Session for the programme Workout

    When I delete the Programme

    Then the Programme is no longer available


    When I open History

    Then the historical Session remains available
    And the historical Session retains its workout name
    And the Session Detail is readable
    And the historical exercise snapshot is present