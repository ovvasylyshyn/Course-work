package org.agency.course_work.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.*;
import java.time.LocalDate;

@Entity
@Table (name = "contracts")
@Setter
@Getter
@ToString
public class Contract extends BaseEntity {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal salary;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;
}
