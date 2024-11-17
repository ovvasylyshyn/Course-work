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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<PlayerDto> getAllPlayers() {
        return playerRepository.findAll().stream()
                .map(playerMapper::toDto)
                .toList();
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
                player.getId(), player.getCreatedAt(), player.getUpdatedAt(), player.getName(), player.getSurname(),
                player.getAge(), player.getPosition(), player.getNationality(), player.getValue(),
                agent != null ? agent.getId() : null, agent != null ? agent.getCreatedAt() : null,
                agent != null ? agent.getUpdatedAt() : null, agent != null ? agent.getFirstName() : null,
                agent != null ? agent.getLastName() : null, agent != null ? agent.getPhoneNumber() : null,
                agent != null ? agent.getCommissionRate() : null
        );
    }

    @Transactional(readOnly = true)
    public List<PlayerDto> getPlayersByAgent(Long agentId) {
        if (!agentRepository.existsById(agentId)) {
            throw new AgentNotFound("Agent not found with ID: " + agentId);
        }
        List<Player> players = playerRepository.findAllByAgentId(agentId);
        return players.stream()
                .map(playerMapper::toDto)
                .toList();
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
    public List<PlayerDto> getSortedPlayers(String sortBy, String order) {
        List<Player> players = playerRepository.findAll();

        List<Player> sortedPlayers;
        switch (sortBy) {
            case "age":
                sortedPlayers = players.stream()
                        .sorted((p1, p2) -> order.equals("asc") ? Integer.compare(p1.getAge(), p2.getAge()) : Integer.compare(p2.getAge(), p1.getAge()))
                        .toList();
                break;
            case "name":
                sortedPlayers = players.stream()
                        .sorted((p1, p2) -> order.equals("asc") ? p1.getName().compareTo(p2.getName()) : p2.getName().compareTo(p1.getName()))
                        .toList();
                break;
            case "value":
                sortedPlayers = players.stream()
                        .sorted((p1, p2) -> order.equals("asc") ? p1.getValue().compareTo(p2.getValue()) : p2.getValue().compareTo(p1.getValue()))
                        .toList();
                break;
            case "surname":
                sortedPlayers = players.stream()
                        .sorted((p1, p2) -> order.equals("asc") ? p1.getSurname().compareTo(p2.getSurname()) : p2.getSurname().compareTo(p1.getSurname()))
                        .toList();
                break;
            case "nationality":
                sortedPlayers = players.stream()
                        .sorted((p1, p2) -> order.equals("asc") ? p1.getNationality().compareTo(p2.getNationality()) : p2.getNationality().compareTo(p1.getNationality()))
                        .toList();
                break;
            default:
                throw new IllegalArgumentException("Unsupported sorting parameter: " + sortBy);
        }

        return sortedPlayers.stream()
                .map(player -> new PlayerDto(
                        player.getId(), player.getCreatedAt(), player.getUpdatedAt(), player.getName(), player.getSurname(),
                        player.getAge(), player.getPosition(), player.getNationality(), player.getValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlayerDto> getFilteredPlayers(Integer age, String name, String surname, String nationality, BigDecimal minValue, BigDecimal maxValue, PlayerPosition position) {
        List<Player> players = playerRepository.findAll();

        if (age != null) {
            players = players.stream()
                    .filter(player -> player.getAge() == age)
                    .toList();
        }
        if (name != null && !name.isEmpty()) {
            players = players.stream()
                    .filter(player -> player.getName().equalsIgnoreCase(name))
                    .toList();
        }
        if (surname != null && !surname.isEmpty()) {
            players = players.stream()
                    .filter(player -> player.getSurname().equalsIgnoreCase(surname))
                    .toList();
        }
        if (nationality != null && !nationality.isEmpty()) {
            players = players.stream()
                    .filter(player -> player.getNationality().equalsIgnoreCase(nationality))
                    .toList();
        }
        if (minValue != null) {
            players = players.stream()
                    .filter(player -> player.getValue() != null && player.getValue().compareTo(minValue) >= 0)
                    .toList();
        }
        if (maxValue != null) {
            players = players.stream()
                    .filter(player -> player.getValue().compareTo(maxValue) <= 0)
                    .toList();
        }
        if (position != null) {
            players=players.stream()
                    .filter(player -> player.getPosition().equals(position))
                    .toList();
        }
        return players.stream()
                .map(player -> new PlayerDto(
                        player.getId(), player.getCreatedAt(), player.getUpdatedAt(), player.getName(), player.getSurname(),
                        player.getAge(), player.getPosition(), player.getNationality(), player.getValue()))
                .toList();
    }

}
