package org.agency.course_work.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.ClubCreationDto;
import org.agency.course_work.dto.ClubDto;
import org.agency.course_work.enums.Stadium;
import org.agency.course_work.exception.AgentNotFound;
import org.agency.course_work.service.AgentService;
import org.agency.course_work.service.ClubService;
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
@RequestMapping("api/clubs")
@AllArgsConstructor
@Tag(name = "Club", description = "Operations related to clubs")
public class ClubController {

    private final ClubService clubService;
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

    @Operation(summary = "Create a new club", description = "Creates a new club and returns its details")
    @ApiResponse(responseCode = "201", description = "Club created successfully")
    @PostMapping
    @CacheEvict(value = "clubs", allEntries = true)
    public ResponseEntity<ClubDto> createClub(@Valid @RequestBody ClubCreationDto clubCreationDto) {
        return new ResponseEntity<>(clubService.createClub(clubCreationDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all clubs", description = "Returns a paginated list of all clubs")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of clubs")
    @ApiResponse(responseCode = "404", description = "No clubs found")
    @GetMapping
    @Cacheable(value = "clubs")
    public ResponseEntity<?> getAllClubs(@PageableDefault Pageable pageable) {
        Page<ClubDto> clubDtos = clubService.getAllClubs(pageable);
        if (clubDtos.isEmpty()) {
            return new ResponseEntity<>("No clubs found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(clubDtos, HttpStatus.OK);
    }

    @Operation(summary = "Get a club by ID", description = "Retrieves details of a specific club by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the club details")
    @ApiResponse(responseCode = "404", description = "Club not found")
    @GetMapping("{id}")
    @Cacheable(value = "clubs", key = "#id")
    public ResponseEntity<ClubDto> getClubById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(clubService.getClubById(id));
    }

    @Operation(summary = "Update a club", description = "Updates the details of an existing club")
    @ApiResponse(responseCode = "200", description = "Successfully updated the club")
    @ApiResponse(responseCode = "404", description = "Club not found")
    @PutMapping("/{id}")
    @CacheEvict(value = "clubs", allEntries = true)
    public ResponseEntity<ClubDto> updateClub(@PathVariable Long id, @RequestBody @Valid ClubDto clubDto) {
        ClubDto updatedClub = clubService.updateClub(id, clubDto);
        return ResponseEntity.ok(updatedClub);
    }

    @Operation(summary = "Sort clubs", description = "Retrieves a list of clubs sorted by the specified criteria")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved sorted list of clubs")
    @ApiResponse(responseCode = "404", description = "No clubs found")
    @GetMapping("/sort")
    public ResponseEntity<?> getSortedClubs(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<ClubDto> sortedClubs = clubService.getSortedClubs(sortBy, order, pageable);
        if (sortedClubs.isEmpty()) {
            return new ResponseEntity<>("No clubs found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedClubs, HttpStatus.OK);
    }

    @Operation(summary = "Filter clubs", description = "Filters clubs by various parameters such as name, stadium, country, and budget")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered list of clubs")
    @ApiResponse(responseCode = "404", description = "No clubs found matching the filter")
    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredClubs(@RequestParam(required = false) String name,
                                              @RequestParam(required = false) Stadium stadium,
                                              @RequestParam(required = false) String country,
                                              @RequestParam(required = false) BigDecimal minBudget,
                                              @RequestParam(required = false) BigDecimal maxBudget,
                                              @PageableDefault Pageable pageable) {
        Page<ClubDto> filteredClubs = clubService.getFilteredClubs(name, stadium, country, minBudget, maxBudget, pageable);
        if (filteredClubs.isEmpty()) {
            return new ResponseEntity<>("No clubs found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredClubs, HttpStatus.OK);
    }

    @Operation(summary = "Delete a club", description = "Marks a club as deleted by its ID")
    @ApiResponse(responseCode = "200", description = "Club deleted successfully")
    @ApiResponse(responseCode = "404", description = "Club not found")
    @DeleteMapping("/{id}")
    @CacheEvict(value = "clubs", allEntries = true)
    public ResponseEntity<String> deleteClubById(@PathVariable("id") Long id) {
        logger.info("Received request to delete Club with ID: {}", id);

        try {
            clubService.deleteClubById(id);
            logger.info("Club with ID: {} marked as deleted successfully", id);
            return ResponseEntity.ok("Club with ID " + id + " marked as deleted successfully.");
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting Club with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while deleting Club with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
}

