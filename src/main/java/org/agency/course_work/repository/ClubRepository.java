package org.agency.course_work.repository;

import org.agency.course_work.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long>, JpaSpecificationExecutor<Club> {
    List<Club> findByMatches_Id(Long matchId);
}
