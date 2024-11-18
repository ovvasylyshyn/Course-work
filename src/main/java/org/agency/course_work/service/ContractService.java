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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    public Page<ContractDto> getAllContracts(Pageable pageable) {
        return contractRepository.findAll(pageable).map(contractMapper::toDto);
    }

    @Transactional
    public ContractDto updateContract(Long id, ContractDto contractDto) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFound("Contract not found"));
        contractMapper.partialUpdate(contractDto, contract);
        return contractMapper.toDto(contractRepository.save(contract));
    }

    public Page<ContractDto> getSortedContracts(String sortBy, String order, Pageable pageable) {
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Contract> contractsPage = contractRepository.findAll(sortedPageable);
        return contractsPage.map(contract -> new ContractDto(contract.getId(), contract.getCreatedAt(), contract.getUpdatedAt(), contract.getStartDate(),
                contract.getEndDate(), contract.getSalary()
        ));
    }

    public Page<ContractDto> getFilteredContracts(LocalDate startDate, LocalDate endDate, BigDecimal minSalary, BigDecimal maxSalary, Pageable pageable) {
        Specification<Contract> specification = Specification.where(null);
        if (startDate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDate));
        }
        if (endDate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDate));
        }
        if (minSalary != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), minSalary));
        }
        if (maxSalary != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("salary"), maxSalary));
        }
        Page<Contract> contractsPage = contractRepository.findAll(specification, pageable);
        return contractsPage.map(contract -> new ContractDto(contract.getId(), contract.getCreatedAt(),
                contract.getUpdatedAt(), contract.getStartDate(), contract.getEndDate(), contract.getSalary()));
    }



}
