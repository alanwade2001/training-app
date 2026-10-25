package com.acme.wo.internal.programme;

import com.acme.wo.internal.data.*;
import com.acme.wo.internal.workout.WorkoutMapper;
import com.acme.wo.internal.workout.WorkoutPersistor;
import com.acme.wo.model.Programme;
import com.acme.wo.model.Week;
import com.acme.wo.model.Workout;
import com.acme.wo.model.WorkoutExercise;
import com.acme.wo.service.ProgrammeSource;
import com.acme.wo.service.WorkoutSource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Component
public class JpaProgrammePersistor implements ProgrammeSource, ProgrammePersistor {

    private final ProgrammeRepository programmeRepository;

    public JpaProgrammePersistor(ProgrammeRepository programmeRepository) {
        this.programmeRepository = programmeRepository;
    }


    @Override
    public List<Programme> persist(List<Programme> programmes) {
        return programmes.stream().map(this::persist).toList();
    }

    @Override
    public Programme persist(Programme p) {
        var programmeEntity = ProgrammeEntity.builder()
                .name(p.name())
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
                }
        ).toList();

        programmeEntity.setWeeks(weekEntities);

        var savedProgrammeEntity = programmeRepository.save(programmeEntity);

        return new Programme(savedProgrammeEntity.getId()
                , programmeEntity.getName()
                , p.weeks());

    }

    @Override
    public List<Programme> getProgrammes() {
        return programmeRepository.findAll().stream().map(p->{
            var weekList = new ArrayList<Week>();
            for( var weekEntity : p.getWeeks()) {
                var workouts = weekEntity.getWorkouts().stream().map(WorkoutMapper::fromEntity).toList();
                var week = new Week(weekEntity.getNumber(),  workouts);
                weekList.add(week);
            }

            return new Programme(p.getId(),p.getName(),weekList);

        }).toList();
    }
}
