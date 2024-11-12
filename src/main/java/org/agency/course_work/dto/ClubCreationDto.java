package org.agency.course_work.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.agency.course_work.enums.Stadium;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link org.agency.course_work.entity.Club}
 */
public record ClubCreationDto(@Size(max = 255) @NotBlank String name, @NotNull Stadium stadium,
                              @Size(max = 255) @NotBlank String country,
                              @NotNull @Positive BigDecimal budget) implements Serializable {
}