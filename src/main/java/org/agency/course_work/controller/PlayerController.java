package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.PlayerAgentDto;
import org.agency.course_work.dto.PlayerCreationDto;
import org.agency.course_work.dto.PlayerDto;
import org.agency.course_work.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

//    @PostMapping
//    public ResponseEntity<PlayerDto> addPlayer(@Valid @RequestBody PlayerCreationDto playerCreationDto){
//        return new ResponseEntity<>(playerService.createPlayer(playerCreationDto), HttpStatus.CREATED);
//    }
//@PostMapping("/create")
//public ResponseEntity<PlayerDto> createPlayer(@RequestBody PlayerCreationDto playerDto, @RequestParam Long agentId) {
//    PlayerDto createdPlayer = playerService.createPlayer(playerDto, agentId);
//    return ResponseEntity.status(HttpStatus.CREATED).body(createdPlayer);
//}
@PostMapping("/create")
public ResponseEntity<PlayerDto> createPlayer(@RequestBody PlayerCreationDto playerDto) {
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
}
