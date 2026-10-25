package com.acme.wo.internal.data;

import com.acme.wo.model.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

@Repository
public interface WorkoutRepository extends JpaRepository<WorkoutEntity, Long> {
}
