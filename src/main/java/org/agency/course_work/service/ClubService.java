package org.agency.course_work.service;

import lombok.AllArgsConstructor;
import org.agency.course_work.dto.ClubCreationDto;
import org.agency.course_work.dto.ClubDto;
import org.agency.course_work.entity.Club;
import org.agency.course_work.exception.ClubNotFound;
import org.agency.course_work.mapper.ClubMapper;
import org.agency.course_work.repository.ClubRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class ClubService {
private final ClubRepository clubRepository;
private final ClubMapper clubMapper;

@Transactional(readOnly = true)
    public ClubDto getClubById(Long id) {
    Club club = clubRepository.findById(id).orElseThrow(()-> new ClubNotFound("Club not found"));
    return clubMapper.toDto(club);
}

public ClubDto createClub(ClubCreationDto club) {
    return clubMapper.toDto(clubRepository.save(clubMapper.toEntity(club)));
}

@Transactional(readOnly = true)
    public List<ClubDto> getAllClubs() {
    return clubRepository.findAll().stream()
            .map(clubMapper::toDto)
            .toList();
}

    public ClubDto updateClub(Long id, ClubDto clubDto) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ClubNotFound("Club not found"));
        clubMapper.partialUpdate(clubDto, club);
        return clubMapper.toDto(clubRepository.save(club));
    }
}
