package org.agency.course_work.service;

import org.agency.course_work.dto.AgentCreationDto;
import org.agency.course_work.dto.AgentDto;
import org.agency.course_work.entity.Agent;
import org.agency.course_work.enums.CommissionRate;
import org.agency.course_work.exception.AgentNotFound;
import org.agency.course_work.mapper.AgentMapper;
import org.agency.course_work.repository.AgentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@Transactional
public class AgentService {
    private final AgentRepository agentRepository;
    private final AgentMapper agentMapper;
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

    public AgentService(AgentRepository agentRepository, AgentMapper agentMapper) {
        this.agentRepository = agentRepository;
        this.agentMapper = agentMapper;
    }

    @Transactional(readOnly = true)
    public AgentDto getAgentById(Long id) {
        logger.info("Fetching agent with ID: {}", id);
        try {
            Agent agent = agentRepository.findById(id).orElseThrow(() -> {
                        logger.warn("Agent with ID: {} not found", id);
                        return new AgentNotFound("Agent not found");
                    });
            logger.info("Agent fetched successfully: {}", agent);
            return agentMapper.toDto(agent);
        } catch (Exception e) {
            logger.error("Error fetching agent with ID: {}", id, e);
            throw e;
        }
    }

    public AgentDto createAgent(AgentCreationDto agent) {
        logger.info("Creating new agent: {}", agent);
        try {
            Agent savedAgent = agentRepository.save(agentMapper.toEntity(agent));
            logger.info("Agent created successfully with ID: {}", savedAgent.getId());
            return agentMapper.toDto(savedAgent);
        } catch (Exception e) {
            logger.error("Error creating agent: {}", agent, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<AgentDto> getAllAgents(Pageable pageable) {
        logger.info("Fetching all agents with pagination: {}", pageable);
        try {
            Page<AgentDto> agents = agentRepository.findAll(pageable).map(agentMapper::toDto);
            logger.info("Fetched {} agents successfully", agents.getTotalElements());
            return agents;
        } catch (Exception e) {
            logger.error("Error fetching all agents", e);
            throw e;
        }
    }

    public AgentDto updateAgent(Long id, AgentDto agentDto) {
        logger.info("Updating agent with ID: {}", id);
        try {
            Agent agent = agentRepository.findById(id).orElseThrow(() -> {
                        logger.warn("Agent with ID: {} not found for update", id);
                        return new AgentNotFound("Agent not found");
                    });
            agentMapper.partialUpdate(agentDto, agent);
            Agent updatedAgent = agentRepository.save(agent);
            logger.info("Agent with ID: {} updated successfully", updatedAgent.getId());
            return agentMapper.toDto(updatedAgent);
        } catch (Exception e) {
            logger.error("Error updating agent with ID: {}", id, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<AgentDto> getSortedAgents(String sortBy, String order, Pageable pageable) {
        logger.info("Fetching sorted agents: sortBy={}, order={}", sortBy, order);
        try {
            if (sortBy == null || sortBy.isEmpty()) {
                logger.warn("SortBy parameter is null or empty. Default sorting will be applied.");
                sortBy = "id";
            }
            Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            Page<Agent> agentsPage = agentRepository.findAll(sortedPageable);
            logger.info("Fetched sorted agents successfully. Total found: {}", agentsPage.getTotalElements());
            return agentsPage.map(agentMapper::toDto);
        } catch (Exception e) {
            logger.error("Error fetching sorted agents: sortBy={}, order={}", sortBy, order, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<AgentDto> getFilteredAgents(String firstName, String lastName, String phoneNumber, CommissionRate commissionRate, Boolean isDeleted, Pageable pageable) {
        logger.info("Fetching filtered agents: firstName={}, lastName={}, phoneNumber={}, commissionRate={}",
                firstName, lastName, phoneNumber, commissionRate);
        try {
            Specification<Agent> specification = Specification.where(null);

            if (firstName != null && !firstName.isEmpty()) {
                specification = specification.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
                logger.debug("Added filter for firstName: {}", firstName);
            }
            if (lastName != null && !lastName.isEmpty()) {
                specification = specification.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
                logger.debug("Added filter for lastName: {}", lastName);
            }
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                specification = specification.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.like(root.get("phoneNumber"), "%" + phoneNumber + "%"));
                logger.debug("Added filter for phoneNumber: {}", phoneNumber);
            }
            if (commissionRate != null) {
                specification = specification.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("commissionRate"), commissionRate));
                logger.debug("Added filter for commissionRate: {}", commissionRate);
            }

            Page<Agent> agents = agentRepository.findAll(specification, pageable);
            logger.info("Filtered agents fetched successfully. Total found: {}", agents.getTotalElements());
            return agents.map(agentMapper::toDto);
        } catch (Exception e) {
            logger.error("Error fetching filtered agents: firstName={}, lastName={}, phoneNumber={}, commissionRate={}",
                    firstName, lastName, phoneNumber, commissionRate, e);
            throw e;
        }
    }

    @Transactional
    public void deleteAgentById(Long id) {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Attempting to mark Agent with ID: {} as deleted", id);
        Agent agent = agentRepository.findById(id).orElseThrow(() -> new AgentNotFound("Agent with ID " + id + " not found."));
        agent.setDeleted(true);
        agentRepository.save(agent);

        logger.info("Agent with ID: {} marked as deleted successfully", id);
    }

}
