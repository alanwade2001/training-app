package com.acme.wo.internal.data;

import com.acme.wo.model.SessionExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface SessionExerciseRepos extends JpaRepository<SessionExerciseEntity, Long> {
    List<SessionExercise> findAllBySessionId(Long sessionId);
}
