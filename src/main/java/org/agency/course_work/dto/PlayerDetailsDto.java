package org.agency.course_work.dto;

import org.agency.course_work.enums.PlayerPosition;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record PlayerDetailsDto(
        Long playerId,
        String playerName,
        int age,
        String nationality,
        PlayerPosition position,
        BigDecimal value,
        String agentName,
        String agentPhone,
        String clubName,
        LocalDate contractStartDate,
        LocalDate contractEndDate,
        BigDecimal contractSalary
) implements Serializable {

}

