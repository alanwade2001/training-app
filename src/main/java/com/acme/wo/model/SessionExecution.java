package com.acme.wo.model;

public record SessionExecution(Long sessionId,
                               int exerciseIndex,
                               int currentSet,
                               ExecutionState state) {
}
