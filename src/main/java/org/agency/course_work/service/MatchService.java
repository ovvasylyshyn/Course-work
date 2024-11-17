package org.agency.course_work.service;

import lombok.AllArgsConstructor;
import org.agency.course_work.dto.MatchCreationDto;
import org.agency.course_work.dto.MatchDto;
import org.agency.course_work.dto.MathesWithClubsDto;
import org.agency.course_work.entity.Club;
import org.agency.course_work.entity.Match;
import org.agency.course_work.enums.City;
import org.agency.course_work.exception.MatchNotFound;
import org.agency.course_work.mapper.MatchMapper;
import org.agency.course_work.repository.ClubRepository;
import org.agency.course_work.repository.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class MatchService {
    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;
    private final ClubRepository clubRepository;

    public MatchDto getMatchById(Long id) {
        Match match = matchRepository.findById(id).orElseThrow(()-> new MatchNotFound("Match not found"));
        return matchMapper.toDto(match);
    }

    @Transactional
    public MatchDto createMatch(MatchCreationDto match) {
//        return matchMapper.toDto(matchRepository.save(matchMapper.toEntity(match)));
            Match matchCreated = matchMapper.toEntity(match);
            List<Club> clubs = clubRepository.findAllById(match.clubIds());
            if (clubs.size() != match.clubIds().size()) {
                throw new IllegalArgumentException("One or more Club IDs are invalid.");
            }
            for (Club club : clubs) {
                club.getMatches().add(matchCreated);
                matchCreated.getClubs().add(club);
            }
            Match savedMatch = matchRepository.save(matchCreated);
            return matchMapper.toDto(savedMatch);
    }

    public List<MatchDto> getAllMatches() {
        return matchRepository.findAll().stream()
                .map(matchMapper::toDto)
                .toList();
    }

    public List<MathesWithClubsDto> getMatchesWithClubs() {
        return matchRepository.findAll().stream()
                .map(match -> new MathesWithClubsDto(
                        match.getId(), match.getCreatedAt(), match.getUpdatedAt(), match.getDate(),
                        match.getCity(), match.getScore(),
                        match.getClubs().stream()
                                .map(Club::getName)
                                .toList()
                ))
                .toList();
    }

    @Transactional
    public MatchDto updateMatch(Long id, MatchDto matchDto) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new MatchNotFound("Match not found"));
        matchMapper.partialUpdate(matchDto, match);
        return matchMapper.toDto(matchRepository.save(match));
    }

    public List<MatchDto> getSortedMatches(String sortBy, String order) {
        List<Match> matches = matchRepository.findAll();
        List<Match> sortedMatches;
        switch (sortBy) {
            case "date":
                sortedMatches = matches.stream()
                        .sorted((m1, m2) -> order.equalsIgnoreCase("asc") ?
                                m1.getDate().compareTo(m2.getDate()) :
                                m2.getDate().compareTo(m1.getDate()))
                        .toList();
                break;
            case "city":
                sortedMatches = matches.stream()
                        .sorted((m1, m2) -> order.equalsIgnoreCase("asc") ?
                                m1.getCity().compareTo(m2.getCity()) :
                                m2.getCity().compareTo(m1.getCity()))
                        .toList();
                break;
            case "score":
                sortedMatches = matches.stream()
                        .sorted((m1, m2) -> order.equalsIgnoreCase("asc") ?
                                m1.getScore().compareTo(m2.getScore()) :
                                m2.getScore().compareTo(m1.getScore()))
                        .toList();
                break;
            default:
                throw new IllegalArgumentException("Unsupported sorting parameter: " + sortBy);
        }
        return sortedMatches.stream()
                .map(match -> new MatchDto(
                        match.getId(), match.getCreatedAt(), match.getUpdatedAt(),
                        match.getDate(), match.getCity(), match.getScore()))
                .toList();
    }

    public List<MatchDto> getFilteredMatches(LocalDate startDate, LocalDate endDate, City city, String score) {
        List<Match> matches = matchRepository.findAll();
        if (startDate != null) {
            matches = matches.stream()
                    .filter(match -> match.getDate() != null && !match.getDate().isBefore(startDate))
                    .toList();
        }
        if (endDate != null) {
            matches = matches.stream()
                    .filter(match -> match.getDate() != null && !match.getDate().isAfter(endDate))
                    .toList();
        }
        if (city != null) {
            matches = matches.stream()
                    .filter(match -> match.getCity().equals(city))
                    .toList();
        }
        if (score != null && !score.isEmpty()) {
            matches = matches.stream()
                    .filter(match -> match.getScore().equalsIgnoreCase(score))
                    .toList();
        }

        return matches.stream()
                .map(match -> new MatchDto(
                        match.getId(), match.getCreatedAt(), match.getUpdatedAt(),
                        match.getDate(), match.getCity(), match.getScore()))
                .toList();
    }


}





