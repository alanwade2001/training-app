package com.acme.wo.internal.programme;

import com.acme.wo.internal.data.ProgrammeEntity;
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

        return new Programme(entity.getId(),entity.getName(),weekList);
    }
}
