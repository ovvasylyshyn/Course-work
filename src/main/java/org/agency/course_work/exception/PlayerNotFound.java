package org.agency.course_work.exception;

import jakarta.persistence.EntityNotFoundException;

public class PlayerNotFound extends EntityNotFoundException{
    public PlayerNotFound(String message) {
        super(message);
    }
}
