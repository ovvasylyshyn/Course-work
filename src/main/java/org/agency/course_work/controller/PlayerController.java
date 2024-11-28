package org.agency.course_work.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("api/players")
@AllArgsConstructor
public class PlayerController {
    private final PlayerService playerService;
    private static final Logger logger = LoggerFactory.getLogger(PlayerController.class);

    @Operation(summary = "Get a player by ID", description = "Fetches a player by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player found"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @GetMapping("{id}")
    @Cacheable(value = "players", key = "#id")
    public ResponseEntity<PlayerDto> getPlayerById(@Parameter(description = "ID of the player to be fetched") @PathVariable("id") Long id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

    @Operation(summary = "Create a new player", description = "Creates a new player in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Player created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/create")
    @CacheEvict(value = {"agents", "clubs", "players"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlayerDto> createPlayer(@Valid @RequestBody PlayerCreationDto playerDto) {
        PlayerDto createdPlayer = playerService.createPlayer(playerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlayer);
    }

    @Operation(summary = "Get all players", description = "Fetches all players with pagination.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Players fetched successfully")
    })
    @GetMapping
    @Cacheable(value = "players")
    public Page<PlayerDto> getAllPlayers(@PageableDefault Pageable pageable) {
        return playerService.getAllPlayers(pageable);
    }

    @Operation(summary = "Get player with agent", description = "Fetches player details along with their agent information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player with agent found"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @GetMapping("/{id}/with-agent")
    @Cacheable(value = "players", key = "#id")
    public ResponseEntity<PlayerAgentDto> getPlayerWithAgent(@Parameter(description = "ID of the player to fetch") @PathVariable Long id) {
        PlayerAgentDto playerAgentDto = playerService.getPlayerWithAgent(id);
        return ResponseEntity.ok(playerAgentDto);
    }

    @Operation(summary = "Get players by agent", description = "Fetches all players associated with a specific agent.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Players fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Agent not found")
    })
    @GetMapping("/agents/{agentId}/players")
    @Cacheable(value = "players")
    public Page<PlayerDto> getPlayersByAgent(@Parameter(description = "ID of the agent") @PathVariable Long agentId, @PageableDefault Pageable pageable) {
        return playerService.getPlayersByAgent(agentId, pageable);
    }

    @Operation(summary = "Update a player", description = "Updates the details of an existing player.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player updated successfully"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @PutMapping("/{id}")
    @CacheEvict(value = {"agents", "clubs", "players"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlayerDto> updatePlayer(@Parameter(description = "ID of the player to be updated") @PathVariable Long id, @RequestBody @Valid PlayerDto playerDto) {
        PlayerDto updatedPlayer = playerService.updatePlayer(id, playerDto);
        return ResponseEntity.ok(updatedPlayer);
    }

    @Operation(summary = "Get player details", description = "Fetches detailed information about a player.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player details fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @GetMapping("/{id}/details")
    @Cacheable(value = "players", key = "#id")
    public ResponseEntity<PlayerDetailsDto> getPlayerDetails(@Parameter(description = "ID of the player to fetch details for") @PathVariable Long id) {
        PlayerDetailsDto playerDetails = playerService.getPlayerDetails(id);
        return ResponseEntity.ok(playerDetails);
    }

    @Operation(summary = "Get sorted players", description = "Fetches a list of players sorted by specified criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Players fetched successfully")
    })
    @GetMapping("/sorted")
    public Page<PlayerDto> getSortedPlayers(@RequestParam String sortBy, @RequestParam(defaultValue = "asc") String order, @PageableDefault Pageable pageable) {
        return playerService.getSortedPlayers(sortBy, order, pageable);
    }

    @Operation(summary = "Filter players", description = "Filters players based on provided criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filtered players fetched successfully"),
            @ApiResponse(responseCode = "404", description = "No players found")
    })
    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredPlayers(@RequestParam(required = false) Integer age, @RequestParam(required = false) String name, @RequestParam(required = false) String surname,
                                                @RequestParam(required = false) String nationality, @RequestParam(required = false) BigDecimal minValue, @RequestParam(required = false) BigDecimal maxValue, @RequestParam(required = false) PlayerPosition position, @PageableDefault Pageable pageable) {
        Page<PlayerDto> filteredPlayers = playerService.getFilteredPlayers(age, name, surname, nationality, minValue, maxValue, position, pageable);
        if (filteredPlayers.isEmpty()) {
            return new ResponseEntity<>("No players found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredPlayers, HttpStatus.OK);
    }

    @Operation(summary = "Delete player", description = "Marks a player as deleted by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    @CacheEvict(value = {"agents", "clubs", "players"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deletePlayer(@Parameter(description = "ID of the player to be deleted") @PathVariable Long id) {
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
