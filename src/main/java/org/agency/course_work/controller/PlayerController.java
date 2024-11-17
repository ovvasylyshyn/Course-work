package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.PlayerAgentDto;
import org.agency.course_work.dto.PlayerCreationDto;
import org.agency.course_work.dto.PlayerDetailsDto;
import org.agency.course_work.dto.PlayerDto;
import org.agency.course_work.enums.PlayerPosition;
import org.agency.course_work.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("api/player")
@AllArgsConstructor
public class PlayerController {
    private PlayerService playerService;

    @GetMapping("{id}")
    public ResponseEntity<PlayerDto>getPlayerById(@PathVariable("id")Long id){
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

@PostMapping("/create")
public ResponseEntity<PlayerDto> createPlayer(@Valid @RequestBody PlayerCreationDto playerDto) {
    PlayerDto createdPlayer = playerService.createPlayer(playerDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdPlayer);
}

    @GetMapping
    public ResponseEntity<List<PlayerDto>> getAllPlayers(){
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    @GetMapping("/{id}/with-agent")
    public ResponseEntity<PlayerAgentDto> getPlayerWithAgent(@PathVariable Long id) {
        PlayerAgentDto playerAgentDto = playerService.getPlayerWithAgent(id);
        return ResponseEntity.ok(playerAgentDto);
    }

    @GetMapping("/agent/{agentId}/players")
    public ResponseEntity<List<PlayerDto>> getPlayersByAgent(@PathVariable Long agentId) {
        List<PlayerDto> players = playerService.getPlayersByAgent(agentId);
        return ResponseEntity.ok(players);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerDto> updatePlayer(@PathVariable Long id, @RequestBody @Valid PlayerDto playerDto) {
        PlayerDto updatedPlayer = playerService.updatePlayer(id,playerDto);
        return ResponseEntity.ok(updatedPlayer);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<PlayerDetailsDto> getPlayerDetails(@PathVariable Long id) {
        PlayerDetailsDto playerDetails = playerService.getPlayerDetails(id);
        return ResponseEntity.ok(playerDetails);
    }

    @GetMapping("/sorted")
    public ResponseEntity<List<PlayerDto>> getSortedPlayers(@RequestParam String sortBy, @RequestParam String order) {
        List<PlayerDto> sortedPlayers = playerService.getSortedPlayers(sortBy, order);
        return ResponseEntity.ok(sortedPlayers);
    }

    @GetMapping("/filter")
    public ResponseEntity<Object> getFilteredPlayers(@RequestParam(required = false) Integer age, @RequestParam(required = false) String name,
                                                     @RequestParam(required = false) String surname, @RequestParam(required = false) String nationality,
                                                     @RequestParam(required = false) BigDecimal minValue, @RequestParam(required = false) BigDecimal maxValue, @RequestParam(required = false)PlayerPosition position) {
        List<PlayerDto> filteredPlayers = playerService.getFilteredPlayers(age, name, surname, nationality, minValue, maxValue,position);
        if (filteredPlayers.isEmpty()) {
            return new ResponseEntity<>("No players found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredPlayers, HttpStatus.OK);
    }
}
