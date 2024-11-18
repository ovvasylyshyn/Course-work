package org.agency.course_work.service;

import lombok.AllArgsConstructor;
import org.agency.course_work.dto.PlayerCreationDto;
import org.agency.course_work.dto.PlayerDetailsDto;
import org.agency.course_work.dto.PlayerDto;
import org.agency.course_work.dto.PlayerAgentDto;
import org.agency.course_work.entity.Agent;
import org.agency.course_work.entity.Club;
import org.agency.course_work.entity.Contract;
import org.agency.course_work.entity.Player;
import org.agency.course_work.enums.PlayerPosition;
import org.agency.course_work.exception.AgentNotFound;
import org.agency.course_work.exception.ClubNotFound;
import org.agency.course_work.exception.ContractNotFound;
import org.agency.course_work.exception.PlayerNotFound;
import org.agency.course_work.mapper.PlayerMapper;
import org.agency.course_work.repository.AgentRepository;
import org.agency.course_work.repository.ClubRepository;
import org.agency.course_work.repository.PlayerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;


import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final AgentRepository agentRepository;
    private final ClubRepository clubRepository;

    @Transactional(readOnly = true)
    public PlayerDto getPlayerById(Long id) {
        Player player = playerRepository.findById(id).orElseThrow(()->new PlayerNotFound("Player not found"));
        return playerMapper.toDto(player);
    }

    @Transactional(readOnly = true)
    public Page<PlayerDto> getAllPlayers(Pageable pageable) {
        return playerRepository.findAll(pageable).map(playerMapper::toDto);
    }


    public PlayerDto createPlayer(PlayerCreationDto playerDto) {
        if (!agentRepository.existsById(playerDto.agentId())) {
            throw new AgentNotFound("Agent not found with ID: " + playerDto.agentId());
        }
        Agent agent = agentRepository.getReferenceById(playerDto.agentId());
        if (!clubRepository.existsById(playerDto.clubId())) {
            throw new ClubNotFound("Club not found with ID: " + playerDto.clubId());
        }
        Club club = clubRepository.getReferenceById(playerDto.clubId());
        Player player = playerMapper.toEntity(playerDto);
        player.setAgent(agent);
        player.setClub(club);
        Player savedPlayer = playerRepository.save(player);
        return playerMapper.toDto(savedPlayer);
    }

    @Transactional(readOnly = true)
    public PlayerAgentDto getPlayerWithAgent(Long playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new PlayerNotFound("Player not found"));
        Agent agent = player.getAgent();
        return new PlayerAgentDto(
                player.getId(), player.getCreatedAt(), player.getUpdatedAt(), player.getName()+" "+ player.getSurname(),
                player.getAge(), player.getPosition(), player.getNationality(), player.getValue(),
                agent != null ? agent.getId() : null, agent != null ? agent.getCreatedAt() : null,
                agent != null ? agent.getUpdatedAt() : null, agent != null ? agent.getFirstName() : null,
                agent != null ? agent.getLastName() : null, agent != null ? agent.getPhoneNumber() : null,
                agent != null ? agent.getCommissionRate() : null
        );
    }

@Transactional(readOnly = true)
public Page<PlayerDto> getPlayersByAgent(Long agentId, Pageable pageable) {
    if (!agentRepository.existsById(agentId)) {
        throw new AgentNotFound("Agent not found with ID: " + agentId);
    }
    Page<Player> players = playerRepository.findAllByAgentId(agentId, pageable);
    return players.map(playerMapper::toDto);
}

    public PlayerDto updatePlayer(Long id, PlayerDto playerDto) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFound("Player not found"));
        playerMapper.partialUpdate(playerDto, player);
        return playerMapper.toDto(playerRepository.save(player));
    }

    @Transactional(readOnly = true)
    public PlayerDetailsDto getPlayerDetails(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFound("Player with ID " + playerId + " not found."));
        Agent agent = player.getAgent();
        Club club = player.getClub();
        Contract contract = player.getContracts().stream()
                .findFirst()
                .orElseThrow(() -> new ContractNotFound("No contract found for player with ID " + playerId));
        return new PlayerDetailsDto(
                player.getId(), player.getName()+" "+player.getSurname(), player.getAge(), player.getNationality(),
                player.getPosition(), player.getValue(), agent.getFirstName() + " " + agent.getLastName(),
                agent.getPhoneNumber(), club.getName(), contract.getStartDate(), contract.getEndDate(), contract.getSalary()
        );
    }

    @Transactional(readOnly = true)
    public Page<PlayerDto> getSortedPlayers(String sortBy, String order, Pageable pageable) {
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(direction, sortBy));
        Page<Player> players = playerRepository.findAll(sortedPageable);
        return players.map(player -> new PlayerDto(
                player.getId(), player.getCreatedAt(), player.getUpdatedAt(), player.getName(), player.getSurname(),
                player.getAge(), player.getPosition(), player.getNationality(), player.getValue()
        ));
    }

@Transactional(readOnly = true)
public Page<PlayerDto> getFilteredPlayers(Integer age, String name, String surname, String nationality, BigDecimal minValue, BigDecimal maxValue, PlayerPosition position, Pageable pageable) {
    Specification<Player> specification = Specification.where(null);
    if (age != null) {
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("age"), age));
    }
    if (name != null && !name.isEmpty()) {
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
    }
    if (surname != null && !surname.isEmpty()) {
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("surname")), "%" + surname.toLowerCase() + "%"));
    }
    if (nationality != null && !nationality.isEmpty()) {
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("nationality")), nationality.toLowerCase()));
    }
    if (minValue != null) {
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("value"), minValue));
    }
    if (maxValue != null) {
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("value"), maxValue));
    }
    if (position != null) {
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("position"), position));
    }
    Page<Player> players = playerRepository.findAll(specification, pageable);
    return players.map(player -> new PlayerDto(player.getId(), player.getCreatedAt(), player.getUpdatedAt(), player.getName(),
            player.getSurname(), player.getAge(), player.getPosition(), player.getNationality(), player.getValue()
    ));
}

}
