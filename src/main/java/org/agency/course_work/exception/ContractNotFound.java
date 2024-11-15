package org.agency.course_work.exception;

import jakarta.persistence.EntityNotFoundException;

public class ContractNotFound extends EntityNotFoundException {
    public ContractNotFound(String contractNotFound) {
        super(contractNotFound);
    }
}
