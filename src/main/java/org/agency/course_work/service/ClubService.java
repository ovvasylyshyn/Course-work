package org.agency.course_work.service;

import lombok.AllArgsConstructor;
import org.agency.course_work.dto.ClubCreationDto;
import org.agency.course_work.dto.ClubDto;
import org.agency.course_work.entity.Club;
import org.agency.course_work.enums.Stadium;
import org.agency.course_work.exception.ClubNotFound;
import org.agency.course_work.mapper.ClubMapper;
import org.agency.course_work.repository.ClubRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Transactional(readOnly = true)
    public List<ClubDto> getSortedClubs(String sortBy, String order) {
        List<Club> clubs = clubRepository.findAll();
        List<Club> sortedClubs;
        switch (sortBy) {
            case "name":
                sortedClubs = clubs.stream()
                        .sorted((c1, c2) -> order.equals("asc") ?
                                c1.getName().compareToIgnoreCase(c2.getName()) :
                                c2.getName().compareToIgnoreCase(c1.getName()))
                        .toList();
                break;
            case "stadium":
                sortedClubs = clubs.stream()
                        .sorted((c1, c2) -> order.equals("asc") ?
                                c1.getStadium().compareTo(c2.getStadium()) :
                                c2.getStadium().compareTo(c1.getStadium()))
                        .toList();
                break;
            case "country":
                sortedClubs = clubs.stream()
                        .sorted((c1, c2) -> order.equals("asc") ?
                                c1.getCountry().compareToIgnoreCase(c2.getCountry()) :
                                c2.getCountry().compareToIgnoreCase(c1.getCountry()))
                        .toList();
                break;
            case "budget":
                sortedClubs = clubs.stream()
                        .sorted((c1, c2) -> order.equals("asc") ?
                                c1.getBudget().compareTo(c2.getBudget()) :
                                c2.getBudget().compareTo(c1.getBudget()))
                        .toList();
                break;
            default:
                throw new IllegalArgumentException("Unsupported sorting parameter: " + sortBy);
        }
        return sortedClubs.stream()
                .map(club -> new ClubDto(
                        club.getId(), club.getCreatedAt(), club.getUpdatedAt(), club.getName(),
                        club.getStadium(), club.getCountry(), club.getBudget()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClubDto> getFilteredClubs(String name, Stadium stadium, String country, BigDecimal minBudget, BigDecimal maxBudget) {
        List<Club> clubs = clubRepository.findAll();
        if (name != null && !name.isEmpty()) {
            clubs = clubs.stream()
                    .filter(club -> club.getName().equalsIgnoreCase(name))
                    .toList();
        }
        if (stadium != null) {
            clubs = clubs.stream()
                    .filter(club -> club.getStadium().equals(stadium))
                    .toList();
        }
        if (country != null && !country.isEmpty()) {
            clubs = clubs.stream()
                    .filter(club -> club.getCountry().equalsIgnoreCase(country))
                    .toList();
        }
        if (minBudget != null) {
            clubs = clubs.stream()
                    .filter(club -> club.getBudget() != null && club.getBudget().compareTo(minBudget) >= 0)
                    .toList();
        }
        if (maxBudget != null) {
            clubs = clubs.stream()
                    .filter(club -> club.getBudget() != null && club.getBudget().compareTo(maxBudget) <= 0)
                    .toList();
        }
        return clubs.stream()
                .map(club -> new ClubDto(
                        club.getId(), club.getCreatedAt(), club.getUpdatedAt(), club.getName(),
                        club.getStadium(), club.getCountry(), club.getBudget()))
                .toList();
    }

}
