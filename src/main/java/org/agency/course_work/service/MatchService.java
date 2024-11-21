package org.agency.course_work.service;

import lombok.AllArgsConstructor;
import org.agency.course_work.dto.MatchCreationDto;
import org.agency.course_work.dto.MatchDto;
import org.agency.course_work.dto.MathesWithClubsDto;
import org.agency.course_work.entity.Club;
import org.agency.course_work.entity.Contract;
import org.agency.course_work.entity.Match;
import org.agency.course_work.enums.City;
import org.agency.course_work.exception.ContractNotFound;
import org.agency.course_work.exception.MatchNotFound;
import org.agency.course_work.mapper.MatchMapper;
import org.agency.course_work.repository.ClubRepository;
import org.agency.course_work.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class MatchService {
    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;
    private final ClubRepository clubRepository;
    private static final Logger logger = LoggerFactory.getLogger(ClubService.class);

    public MatchDto getMatchById(Long id) {
        logger.info("Fetching match with ID: {}", id);
        try {
            Match match = matchRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Match with ID: {} not found", id);
                        return new MatchNotFound("Match not found");
                    });
            logger.info("Match found: {}", match);
            return matchMapper.toDto(match);
        } catch (Exception e) {
            logger.error("Error fetching match with ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public MatchDto createMatch(MatchCreationDto match) {
        logger.info("Creating match with details: {}", match);
        try {
            // Mapping DTO to Entity
            Match matchCreated = matchMapper.toEntity(match);
            logger.debug("Mapped match creation DTO to entity: {}", matchCreated);

            // Fetching clubs for the match
            List<Club> clubs = clubRepository.findAllById(match.clubIds());
            if (clubs.size() != match.clubIds().size()) {
                logger.error("One or more Club IDs are invalid: {}", match.clubIds());
                throw new IllegalArgumentException("One or more Club IDs are invalid.");
            }

            // Associating clubs with the match
            for (Club club : clubs) {
                logger.debug("Associating match with club: {}", club.getName());
                club.getMatches().add(matchCreated);
                matchCreated.getClubs().add(club);
            }

            // Saving the match to the database
            Match savedMatch = matchRepository.save(matchCreated);
            logger.info("Match created successfully with ID: {}", savedMatch.getId());
            return matchMapper.toDto(savedMatch);
        } catch (Exception e) {
            logger.error("Error creating match. Error: {}", e.getMessage());
            throw e;
        }
    }

    public Page<MatchDto> getAllMatches(Pageable pageable) {
        logger.info("Fetching all matches with pagination: {}", pageable);
        try {
            Page<MatchDto> matchesPage = matchRepository.findAll(pageable)
                    .map(matchMapper::toDto);
            logger.info("Fetched {} matches", matchesPage.getTotalElements());
            return matchesPage;
        } catch (Exception e) {
            logger.error("Error fetching all matches with pagination. Error: {}", e.getMessage());
            throw e;
        }
    }

    public Page<MathesWithClubsDto> getMatchesWithClubs(Pageable pageable) {
        logger.info("Fetching matches with clubs information, pagination: {}", pageable);
        try {
            Page<Match> matchesPage = matchRepository.findAll(pageable);
            Page<MathesWithClubsDto> result = matchesPage.map(match ->
                    new MathesWithClubsDto(match.getId(), match.getCreatedAt(), match.getUpdatedAt(),
                            match.getDate(), match.getCity(), match.getScore(),
                            match.getClubs().stream().map(Club::getName).toList()));
            logger.info("Fetched {} matches with clubs", result.getTotalElements());
            return result;
        } catch (Exception e) {
            logger.error("Error fetching matches with clubs. Error: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public MatchDto updateMatch(Long id, MatchDto matchDto) {
        logger.info("Updating match with ID: {}", id);
        try {
            Match match = matchRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Match with ID: {} not found", id);
                        return new MatchNotFound("Match not found");
                    });
            logger.debug("Match found for update: {}", match);
            matchMapper.partialUpdate(matchDto, match);
            Match updatedMatch = matchRepository.save(match);
            logger.info("Successfully updated match with ID: {}", id);
            return matchMapper.toDto(updatedMatch);
        } catch (Exception e) {
            logger.error("Error updating match with ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    public Page<MatchDto> getSortedMatches(String sortBy, String order, Pageable pageable) {
        logger.info("Fetching sorted matches by {} in {} order, pagination: {}", sortBy, order, pageable);
        try {
            Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            Page<Match> matchesPage = matchRepository.findAll(sortedPageable);
            Page<MatchDto> result = matchesPage.map(match -> new MatchDto(
                    match.getId(), match.getCreatedAt(), match.getUpdatedAt(), match.getDate(),
                    match.getCity(), match.getScore()));
            logger.info("Fetched {} sorted matches", result.getTotalElements());
            return result;
        } catch (Exception e) {
            logger.error("Error fetching sorted matches. Error: {}", e.getMessage());
            throw e;
        }
    }

    public Page<MatchDto> getFilteredMatches(LocalDate startDate, LocalDate endDate, City city, String score, Pageable pageable) {
        logger.info("Fetching filtered matches with pagination: {}", pageable);
        logger.debug("Filter Criteria - Start Date: {}, End Date: {}, City: {}, Score: {}",
                startDate, endDate, city, score);
        try {
            Specification<Match> specification = Specification.where(null);

            if (startDate != null) {
                logger.debug("Adding start date filter: {}", startDate);
                specification = specification.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startDate));
            }
            if (endDate != null) {
                logger.debug("Adding end date filter: {}", endDate);
                specification = specification.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.lessThanOrEqualTo(root.get("date"), endDate));
            }
            if (city != null) {
                logger.debug("Adding city filter: {}", city);
                specification = specification.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("city"), city));
            }
            if (score != null && !score.isEmpty()) {
                logger.debug("Adding score filter: {}", score);
                specification = specification.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(criteriaBuilder.lower(root.get("score")), score.toLowerCase()));
            }

            logger.info("Executing the filtered query...");
            Page<Match> matchesPage = matchRepository.findAll(specification, pageable);
            logger.info("Fetched {} filtered matches", matchesPage.getTotalElements());

            return matchesPage.map(match -> new MatchDto(
                    match.getId(), match.getCreatedAt(), match.getUpdatedAt(),
                    match.getDate(), match.getCity(), match.getScore()));
        } catch (Exception e) {
            logger.error("Error fetching filtered matches. Error: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
//    public void deleteMatchById(Long id) {
//        logger.info("Deleting Match with ID: {}", id);
//        try {
//            Match match = matchRepository.findById(id).orElseThrow(() -> {
//                logger.warn("Match with ID: {} not found", id);
//                return new MatchNotFound("Match not found");
//            });
//            matchRepository.delete(match);
//            logger.info("Match with ID: {} deleted successfully", id);
//        } catch (Exception e) {
//            logger.error("Error deleting Match with ID: {}", id, e);
//            throw e;
//        }
//    }

    public void deleteMatchById(Long id) {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Attempting to mark Match with ID: {} as deleted", id);

        try {
            Match match = matchRepository.findById(id).orElseThrow(() -> {
                logger.warn("Match with ID: {} not found", id);
                return new MatchNotFound(  "Match with ID " + id + " not found.");
            });

            match.setDeleted(true);
            matchRepository.save(match);

            logger.info("Match with ID: {} marked as deleted successfully", id);
        } catch (Exception e) {
            logger.error("Error occurred while marking Match with ID: {} as deleted", id, e);
            throw e;
        }
    }

}





