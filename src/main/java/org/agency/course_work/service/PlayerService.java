package org.agency.course_work.service;

import lombok.AllArgsConstructor;
import org.agency.course_work.dto.PlayerCreationDto;
import org.agency.course_work.dto.PlayerDto;
import org.agency.course_work.dto.PlayerAgentDto;
import org.agency.course_work.entity.Agent;
import org.agency.course_work.entity.Club;
import org.agency.course_work.entity.Player;
import org.agency.course_work.exception.AgentNotFound;
import org.agency.course_work.exception.ClubNotFound;
import org.agency.course_work.exception.PlayerNotFound;
import org.agency.course_work.mapper.PlayerMapper;
import org.agency.course_work.repository.AgentRepository;
import org.agency.course_work.repository.ClubRepository;
import org.agency.course_work.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                player.getId(),
                player.getCreatedAt(),
                player.getUpdatedAt(),
                player.getName(),
                player.getSurname(),
                player.getAge(),
                player.getPosition(),
                player.getNationality(),
                player.getValue(),
                agent != null ? agent.getId() : null,
                agent != null ? agent.getCreatedAt() : null,
                agent != null ? agent.getUpdatedAt() : null,
                agent != null ? agent.getFirstName() : null,
                agent != null ? agent.getLastName() : null,
                agent != null ? agent.getPhoneNumber() : null,
                agent != null ? agent.getCommissionRate() : null
        );
    }
    public List<PlayerDto> getPlayersByAgent(Long agentId) {
        if (!agentRepository.existsById(agentId)) {
            throw new AgentNotFound("Agent not found with ID: " + agentId);
        }
        List<Player> players = playerRepository.findAllByAgentId(agentId);
        return players.stream()
                .map(playerMapper::toDto)
                .toList();
    }

}
