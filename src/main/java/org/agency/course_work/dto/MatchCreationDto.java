package org.agency.course_work.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import org.agency.course_work.enums.City;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for {@link org.agency.course_work.entity.Match}
 */
public record MatchCreationDto(@NotNull @PastOrPresent LocalDate date, @NotNull City city,
                               @Pattern(regexp = "^\\d{1,2}:\\d{1,2}$",
                                       message = "Score must be in the format 'X:Y', where X and Y are numbers between 0 and 99.")
                               @NotBlank String score,
                               @NotNull List<Long> clubIds) implements Serializable {
}