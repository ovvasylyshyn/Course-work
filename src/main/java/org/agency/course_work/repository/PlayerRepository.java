package org.agency.course_work.repository;

import org.agency.course_work.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findAllByAgentId(Long agentId);
}
