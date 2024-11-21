package org.agency.course_work.dto;

import org.agency.course_work.enums.CommissionRate;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link org.agency.course_work.entity.Agent}
 */
public record AgentDto(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, String firstName, String lastName,
                       String phoneNumber, CommissionRate commissionRate, Boolean isDeleted) implements Serializable {
}