package org.agency.course_work.exception;

import jakarta.persistence.EntityNotFoundException;

public class MatchNotFound extends EntityNotFoundException {
    public MatchNotFound(String message) {
        super(message);
    }
}
