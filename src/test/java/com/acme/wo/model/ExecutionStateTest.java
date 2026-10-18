package com.acme.wo.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionStateTest {

    @Test
    public void testCanHappyPath() {
        Assertions.assertThat(ExecutionState.READY.canStart()).isTrue();

        Assertions.assertThat(ExecutionState.ACTIVE.canComplete()).isTrue();

        Assertions.assertThat(ExecutionState.RESTING.canContinue()).isTrue();

        Assertions.assertThat(ExecutionState.EXERCISE_COMPLETE.canAdvance()).isTrue();

        Assertions.assertThat(ExecutionState.SESSION_COMPLETE.canEnd()).isTrue();

    }

}