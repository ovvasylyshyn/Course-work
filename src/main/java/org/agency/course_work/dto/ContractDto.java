package org.agency.course_work.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for {@link org.agency.course_work.entity.Contract}
 */
public record ContractDto(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDate startDate,
                          LocalDate endDate, BigDecimal salary) implements Serializable {
}