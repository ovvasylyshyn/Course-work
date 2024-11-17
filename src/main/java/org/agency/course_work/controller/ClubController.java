package org.agency.course_work.controller;


import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.ClubCreationDto;
import org.agency.course_work.dto.ClubDto;
import org.agency.course_work.service.ClubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<ClubDto>> getClubs() {
        return ResponseEntity.ok(clubService.getAllClubs());
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
}
