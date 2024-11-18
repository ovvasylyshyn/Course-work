package org.agency.course_work.controller;


import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.ClubCreationDto;
import org.agency.course_work.dto.ClubDto;
import org.agency.course_work.enums.Stadium;
import org.agency.course_work.service.ClubService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("api/club")
@AllArgsConstructor
public class ClubController {
    private final ClubService clubService;

    @PostMapping
    public ResponseEntity<ClubDto> createClub(@Valid @RequestBody ClubCreationDto clubCreationDto) {
        return new ResponseEntity(clubService.createClub(clubCreationDto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> getAllClubs(@PageableDefault Pageable pageable) {
        Page<ClubDto> clubDtos = clubService.getAllClubs(pageable);
        if (clubDtos.isEmpty()) {
            return new ResponseEntity<>("No clubs found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(clubDtos, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<ClubDto> getClubById(@PathVariable("id") Long id) {
            return ResponseEntity.ok(clubService.getClubById(id));
    }

    @PutMapping("/{id}")
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


}
