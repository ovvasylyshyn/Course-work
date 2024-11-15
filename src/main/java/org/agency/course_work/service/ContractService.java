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
}
