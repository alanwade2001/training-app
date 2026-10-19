package com.acme.wo.model;

import java.time.Instant;

public record SessionExecution(int exerciseIndex,
                               int currentSet,
                               Instant stateStartedAt,
                               ExecutionState state) {


}
