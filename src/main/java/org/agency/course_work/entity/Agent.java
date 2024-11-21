package org.agency.course_work.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.agency.course_work.enums.CommissionRate;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table (name = "agents")
@Setter
@Getter
@ToString
@Where(clause = "is_deleted = false")
public class Agent extends BaseEntity {
    @Column(name = "first_name")
    private String firstName;

    @Column
    private String lastName;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private CommissionRate commissionRate;

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Player> players = new HashSet<>();

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Contract> contracts = new HashSet<>();
}
