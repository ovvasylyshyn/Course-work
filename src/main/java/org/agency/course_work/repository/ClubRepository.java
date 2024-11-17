package org.agency.course_work.repository;

import org.agency.course_work.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long> {
    List<Club> findByMatches_Id(Long matchId);
}
