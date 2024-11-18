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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;

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

    public Page<MatchDto> getAllMatches(Pageable pageable) {
        return matchRepository.findAll(pageable).map(matchMapper::toDto);
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

    public Page<MatchDto> getSortedMatches(String sortBy, String order, Pageable pageable) {
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Match> matchesPage = matchRepository.findAll(sortedPageable);
        return matchesPage.map(match -> new MatchDto(
                match.getId(), match.getCreatedAt(), match.getUpdatedAt(), match.getDate(), match.getCity(), match.getScore()
        ));
    }

    public Page<MatchDto> getFilteredMatches(LocalDate startDate, LocalDate endDate, City city, String score, Pageable pageable) {
        Specification<Match> specification = Specification.where(null);
        if (startDate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startDate));
        }
        if (endDate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("date"), endDate));
        }
        if (city != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("city"), city));
        }
        if (score != null && !score.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.lower(root.get("score")), score.toLowerCase()));
        }
        Page<Match> matchesPage = matchRepository.findAll(specification, pageable);
        return matchesPage.map(match -> new MatchDto(
                match.getId(), match.getCreatedAt(), match.getUpdatedAt(),
                match.getDate(), match.getCity(), match.getScore()));
    }


}





