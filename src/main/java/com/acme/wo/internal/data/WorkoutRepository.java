package com.acme.wo.internal.data;

import com.acme.wo.model.Workout;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutRepository extends JpaRepository<WorkoutEntity, Long> {
    List<WorkoutEntity> findByWeekIsNull();

    List<WorkoutEntity> findAllByWeekIsNull();

    Optional<WorkoutEntity> findByName(String name);
}
