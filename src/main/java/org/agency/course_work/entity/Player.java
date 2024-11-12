package org.agency.course_work.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.agency.course_work.enums.PlayerPosition;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "players")
@Setter
@Getter
@ToString
//@AllArgsConstructor
public class Player extends BaseEntity {
    private String name;
    private String surname;
    private int age;

    @Enumerated(EnumType.STRING)
    private PlayerPosition position;

    private String nationality;
    private BigDecimal value;

    @ManyToOne
    private Agent agent;

    @ManyToOne
    private Club club;

    @OneToMany(mappedBy = "player")
    private Set<Contract> contracts = new HashSet<>();
}
