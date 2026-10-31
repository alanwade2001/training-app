Feature: Complete a standalone workout

  Background:
    Given a standalone Workout exists

  Scenario: Preview and return from a standalone Workout
    When I open Workouts
    And I preview the standalone Workout
    And I return from the Workout preview
    Then I am returned to Workouts

  Scenario: Complete a standalone Workout
    When I open Workouts
    And I preview the standalone Workout
    And I start the Session
    And I complete the Session
    And I select DONE
    Then I am returned to Workouts
    And the Session appears in History
    And the Session Detail is readable
    And the historical exercise snapshot is present