Feature: Complete a programme workout

  Background:
    Given a programme contains an incomplete planned workout

  Scenario: Return from a programme workout preview
    When I open Home
    And I open Programmes
    And I select the Programme
    And I select the Week
    And I preview the Workout
    And I return to the Week
    Then I am viewing the originating programme Week

  Scenario: Complete a programme workout
    Given the Week has 0 completed workouts
    When I open Home
    And I open Programmes
    And I select the Programme
    And I select the Week
    And I preview the Workout
    And I return to the Week
    And I preview the Workout again
    And I start the Session
    And I complete the Session
    And I select DONE
    Then I am returned to the originating programme Week
    And the Workout is complete
    And the Week has 1 completed workout
    And the Session appears in History
    And the Session Detail is readable
    And the historical exercise snapshot is present

  Scenario: Repeat a completed programme workout
    Given the Workout has already been completed once
    When I preview the Workout
    And I start the Session
    And I complete the Session
    And I select DONE
    Then the Week still has 1 completed workout
    And the Workout has 2 completed Sessions
    And both Sessions appear in History