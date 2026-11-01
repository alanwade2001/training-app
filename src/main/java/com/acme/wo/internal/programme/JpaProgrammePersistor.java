package com.acme.wo.internal.programme;

import com.acme.wo.internal.data.*;
import com.acme.wo.internal.workout.WorkoutMapper;
import com.acme.wo.model.Programme;
import com.acme.wo.model.Week;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Transactional
@Component
public class JpaProgrammePersistor implements ProgrammePersistor {

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
    public void deleteById(Long programmeId) {
        programmeRepository.deleteById(programmeId);
    }

    @Override
    public List<Programme> findAll() {
        return getProgrammes();
    }

    @Override
    public Optional<Programme> findById(Long programmeId) {
        return programmeRepository.findById(programmeId).map(ProgrammeMapper::mapFromEntity);
    }

    public List<Programme> getProgrammes() {
        return programmeRepository.findAll().stream().map(ProgrammeMapper::mapFromEntity).toList();
    }
}
