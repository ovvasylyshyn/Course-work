package org.agency.course_work.dto;

import org.agency.course_work.enums.PlayerPosition;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for {@link org.agency.course_work.entity.Player}
 */
public record PlayerDto(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, String name, String surname, Integer age,
                        PlayerPosition position, String nationality, BigDecimal value) implements Serializable {
}