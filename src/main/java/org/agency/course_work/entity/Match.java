package org.agency.course_work.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.agency.course_work.enums.City;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "matches")
@Setter
@Getter
@ToString
@Where(clause = "is_deleted = false")
public class Match extends BaseEntity {
    private LocalDate date;

@Enumerated(EnumType.STRING)
    private City city;

    private String score;

    @ManyToMany(mappedBy = "matches", cascade = CascadeType.REMOVE)
    private Set<Club> clubs = new HashSet<>();
}
