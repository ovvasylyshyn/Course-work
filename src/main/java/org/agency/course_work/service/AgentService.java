package org.agency.course_work.service;

import org.agency.course_work.dto.AgentCreationDto;
import org.agency.course_work.dto.AgentDto;
import org.agency.course_work.entity.Agent;
import org.agency.course_work.enums.CommissionRate;
import org.agency.course_work.exception.AgentNotFound;
import org.agency.course_work.mapper.AgentMapper;
import org.agency.course_work.repository.AgentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new AgentNotFound("Agent not found"));
        return agentMapper.toDto(agent);
    }

    public AgentDto createAgent(AgentCreationDto agent) {
        return agentMapper.toDto(agentRepository.save(agentMapper.toEntity(agent)));
    }

    @Transactional(readOnly = true)
    public Page<AgentDto> getAllAgents(Pageable pageable) {
        return agentRepository.findAll(pageable).map(agentMapper::toDto);
    }

    public AgentDto updateAgent(Long id, AgentDto agentDto) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new AgentNotFound("Agent not found"));
        agentMapper.partialUpdate(agentDto, agent);
        return agentMapper.toDto(agentRepository.save(agent));
    }

    @Transactional(readOnly = true)
    public Page<AgentDto> getSortedAgents(String sortBy, String order, Pageable pageable) {
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Agent> agentsPage = agentRepository.findAll(sortedPageable);
        return agentsPage.map(agentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<AgentDto> getFilteredAgents(String firstName, String lastName, String phoneNumber, CommissionRate commissionRate, Pageable pageable) {
        Specification<Agent> specification = Specification.where(null);
        if (firstName != null && !firstName.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
        }
        if (lastName != null && !lastName.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("phoneNumber"), "%" + phoneNumber + "%"));
        }
        if (commissionRate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("commissionRate"), commissionRate));
        }
        Page<Agent> agents = agentRepository.findAll(specification, pageable);
        return agents.map(agent -> new AgentDto(agent.getId(), agent.getCreatedAt(), agent.getUpdatedAt(), agent.getFirstName(), agent.getLastName(), agent.getPhoneNumber(), agent.getCommissionRate()
        ));
    }


}
