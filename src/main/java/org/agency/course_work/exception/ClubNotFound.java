package org.agency.course_work.exception;

import jakarta.persistence.EntityNotFoundException;

public class ClubNotFound extends EntityNotFoundException {
    public ClubNotFound(String clubNotFound) {
        super(clubNotFound);
    }
}
