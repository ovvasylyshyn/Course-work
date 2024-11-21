package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.*;
import org.agency.course_work.enums.City;
import org.agency.course_work.exception.AgentNotFound;
import org.agency.course_work.exception.MatchNotFound;
import org.agency.course_work.service.AgentService;
import org.agency.course_work.service.MatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

    @GetMapping("{id}")
    @Cacheable(value = "matches", key = "#id")
    public ResponseEntity<MatchDto> getMatchById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @PostMapping
    @CacheEvict(value = "matches", allEntries = true)
    public ResponseEntity<MatchDto> createMatch(@RequestBody @Valid MatchCreationDto matchDto) {
        MatchDto savedMatch = matchService.createMatch(matchDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMatch);
    }

    @GetMapping
    @Cacheable(value = "matches")
    public ResponseEntity<?> getAllMatches(@PageableDefault Pageable pageable) {
        Page<MatchDto> matchDtos = matchService.getAllMatches(pageable);
        if (matchDtos.isEmpty()) {
            return new ResponseEntity<>("No matches found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(matchDtos, HttpStatus.OK);
    }

    @GetMapping("/matches")
    @Cacheable(value = "matches")
    public ResponseEntity<?> getMatchesWithClubs(@PageableDefault Pageable pageable) {
        Page<MathesWithClubsDto> matchesWithClubs = matchService.getMatchesWithClubs(pageable);
        if (matchesWithClubs.isEmpty()) {
            return new ResponseEntity<>("No matches with clubs found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(matchesWithClubs, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"matches", "agents", "players", "clubs"})
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

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMatch(@PathVariable Long id) {
        logger.info("Received request to mark Match with ID: {} as deleted", id);

        try {
            matchService.deleteMatchById(id);
            logger.info("Match with ID: {} marked as deleted successfully", id);
            return ResponseEntity.ok("Match with ID " + id + " marked as deleted successfully.");
        } catch (MatchNotFound e) {
            logger.error("Error marking Match with ID: {} as deleted", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while marking Match with ID: {} as deleted", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

}
