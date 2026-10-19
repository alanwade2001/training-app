package com.acme.wo.internal.data;

import com.acme.wo.model.ExecutionState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SessionExecutionEntity {

    int exerciseIndex;
    int currentSet;
    Instant stateStartedAt;

    @Enumerated(EnumType.STRING)
    ExecutionState state;
}
