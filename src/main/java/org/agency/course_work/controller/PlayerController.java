package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.PlayerAgentDto;
import org.agency.course_work.dto.PlayerCreationDto;
import org.agency.course_work.dto.PlayerDetailsDto;
import org.agency.course_work.dto.PlayerDto;
import org.agency.course_work.enums.PlayerPosition;
import org.agency.course_work.exception.AgentNotFound;
import org.agency.course_work.exception.PlayerNotFound;
import org.agency.course_work.service.AgentService;
import org.agency.course_work.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("api/players")
@AllArgsConstructor
public class PlayerController {
    private PlayerService playerService;
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

    @GetMapping("{id}")
    @Cacheable(value = "players", key = "#id")
    public ResponseEntity<PlayerDto> getPlayerById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

    @PostMapping("/create")
    @CacheEvict(value = {"agents", "clubs", "players"})
    public ResponseEntity<PlayerDto> createPlayer(@Valid @RequestBody PlayerCreationDto playerDto) {
        PlayerDto createdPlayer = playerService.createPlayer(playerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlayer);
    }

    @GetMapping
    @Cacheable(value = "players")
    public Page<PlayerDto> getAllPlayers(@PageableDefault Pageable pageable) {
        return playerService.getAllPlayers(pageable);
    }


    @GetMapping("/{id}/with-agent")
    @Cacheable(value = "players", key = "#id")
    public ResponseEntity<PlayerAgentDto> getPlayerWithAgent(@PathVariable Long id) {
        PlayerAgentDto playerAgentDto = playerService.getPlayerWithAgent(id);
        return ResponseEntity.ok(playerAgentDto);
    }

    @GetMapping("/agents/{agentId}/players")
    @Cacheable(value = "players")
    public Page<PlayerDto> getPlayersByAgent(@PathVariable Long agentId, @PageableDefault Pageable pageable) {
        return playerService.getPlayersByAgent(agentId, pageable);
    }


    @PutMapping("/{id}")
    @CacheEvict(value = {"agents", "clubs", "players"})
    public ResponseEntity<PlayerDto> updatePlayer(@PathVariable Long id, @RequestBody @Valid PlayerDto playerDto) {
        PlayerDto updatedPlayer = playerService.updatePlayer(id, playerDto);
        return ResponseEntity.ok(updatedPlayer);
    }

    @GetMapping("/{id}/details")
    @Cacheable(value = "players", key = "#id")
    public ResponseEntity<PlayerDetailsDto> getPlayerDetails(@PathVariable Long id) {
        PlayerDetailsDto playerDetails = playerService.getPlayerDetails(id);
        return ResponseEntity.ok(playerDetails);
    }

    @GetMapping("/sorted")
    public Page<PlayerDto> getSortedPlayers(@RequestParam String sortBy, @RequestParam(defaultValue = "asc") String order, @PageableDefault Pageable pageable) {
        return playerService.getSortedPlayers(sortBy, order, pageable);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredPlayers(@RequestParam(required = false) Integer age, @RequestParam(required = false) String name, @RequestParam(required = false) String surname,
                                                @RequestParam(required = false) String nationality, @RequestParam(required = false) BigDecimal minValue, @RequestParam(required = false) BigDecimal maxValue, @RequestParam(required = false) PlayerPosition position, @PageableDefault Pageable pageable
    ) {
        Page<PlayerDto> filteredPlayers = playerService.getFilteredPlayers(age, name, surname, nationality, minValue, maxValue, position, pageable);
        if (filteredPlayers.isEmpty()) {
            return new ResponseEntity<>("No players found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredPlayers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePlayer(@PathVariable Long id) {
        logger.info("Received request to mark Player with ID: {} as deleted", id);
        try {
            playerService.deletePlayerById(id);
            logger.info("Player with ID: {} marked as deleted successfully", id);
            return ResponseEntity.ok("Player with ID " + id + " marked as deleted successfully.");
        } catch (PlayerNotFound e) {
            logger.error("Player with ID: {} not found", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while marking Player with ID: {} as deleted", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

}
