package org.agency.course_work.repository;

import org.agency.course_work.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AgentRepository extends JpaRepository<Agent, Long>, JpaSpecificationExecutor<Agent> {
}
