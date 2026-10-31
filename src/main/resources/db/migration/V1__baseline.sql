
create sequence programme_entity_seq start with 1 increment by 50;

create sequence session_entity_seq start with 1 increment by 50;

create sequence session_exercise_entity_seq start with 1 increment by 50;

create sequence week_entity_seq start with 1 increment by 50;

create sequence workout_entity_seq start with 1 increment by 50;

create sequence workout_exercise_entity_seq start with 1 increment by 50;

create table programme_entity (
                                  id bigint not null,
                                  name varchar(255),
                                  primary key (id)
);

create table session_entity (
                                current_set integer,
                                exercise_index integer,
                                id bigint not null,
                                started_at timestamp(6) with time zone,
                                state_started_at timestamp(6) with time zone,
                                updated_at timestamp(6) with time zone,
                                workout_id bigint,
                                state varchar(255) check ((state in ('READY','ACTIVE','RESTING','EXERCISE_COMPLETE','SESSION_COMPLETE'))),
                                workout_name varchar(255),
                                primary key (id)
);

create table session_exercise_entity (
                                         recovery_in_secs integer not null,
                                         reps integer not null,
                                         sequence integer not null,
                                         sets integer not null,
                                         id bigint not null,
                                         session_id bigint not null,
                                         name varchar(255),
                                         primary key (id)
);

create table week_entity (
                             number integer not null,
                             id bigint not null,
                             programme_id bigint not null,
                             primary key (id)
);

create table workout_entity (
                                planned_day smallint check ((planned_day between 0 and 6)),
                                id bigint not null,
                                week_id bigint,
                                name varchar(255),
                                primary key (id)
);

create table workout_exercise_entity (
                                         quantity integer not null,
                                         recovery_in_secs integer not null,
                                         reps integer not null,
                                         sequence integer not null,
                                         sets integer not null,
                                         units smallint check ((units between 0 and 3)),
                                         id bigint not null,
                                         workout_id bigint not null,
                                         name varchar(255),
                                         primary key (id)
);

alter table if exists session_exercise_entity
    add constraint FKj0xn18x99n4ijjbddob5ote19
    foreign key (session_id)
    references session_entity;

alter table if exists week_entity
    add constraint FK9cndsyt6o3j0tqutkekxwgebl
    foreign key (programme_id)
    references programme_entity;

alter table if exists workout_entity
    add constraint FK6qq7wybqfjgqls7g3tbugxfyh
    foreign key (week_id)
    references week_entity;

alter table if exists workout_exercise_entity
    add constraint FK10ohghemlr8ymyd4jllcjg6p8
    foreign key (workout_id)
    references workout_entity;