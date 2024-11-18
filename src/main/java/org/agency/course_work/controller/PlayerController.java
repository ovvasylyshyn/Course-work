package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.PlayerAgentDto;
import org.agency.course_work.dto.PlayerCreationDto;
import org.agency.course_work.dto.PlayerDetailsDto;
import org.agency.course_work.dto.PlayerDto;
import org.agency.course_work.enums.PlayerPosition;
import org.agency.course_work.service.PlayerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

//    @GetMapping
//    public ResponseEntity<List<PlayerDto>> getAllPlayers(){
//        return ResponseEntity.ok(playerService.getAllPlayers());
//    }\

    @GetMapping
    public Page<PlayerDto> getAllPlayers(@PageableDefault Pageable pageable) {
        return playerService.getAllPlayers(pageable);
    }


    @GetMapping("/{id}/with-agent")
    public ResponseEntity<PlayerAgentDto> getPlayerWithAgent(@PathVariable Long id) {
        PlayerAgentDto playerAgentDto = playerService.getPlayerWithAgent(id);
        return ResponseEntity.ok(playerAgentDto);
    }

@GetMapping("/agents/{agentId}/players")
public Page<PlayerDto> getPlayersByAgent(@PathVariable Long agentId, @PageableDefault Pageable pageable) {
    return playerService.getPlayersByAgent(agentId, pageable);
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


}
