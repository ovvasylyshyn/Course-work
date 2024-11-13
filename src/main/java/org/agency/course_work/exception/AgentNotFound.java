package org.agency.course_work.exception;

import jakarta.persistence.EntityNotFoundException;

public class AgentNotFound extends EntityNotFoundException{
    public AgentNotFound(String message) {
        super(message);
    }
}
