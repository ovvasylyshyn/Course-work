package org.agency.course_work.dto;

import org.agency.course_work.enums.City;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MathesWithClubsDto(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDate date, City city,
                                 String score, List<String> clubNames) implements Serializable {
}
