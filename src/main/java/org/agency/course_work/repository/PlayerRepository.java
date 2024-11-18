package org.agency.course_work.repository;

import org.agency.course_work.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player> {
    Page<Player> findAllByAgentId(Long agentId, Pageable pageable);
}
