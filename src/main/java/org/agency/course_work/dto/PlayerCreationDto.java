package org.agency.course_work.dto;

import jakarta.validation.constraints.*;
import org.agency.course_work.enums.PlayerPosition;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link org.agency.course_work.entity.Player}
 */
public record PlayerCreationDto(@Size(max = 255) @NotBlank String name, @Size(max = 255) @NotBlank String surname,
                                @Min(10) @Max(99) @Positive int age, @NotNull PlayerPosition position,
                                @Size(max = 255) @NotBlank String nationality,
                                @NotNull @Positive BigDecimal value,
                                @NotNull Long agentId) implements Serializable {
}