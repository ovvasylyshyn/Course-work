package org.agency.course_work.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@AllArgsConstructor
@RequestMapping("api/matches")
@Tag(name = "Match", description = "Operations related to matches")
public class MatchController {
    private final MatchService matchService;
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

    @Operation(summary = "Get match by ID", description = "Returns the details of a match by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved match")
    @ApiResponse(responseCode = "404", description = "Match not found")
    @GetMapping("{id}")
    @Cacheable(value = "matches", key = "#id")
    public ResponseEntity<MatchDto> getMatchById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @Operation(summary = "Create a new match", description = "Creates a new match and returns its details")
    @ApiResponse(responseCode = "201", description = "Match created successfully")
    @PostMapping
    @CacheEvict(value = "matches", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MatchDto> createMatch(@RequestBody @Valid MatchCreationDto matchDto) {
        MatchDto savedMatch = matchService.createMatch(matchDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMatch);
    }

    @Operation(summary = "Get all matches", description = "Returns a paginated list of all matches")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of matches")
    @ApiResponse(responseCode = "404", description = "No matches found")
    @GetMapping
    @Cacheable(value = "matches")
    public ResponseEntity<?> getAllMatches(@PageableDefault Pageable pageable) {
        Page<MatchDto> matchDtos = matchService.getAllMatches(pageable);
        if (matchDtos.isEmpty()) {
            return new ResponseEntity<>("No matches found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(matchDtos, HttpStatus.OK);
    }

    @Operation(summary = "Get matches with clubs", description = "Returns a paginated list of matches with club information")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of matches with clubs")
    @ApiResponse(responseCode = "404", description = "No matches with clubs found")
    @GetMapping("/matches")
    @Cacheable(value = "matches")
    public ResponseEntity<?> getMatchesWithClubs(@PageableDefault Pageable pageable) {
        Page<MathesWithClubsDto> matchesWithClubs = matchService.getMatchesWithClubs(pageable);
        if (matchesWithClubs.isEmpty()) {
            return new ResponseEntity<>("No matches with clubs found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(matchesWithClubs, HttpStatus.OK);
    }

    @Operation(summary = "Update match by ID", description = "Updates the details of a match by its ID")
    @ApiResponse(responseCode = "200", description = "Match updated successfully")
    @ApiResponse(responseCode = "404", description = "Match not found")
    @PutMapping("/{id}")
    @CacheEvict(value = {"matches", "agents", "players", "clubs"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MatchDto> updateMatch(@PathVariable Long id, @RequestBody @Valid MatchDto matchDto) {
        MatchDto updatedMatch = matchService.updateMatch(id, matchDto);
        return ResponseEntity.ok(updatedMatch);
    }

    @Operation(summary = "Get sorted list of matches", description = "Returns a sorted list of matches based on the provided sorting parameters")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved sorted matches")
    @ApiResponse(responseCode = "404", description = "No matches found")
    @GetMapping("/sort")
    public ResponseEntity<?> getSortedMatches(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<MatchDto> sortedMatches = matchService.getSortedMatches(sortBy, order, pageable);
        if (sortedMatches.isEmpty()) {
            return new ResponseEntity<>("No matches found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedMatches, HttpStatus.OK);
    }

    @Operation(summary = "Get filtered list of matches", description = "Returns a filtered list of matches based on the provided parameters")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered matches")
    @ApiResponse(responseCode = "404", description = "No matches found")
    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredMatches(@RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate, @RequestParam(required = false) City city,
                                                @RequestParam(required = false) String score, @PageableDefault Pageable pageable) {
        Page<MatchDto> filteredMatches = matchService.getFilteredMatches(startDate, endDate, city, score, pageable);
        if (filteredMatches.isEmpty()) {
            return new ResponseEntity<>("No matches found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredMatches, HttpStatus.OK);
    }

    @Operation(summary = "Delete match by ID", description = "Marks a match as deleted by its ID")
    @ApiResponse(responseCode = "200", description = "Match marked as deleted successfully")
    @ApiResponse(responseCode = "404", description = "Match not found")
    @DeleteMapping("/{id}")
    @CacheEvict(value = "matches", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
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

