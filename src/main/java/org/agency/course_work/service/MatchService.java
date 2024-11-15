package org.agency.course_work.service;

import lombok.AllArgsConstructor;
import org.agency.course_work.dto.MatchCreationDto;
import org.agency.course_work.dto.MatchDto;
import org.agency.course_work.entity.Club;
import org.agency.course_work.entity.Match;
import org.agency.course_work.exception.ClubNotFound;
import org.agency.course_work.exception.MatchNotFound;
import org.agency.course_work.mapper.MatchMapper;
import org.agency.course_work.repository.ClubRepository;
import org.agency.course_work.repository.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<MatchDto> getAllMatches() {
        return matchRepository.findAll().stream()
                .map(matchMapper::toDto)
                .toList();
    }
}





