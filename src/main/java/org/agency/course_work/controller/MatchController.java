package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.*;
import org.agency.course_work.enums.City;
import org.agency.course_work.service.MatchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @GetMapping("/sort")
    public ResponseEntity<Object> getSortedMatches(@RequestParam String sortBy, @RequestParam String order) {
        List<MatchDto> sortedMatches = matchService.getSortedMatches(sortBy, order);
        if (sortedMatches.isEmpty()) {
            return new ResponseEntity<>("No matches found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedMatches, HttpStatus.OK);
    }
    @GetMapping("/filter")
    public ResponseEntity<Object> getFilteredMatches(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                     @RequestParam(required = false) City city, @RequestParam(required = false) String score) {
        List<MatchDto> filteredMatches = matchService.getFilteredMatches(startDate, endDate, city, score);
        if (filteredMatches.isEmpty()) {
            return new ResponseEntity<>("No matches found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredMatches, HttpStatus.OK);
    }
}
