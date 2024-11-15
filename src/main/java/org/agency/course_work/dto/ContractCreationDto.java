package org.agency.course_work.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link org.agency.course_work.entity.Contract}
 */
public record ContractCreationDto(@NotNull @PastOrPresent LocalDate startDate,
                                  @NotNull @FutureOrPresent LocalDate endDate,
                                  @NotNull @Positive BigDecimal salary,
                                  @NotNull Long playerId,
                                  @NotNull Long agentId,
                                  Long clubId) implements Serializable {
}