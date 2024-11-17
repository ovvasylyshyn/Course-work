package org.agency.course_work.repository;

import org.agency.course_work.entity.Match;
import org.agency.course_work.enums.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findAllByCity(City city);
}
