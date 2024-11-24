package org.agency.course_work.exception;

import jakarta.persistence.EntityNotFoundException;

public class UserNotFound extends EntityNotFoundException {
    public UserNotFound(String message) {
        super(message);
    }
}
