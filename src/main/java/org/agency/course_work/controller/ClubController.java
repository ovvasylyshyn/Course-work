package org.agency.course_work.controller;


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
public class ClubController {
    private final ClubService clubService;
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

    @PostMapping
    @CacheEvict(value = "clubs", allEntries = true)
    public ResponseEntity<ClubDto> createClub(@Valid @RequestBody ClubCreationDto clubCreationDto) {
        return new ResponseEntity(clubService.createClub(clubCreationDto), HttpStatus.CREATED);
    }

    @GetMapping
    @Cacheable(value = "clubs")
    public ResponseEntity<?> getAllClubs(@PageableDefault Pageable pageable) {
        Page<ClubDto> clubDtos = clubService.getAllClubs(pageable);
        if (clubDtos.isEmpty()) {
            return new ResponseEntity<>("No clubs found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(clubDtos, HttpStatus.OK);
    }

    @GetMapping("{id}")
    @Cacheable(value = "clubs", key = "#id")
    public ResponseEntity<ClubDto> getClubById(@PathVariable("id") Long id) {
            return ResponseEntity.ok(clubService.getClubById(id));
    }

    @PutMapping("/{id}")
    @CacheEvict(value = "agents", allEntries = true)
    public ResponseEntity<ClubDto> updateClub(@PathVariable Long id, @RequestBody @Valid ClubDto clubDto) {
        ClubDto updatedClub = clubService.updateClub(id, clubDto);
        return ResponseEntity.ok(updatedClub);
    }

    @GetMapping("/sort")
    public ResponseEntity<?> getSortedClubs(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<ClubDto> sortedClubs = clubService.getSortedClubs(sortBy, order, pageable);
        if (sortedClubs.isEmpty()) {
            return new ResponseEntity<>("No clubs found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedClubs, HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredClubs(@RequestParam(required = false) String name, @RequestParam(required = false) Stadium stadium, @RequestParam(required = false) String country, @RequestParam(required = false) BigDecimal minBudget,
                                              @RequestParam(required = false) BigDecimal maxBudget, @PageableDefault Pageable pageable) {
        Page<ClubDto> filteredClubs = clubService.getFilteredClubs(name, stadium, country, minBudget, maxBudget, pageable);
        if (filteredClubs.isEmpty()) {
            return new ResponseEntity<>("No clubs found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredClubs, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
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
