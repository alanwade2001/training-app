package com.acme.wo.model;

public enum ExecutionState {
    READY,
    ACTIVE,
    RESTING,
    EXERCISE_COMPLETE,
    SESSION_COMPLETE;

//    public boolean canTransitionTo(ExecutionState next) {
//        return switch (this) {
//            case READY ->
//                    next == ACTIVE;
//
//            case ACTIVE ->
//                    next == RESTING ||
//                            next == EXERCISE_COMPLETE ||
//                            next == SESSION_COMPLETE;
//
//            case RESTING ->
//                    next == READY;
//
//            case EXERCISE_COMPLETE -> next == RESTING ||
//                    next == SESSION_COMPLETE ||
//                    next == READY;
//            case SESSION_COMPLETE ->  false;
//        };
//    }

    public boolean canStart() {
        return this == READY ;
    }

    public boolean canAdvance() {
        return this == EXERCISE_COMPLETE;
    }

    public boolean canComplete() {
        return this == ACTIVE;
    }



    public boolean canContinue() {
        return this == RESTING
                || this == EXERCISE_COMPLETE;
    }

    public boolean canEnd() {
        return this == EXERCISE_COMPLETE;
    }

    public boolean isComplete() {
        return this == SESSION_COMPLETE;
    }

}
