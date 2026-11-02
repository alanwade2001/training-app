package com.acme.wo.internal.programme;

import com.acme.wo.internal.data.ProgrammeEntity;
import com.acme.wo.internal.data.WeekEntity;
import com.acme.wo.internal.workout.WorkoutMapper;
import com.acme.wo.model.Programme;
import com.acme.wo.model.Week;

import java.util.ArrayList;

public class ProgrammeMapper {

    public static Programme mapFromEntity(ProgrammeEntity entity) {
        var weekList = new ArrayList<Week>();
        for( var weekEntity : entity.getWeeks()) {
            var workouts = weekEntity.getWorkouts().stream().map(WorkoutMapper::fromEntity).toList();
            var week = new Week(weekEntity.getNumber(),  workouts);
            weekList.add(week);
        }

        return new Programme(entity.getId(),entity.getName(), entity.getStartDate(),weekList);
    }

    public static ProgrammeEntity mapToEntity(Programme p) {
        var programmeEntity = ProgrammeEntity.builder()
                .id(p.id())
                .name(p.name())
                .startDate(p.startDate())
                .build();

        var weekEntities = p.weeks().stream().map(week -> {
            var weekEntity = WeekEntity.builder()
                    .number(week.number())
                    .programme(programmeEntity)
                    .build();

            var workoutEntities = week.workouts().stream().map(WorkoutMapper::toEntity).toList();
            workoutEntities.forEach(workout -> {workout.setWeek(weekEntity);});

            weekEntity.setWorkouts(workoutEntities);

            return weekEntity;
        }).toList();

        programmeEntity.setWeeks(weekEntities);

        return programmeEntity;
    }
}
