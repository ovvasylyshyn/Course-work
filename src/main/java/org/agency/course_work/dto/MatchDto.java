package org.agency.course_work.dto;

import org.agency.course_work.enums.City;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link org.agency.course_work.entity.Match}
 */
public record MatchDto(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime date, City city,
                       String score) implements Serializable {
}