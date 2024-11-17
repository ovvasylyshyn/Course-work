package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.*;
import org.agency.course_work.service.MatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/match")
public class MatchController {
private final MatchService matchService;

    @GetMapping("{id}")
    public ResponseEntity<MatchDto>getMatchById(@PathVariable("id") Long id){
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

//    @PostMapping
//    public ResponseEntity<MatchDto> createMatch(@Valid @RequestBody MatchCreationDto matchDto) {
//        MatchDto createdMatch = matchService.createMatch(matchDto);
//        return ResponseEntity.status(HttpStatus.CREATED).body(createdMatch);
//    }

    @PostMapping
    public ResponseEntity<MatchDto> createMatch(@RequestBody @Valid MatchCreationDto matchDto) {
        MatchDto savedMatch = matchService.createMatch(matchDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMatch);
    }
    @GetMapping
    public ResponseEntity<List<MatchDto>> getAllMatches(){
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @GetMapping("/matches")
    public ResponseEntity<List<MathesWithClubsDto>> getAllMatchesWithClubs() {
        List<MathesWithClubsDto> matches = matchService.getMatchesWithClubs();
        return ResponseEntity.ok(matches);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchDto> updateMatch(@PathVariable Long id, @RequestBody @Valid MatchDto matchDto) {
        MatchDto updatedMatch = matchService.updateMatch(id, matchDto);
        return ResponseEntity.ok(updatedMatch);
    }
}
