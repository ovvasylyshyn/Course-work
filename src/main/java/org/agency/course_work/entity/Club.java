package org.agency.course_work.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.agency.course_work.enums.Stadium;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Table (name = "clubs")
@Setter
@Getter
@ToString
public class Club extends BaseEntity {
    private String name;

    @Enumerated(EnumType.STRING)
    private Stadium stadium;

    private String country;
    private BigDecimal budget;

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Contract> contracts = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "club_match",
            joinColumns = @JoinColumn(name = "club_id"),
            inverseJoinColumns = @JoinColumn(name = "match_id")
    )
    private Set<Match> matches = new HashSet<>();
}
