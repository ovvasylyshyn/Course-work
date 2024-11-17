package org.agency.course_work.service;

import org.agency.course_work.dto.AgentCreationDto;
import org.agency.course_work.dto.AgentDto;
import org.agency.course_work.entity.Agent;
import org.agency.course_work.enums.CommissionRate;
import org.agency.course_work.exception.AgentNotFound;
import org.agency.course_work.mapper.AgentMapper;
import org.agency.course_work.repository.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)
    public List<AgentDto> getAllAgents() {
    return agentRepository.findAll().stream()
        .map(agentMapper::toDto)
        .toList();
    }

    public AgentDto updateAgent(Long id, AgentDto agentDto) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new AgentNotFound("Agent not found"));
        agentMapper.partialUpdate(agentDto, agent);
        return agentMapper.toDto(agentRepository.save(agent));
    }

    @Transactional(readOnly = true)
    public List<AgentDto> getSortedAgents(String sortBy, String order) {
        List<Agent> agents = agentRepository.findAll();
        List<Agent> sortedAgents;
        switch (sortBy) {
            case "firstName":
                sortedAgents = agents.stream()
                        .sorted((a1, a2) -> order.equals("asc") ?
                                a1.getFirstName().compareToIgnoreCase(a2.getFirstName()) :
                                a2.getFirstName().compareToIgnoreCase(a1.getFirstName()))
                        .toList();
                break;
            case "lastName":
                sortedAgents = agents.stream()
                        .sorted((a1, a2) -> order.equals("asc") ?
                                a1.getLastName().compareToIgnoreCase(a2.getLastName()) :
                                a2.getLastName().compareToIgnoreCase(a1.getLastName()))
                        .toList();
                break;
            case "phoneNumber":
                sortedAgents = agents.stream()
                        .sorted((a1, a2) -> order.equals("asc") ?
                                a1.getPhoneNumber().compareTo(a2.getPhoneNumber()) :
                                a2.getPhoneNumber().compareTo(a1.getPhoneNumber()))
                        .toList();
                break;
            case "commissionRate":
                sortedAgents = agents.stream()
                        .sorted((a1, a2) -> order.equals("asc") ?
                                a1.getCommissionRate().compareTo(a2.getCommissionRate()) :
                                a2.getCommissionRate().compareTo(a1.getCommissionRate()))
                        .toList();
                break;
            default:
                throw new IllegalArgumentException("Unsupported sorting parameter: " + sortBy);
        }
        return sortedAgents.stream()
                .map(agent -> new AgentDto(
                        agent.getId(), agent.getCreatedAt(), agent.getUpdatedAt(), agent.getFirstName(),
                        agent.getLastName(), agent.getPhoneNumber(), agent.getCommissionRate()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AgentDto> getFilteredAgents(String firstName, String lastName, String phoneNumber, CommissionRate commissionRate) {
        List<Agent> agents = agentRepository.findAll();
        if (firstName != null && !firstName.isEmpty()) {
            agents = agents.stream()
                    .filter(agent -> agent.getFirstName().equalsIgnoreCase(firstName))
                    .toList();
        }
        if (lastName != null && !lastName.isEmpty()) {
            agents = agents.stream()
                    .filter(agent -> agent.getLastName().equalsIgnoreCase(lastName))
                    .toList();
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            agents = agents.stream()
                    .filter(agent -> agent.getPhoneNumber().equals(phoneNumber))
                    .toList();
        }
        if (commissionRate != null) {
            agents = agents.stream()
                    .filter(agent -> agent.getCommissionRate().equals(commissionRate))
                    .toList();
        }
        return agents.stream()
                .map(agent -> new AgentDto(
                        agent.getId(), agent.getCreatedAt(), agent.getUpdatedAt(), agent.getFirstName(), agent.getLastName(),
                        agent.getPhoneNumber(), agent.getCommissionRate()))
                .toList();
    }
}
