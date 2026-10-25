package com.acme.wo.internal.session;

import com.acme.wo.model.Session;
import com.acme.wo.model.SessionExecution;

public interface SessionPersistor {
    Session persist(Session session);

    void updateSessionExecution(Long sessionId, SessionExecution updated);
}
