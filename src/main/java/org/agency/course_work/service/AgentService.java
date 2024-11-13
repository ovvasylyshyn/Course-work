package org.agency.course_work.service;

import org.agency.course_work.dto.AgentCreationDto;
import org.agency.course_work.dto.AgentDto;
import org.agency.course_work.entity.Agent;
import org.agency.course_work.exception.AgentNotFound;
import org.agency.course_work.mapper.AgentMapper;
import org.agency.course_work.repository.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AgentService {
    private final AgentRepository agentRepository;
    private final AgentMapper agentMapper;

    public AgentService(AgentRepository agentRepository, AgentMapper agentMapper) {
        this.agentRepository = agentRepository;
        this.agentMapper = agentMapper;
    }

    @Transactional(readOnly = true)
    public AgentDto getAgentById(Long id) {
        Agent agent = agentRepository.findById(id).orElseThrow(() -> new AgentNotFound("Agent not found"));
        return agentMapper.toDto(agent);
    }

    public AgentDto createAgent(AgentCreationDto agent) {
        return agentMapper.toDto(agentRepository.save(agentMapper.toEntity(agent)));
    }


}
