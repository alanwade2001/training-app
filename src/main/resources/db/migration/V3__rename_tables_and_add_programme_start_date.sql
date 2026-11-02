-- Rename tables

ALTER TABLE programme_entity
    RENAME TO programme;

ALTER TABLE week_entity
    RENAME TO week;

ALTER TABLE workout_entity
    RENAME TO workout;

ALTER TABLE workout_exercise_entity
    RENAME TO workout_exercise;

ALTER TABLE session_entity
    RENAME TO "session";

ALTER TABLE session_exercise_entity
    RENAME TO session_exercise;


-- Rename sequences

ALTER SEQUENCE programme_entity_seq
    RENAME TO programme_seq;

ALTER SEQUENCE week_entity_seq
    RENAME TO week_seq;

ALTER SEQUENCE workout_entity_seq
    RENAME TO workout_seq;

ALTER SEQUENCE workout_exercise_entity_seq
    RENAME TO workout_exercise_seq;

ALTER SEQUENCE session_entity_seq
    RENAME TO session_seq;

ALTER SEQUENCE session_exercise_entity_seq
    RENAME TO session_exercise_seq;
