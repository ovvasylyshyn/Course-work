package org.agency.course_work.service;

import lombok.AllArgsConstructor;
import org.agency.course_work.dto.PlayerCreationDto;
import org.agency.course_work.dto.PlayerDetailsDto;
import org.agency.course_work.dto.PlayerDto;
import org.agency.course_work.dto.PlayerAgentDto;
import org.agency.course_work.entity.*;
import org.agency.course_work.enums.PlayerPosition;
import org.agency.course_work.exception.*;
import org.agency.course_work.mapper.PlayerMapper;
import org.agency.course_work.repository.AgentRepository;
import org.agency.course_work.repository.ClubRepository;
import org.agency.course_work.repository.PlayerRepository;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
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
    private static final Logger logger = LoggerFactory.getLogger(ClubService.class);

    @Transactional(readOnly = true)
    public PlayerDto getPlayerById(Long id) {
        logger.info("Fetching player with ID: {}", id);

        try {
            Player player = playerRepository.findById(id)
                    .orElseThrow(() -> new PlayerNotFound("Player not found"));
            logger.info("Successfully fetched player with ID: {}", id);
            return playerMapper.toDto(player);
        } catch (Exception e) {
            logger.error("Error fetching player with ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<PlayerDto> getAllPlayers(Pageable pageable) {
        logger.info("Fetching all players with pagination: {}", pageable);

        try {
            Page<PlayerDto> playersPage = playerRepository.findAll(pageable).map(playerMapper::toDto);
            logger.info("Successfully fetched {} players", playersPage.getTotalElements());
            return playersPage;
        } catch (Exception e) {
            logger.error("Error fetching all players. Error: {}", e.getMessage());
            throw e;
        }
    }

    public PlayerDto createPlayer(PlayerCreationDto playerDto) {
        logger.info("Creating new player with agent ID: {} and club ID: {}", playerDto.agentId(), playerDto.clubId());

        try {
            // Check if agent exists
            if (!agentRepository.existsById(playerDto.agentId())) {
                logger.warn("Agent with ID: {} not found", playerDto.agentId());
                throw new AgentNotFound("Agent not found with ID: " + playerDto.agentId());
            }
            Agent agent = agentRepository.getReferenceById(playerDto.agentId());

            // Check if club exists
            if (!clubRepository.existsById(playerDto.clubId())) {
                logger.warn("Club with ID: {} not found", playerDto.clubId());
                throw new ClubNotFound("Club not found with ID: " + playerDto.clubId());
            }
            Club club = clubRepository.getReferenceById(playerDto.clubId());

            // Create and save player
            Player player = playerMapper.toEntity(playerDto);
            player.setAgent(agent);
            player.setClub(club);
            Player savedPlayer = playerRepository.save(player);
            logger.info("Successfully created player with ID: {}", savedPlayer.getId());

            return playerMapper.toDto(savedPlayer);
        } catch (Exception e) {
            logger.error("Error creating player with agent ID: {} and club ID: {}. Error: {}",
                    playerDto.agentId(), playerDto.clubId(), e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public PlayerAgentDto getPlayerWithAgent(Long playerId) {
        logger.info("Fetching player with ID: {} and associated agent", playerId);

        try {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFound("Player not found"));
            Agent agent = player.getAgent();
            logger.info("Successfully fetched player with ID: {} and agent info", playerId);

            return new PlayerAgentDto(
                    player.getId(), player.getCreatedAt(), player.getUpdatedAt(), player.getName() + " " + player.getSurname(),
                    player.getAge(), player.getPosition(), player.getNationality(), player.getValue(),
                    agent != null ? agent.getId() : null, agent != null ? agent.getCreatedAt() : null,
                    agent != null ? agent.getUpdatedAt() : null, agent != null ? agent.getFirstName() : null,
                    agent != null ? agent.getLastName() : null, agent != null ? agent.getPhoneNumber() : null,
                    agent != null ? agent.getCommissionRate() : null
            );
        } catch (Exception e) {
            logger.error("Error fetching player with ID: {}. Error: {}", playerId, e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<PlayerDto> getPlayersByAgent(Long agentId, Pageable pageable) {
        logger.info("Fetching players associated with agent ID: {} with pagination: {}", agentId, pageable);

        try {
            if (!agentRepository.existsById(agentId)) {
                logger.warn("Agent with ID: {} not found", agentId);
                throw new AgentNotFound("Agent not found with ID: " + agentId);
            }
            Page<Player> players = playerRepository.findAllByAgentId(agentId, pageable);
            logger.info("Successfully fetched {} players for agent with ID: {}", players.getTotalElements(), agentId);
            return players.map(playerMapper::toDto);
        } catch (Exception e) {
            logger.error("Error fetching players for agent with ID: {}. Error: {}", agentId, e.getMessage());
            throw e;
        }
    }

    public PlayerDto updatePlayer(Long id, PlayerDto playerDto) {
        logger.info("Updating player with ID: {}", id);

        try {
            Player player = playerRepository.findById(id)
                    .orElseThrow(() -> new PlayerNotFound("Player not found"));
            playerMapper.partialUpdate(playerDto, player);
            Player updatedPlayer = playerRepository.save(player);
            logger.info("Successfully updated player with ID: {}", id);
            return playerMapper.toDto(updatedPlayer);
        } catch (Exception e) {
            logger.error("Error updating player with ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public PlayerDetailsDto getPlayerDetails(Long playerId) {
        logger.info("Fetching detailed information for player with ID: {}", playerId);

        try {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFound("Player with ID " + playerId + " not found."));
            Agent agent = player.getAgent();
            Club club = player.getClub();
            Contract contract = player.getContracts().stream()
                    .findFirst()
                    .orElseThrow(() -> new ContractNotFound("No contract found for player with ID " + playerId));

            logger.info("Successfully fetched details for player with ID: {}", playerId);
            return new PlayerDetailsDto(
                    player.getId(), player.getName() + " " + player.getSurname(), player.getAge(), player.getNationality(),
                    player.getPosition(), player.getValue(), agent.getFirstName() + " " + agent.getLastName(),
                    agent.getPhoneNumber(), club.getName(), contract.getStartDate(), contract.getEndDate(), contract.getSalary()
            );
        } catch (Exception e) {
            logger.error("Error fetching details for player with ID: {}. Error: {}", playerId, e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<PlayerDto> getSortedPlayers(String sortBy, String order, Pageable pageable) {
        logger.info("Fetching sorted players by: {} in {} order", sortBy, order);

        try {
            Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(direction, sortBy));
            Page<Player> players = playerRepository.findAll(sortedPageable);

            logger.info("Successfully fetched sorted players by: {} in {} order", sortBy, order);
            return players.map(player -> new PlayerDto(
                    player.getId(), player.getCreatedAt(), player.getUpdatedAt(), player.getName(), player.getSurname(),
                    player.getAge(), player.getPosition(), player.getNationality(), player.getValue()
            ));
        } catch (Exception e) {
            logger.error("Error fetching sorted players by: {} in {} order. Error: {}", sortBy, order, e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<PlayerDto> getFilteredPlayers(Integer age, String name, String surname, String nationality, BigDecimal minValue, BigDecimal maxValue, PlayerPosition position, Pageable pageable) {
        logger.info("Fetching filtered players with filters - Age: {}, Name: {}, Surname: {}, Nationality: {}, Min Value: {}, Max Value: {}, Position: {}",
                age, name, surname, nationality, minValue, maxValue, position);

        try {
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

            logger.info("Successfully fetched filtered players with the provided filters.");
            return players.map(player -> new PlayerDto(player.getId(), player.getCreatedAt(), player.getUpdatedAt(), player.getName(),
                    player.getSurname(), player.getAge(), player.getPosition(), player.getNationality(), player.getValue()
            ));
        } catch (Exception e) {
            logger.error("Error fetching filtered players with filters - Age: {}, Name: {}, Surname: {}, Nationality: {}, Min Value: {}, Max Value: {}, Position: {}. Error: {}",
                    age, name, surname, nationality, minValue, maxValue, position, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void deletePlayerById(Long id) {
        logger.info("Attempting to mark Player with ID: {} as deleted", id);
        try {
            Player player = playerRepository.findById(id).orElseThrow(() -> {
                logger.warn("Player with ID: {} not found", id);
                return new PlayerNotFound("Player with ID " + id + " not found.");
            });
            player.setDeleted(true);
            playerRepository.save(player);
            logger.info("Player with ID: {} marked as deleted successfully", id);
        } catch (Exception e) {
            logger.error("Error occurred while marking Player with ID: {} as deleted", id, e);
            throw e;
        }
    }

}
