package org.agency.course_work.dto;

import lombok.Setter;
import org.agency.course_work.enums.CommissionRate;
import org.agency.course_work.enums.PlayerPosition;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for {@link org.agency.course_work.entity.Player}
 */

public record PlayerAgentDto(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, String name, String surname, int age, PlayerPosition position, String nationality, BigDecimal value, Long agentId, LocalDateTime agentCreatedAt, LocalDateTime agentUpdatedAt, String agentFirstName, String agentLastName, String agentPhoneNumber, CommissionRate agentCommissionRate) implements Serializable {
  }