package org.agency.course_work.service;

import lombok.AllArgsConstructor;
import org.agency.course_work.dto.*;
import org.agency.course_work.entity.Agent;
import org.agency.course_work.entity.Club;
import org.agency.course_work.entity.Contract;
import org.agency.course_work.entity.Player;
import org.agency.course_work.exception.AgentNotFound;
import org.agency.course_work.exception.ClubNotFound;
import org.agency.course_work.exception.ContractNotFound;
import org.agency.course_work.exception.PlayerNotFound;
import org.agency.course_work.mapper.ContractMapper;
import org.agency.course_work.repository.AgentRepository;
import org.agency.course_work.repository.ClubRepository;
import org.agency.course_work.repository.ContractRepository;
import org.agency.course_work.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ContractService {
    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;
    private final ClubRepository clubRepository;
    private final PlayerRepository playerRepository;
    private final AgentRepository agentRepository;

    public ContractDto getContractById(Long id) {
        Contract contract = contractRepository.findById(id).orElseThrow(() -> new ContractNotFound("Contract not found"));
        return contractMapper.toDto(contract);
    }

    @Transactional
    public ContractDto createContract(ContractCreationDto contractDto) {
        if (!agentRepository.existsById(contractDto.agentId())) {
            throw new AgentNotFound("Agent not found with ID: " + contractDto.agentId());
        }
        Agent agent = agentRepository.getReferenceById(contractDto.agentId());
        if (!clubRepository.existsById(contractDto.clubId())) {
            throw new ClubNotFound("Club not found with ID: " + contractDto.clubId());
        }
        Club club = clubRepository.getReferenceById(contractDto.clubId());
        if (!playerRepository.existsById(contractDto.playerId())) {
            throw new PlayerNotFound("Player not found with ID: " + contractDto.playerId());
        }
        Player player = playerRepository.getReferenceById(contractDto.playerId());
        Contract contract = contractMapper.toEntity(contractDto);
       contract.setAgent(agent);
       contract.setClub(club);
       contract.setPlayer(player);
       Contract savedContract = contractRepository.save(contract);
        return contractMapper.toDto(savedContract);
    }

    public List<ContractDto> getAllContracts() {
        return contractRepository.findAll().stream()
                .map(contractMapper::toDto)
                .toList();
    }

    @Transactional
    public ContractDto updateContract(Long id, ContractDto contractDto) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFound("Contract not found"));
        contractMapper.partialUpdate(contractDto, contract);
        return contractMapper.toDto(contractRepository.save(contract));
    }

    public List<ContractDto> getSortedContracts(String sortBy, String order) {
        List<Contract> contracts = contractRepository.findAll();
        List<Contract> sortedContracts;
        switch (sortBy) {
            case "startDate":
                sortedContracts = contracts.stream()
                        .sorted((c1, c2) -> order.equalsIgnoreCase("asc") ?
                                c1.getStartDate().compareTo(c2.getStartDate()) :
                                c2.getStartDate().compareTo(c1.getStartDate()))
                        .toList();
                break;
            case "endDate":
                sortedContracts = contracts.stream()
                        .sorted((c1, c2) -> order.equalsIgnoreCase("asc") ?
                                c1.getEndDate().compareTo(c2.getEndDate()) :
                                c2.getEndDate().compareTo(c1.getEndDate()))
                        .toList();
                break;
            case "salary":
                sortedContracts = contracts.stream()
                        .sorted((c1, c2) -> order.equalsIgnoreCase("asc") ?
                                c1.getSalary().compareTo(c2.getSalary()) :
                                c2.getSalary().compareTo(c1.getSalary()))
                        .toList();
                break;
            default:
                throw new IllegalArgumentException("Unsupported sorting parameter: " + sortBy);
        }
        return sortedContracts.stream()
                .map(contract -> new ContractDto(
                        contract.getId(), contract.getCreatedAt(), contract.getUpdatedAt(),
                        contract.getStartDate(), contract.getEndDate(), contract.getSalary()))
                .toList();
    }

    public List<ContractDto> getFilteredContracts(LocalDate startDate, LocalDate endDate, BigDecimal minSalary, BigDecimal maxSalary) {
        List<Contract> contracts = contractRepository.findAll();
        if (startDate != null) {
            contracts = contracts.stream()
                    .filter(contract -> contract.getStartDate() != null && !contract.getStartDate().isBefore(startDate))
                    .toList();
        }
        if (endDate != null) {
            contracts = contracts.stream()
                    .filter(contract -> contract.getEndDate() != null && !contract.getEndDate().isAfter(endDate))
                    .toList();
        }
        if (minSalary != null) {
            contracts = contracts.stream()
                    .filter(contract -> contract.getSalary() != null && contract.getSalary().compareTo(minSalary) >= 0)
                    .toList();
        }
        if (maxSalary != null) {
            contracts = contracts.stream()
                    .filter(contract -> contract.getSalary() != null && contract.getSalary().compareTo(maxSalary) <= 0)
                    .toList();
        }
        return contracts.stream()
                .map(contract -> new ContractDto(
                        contract.getId(), contract.getCreatedAt(), contract.getUpdatedAt(), contract.getStartDate(),
                        contract.getEndDate(), contract.getSalary()))
                .toList();
    }


}
