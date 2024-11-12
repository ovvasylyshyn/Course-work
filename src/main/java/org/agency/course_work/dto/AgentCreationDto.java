package org.agency.course_work.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.agency.course_work.enums.CommissionRate;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * DTO for {@link org.agency.course_work.entity.Agent}
 */
public record AgentCreationDto(@Size(max = 255) @NotBlank(message = "Can`t be blank") String firstName,
                               @Size(max = 255) @NotBlank(message = "Can`t be blank") String lastName,
                               @NotBlank @Length(min = 7, max = 15) String phoneNumber,
                               @NotNull CommissionRate commissionRate) implements Serializable {
}