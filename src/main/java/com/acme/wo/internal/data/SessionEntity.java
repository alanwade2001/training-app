package com.acme.wo.internal.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SessionEntity {
    @Id @GeneratedValue
    private Long id;

    private Long workoutId;
    private String workoutName;

    @Embedded
    private SessionExecutionEntity currentSessionExecution;
}
