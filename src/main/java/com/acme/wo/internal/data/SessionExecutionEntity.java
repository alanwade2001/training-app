package com.acme.wo.internal.data;

import com.acme.wo.model.ExecutionState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SessionExecutionEntity {

    int exerciseIndex;
    int currentSet;

    @Enumerated(EnumType.STRING)
    ExecutionState state;
}
