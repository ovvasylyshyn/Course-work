package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.*;
import org.agency.course_work.enums.City;
import org.agency.course_work.service.MatchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/matches")
public class MatchController {
private final MatchService matchService;

    @GetMapping("{id}")
    public ResponseEntity<MatchDto>getMatchById(@PathVariable("id") Long id){
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @PostMapping
    public ResponseEntity<MatchDto> createMatch(@RequestBody @Valid MatchCreationDto matchDto) {
        MatchDto savedMatch = matchService.createMatch(matchDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMatch);
    }

    @GetMapping
    public ResponseEntity<?> getAllMatches(@PageableDefault Pageable pageable) {
        Page<MatchDto> matchDtos = matchService.getAllMatches(pageable);
        if (matchDtos.isEmpty()) {
            return new ResponseEntity<>("No matches found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(matchDtos, HttpStatus.OK);
    }

@GetMapping("/matches")
public ResponseEntity<?> getMatchesWithClubs(@PageableDefault Pageable pageable) {
    Page<MathesWithClubsDto> matchesWithClubs = matchService.getMatchesWithClubs(pageable);
    if (matchesWithClubs.isEmpty()) {
        return new ResponseEntity<>("No matches with clubs found.", HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(matchesWithClubs, HttpStatus.OK);
}

    @PutMapping("/{id}")
    public ResponseEntity<MatchDto> updateMatch(@PathVariable Long id, @RequestBody @Valid MatchDto matchDto) {
        MatchDto updatedMatch = matchService.updateMatch(id, matchDto);
        return ResponseEntity.ok(updatedMatch);
    }

    @GetMapping("/sort")
    public ResponseEntity<?> getSortedMatches(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<MatchDto> sortedMatches = matchService.getSortedMatches(sortBy, order, pageable);
        if (sortedMatches.isEmpty()) {
            return new ResponseEntity<>("No matches found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedMatches, HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredMatches(@RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate, @RequestParam(required = false) City city,
                                                @RequestParam(required = false) String score, @PageableDefault Pageable pageable) {
        Page<MatchDto> filteredMatches = matchService.getFilteredMatches(startDate, endDate, city, score, pageable);
        if (filteredMatches.isEmpty()) {
            return new ResponseEntity<>("No matches found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredMatches, HttpStatus.OK);
    }

}
