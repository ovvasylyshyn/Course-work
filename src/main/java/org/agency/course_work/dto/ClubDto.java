package org.agency.course_work.dto;

import org.agency.course_work.enums.Stadium;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for {@link org.agency.course_work.entity.Club}
 */
public record ClubDto(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, String name, Stadium stadium, String country, BigDecimal budget) implements Serializable {
  }