package org.agency.course_work.service;

import lombok.AllArgsConstructor;
import org.agency.course_work.dto.ClubCreationDto;
import org.agency.course_work.dto.ClubDto;
import org.agency.course_work.entity.Club;
import org.agency.course_work.enums.Stadium;
import org.agency.course_work.exception.ClubNotFound;
import org.agency.course_work.mapper.ClubMapper;
import org.agency.course_work.repository.ClubRepository;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import org.slf4j.Logger;

@Service
@AllArgsConstructor
@Transactional
public class ClubService {
    private static final Logger logger = LoggerFactory.getLogger(ClubService.class);

    private final ClubRepository clubRepository;
    private final ClubMapper clubMapper;

    @Transactional(readOnly = true)
    public ClubDto getClubById(Long id) {
        logger.info("Fetching club by ID: {}", id);
        try {
            Club club = clubRepository.findById(id).orElseThrow(() -> new ClubNotFound("Club not found"));
            logger.info("Club with ID {} fetched successfully", id);
            return clubMapper.toDto(club);
        } catch (ClubNotFound e) {
            logger.warn("Club with ID {} not found", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error while fetching club by ID: {}", id, e);
            throw e;
        }
    }

    public ClubDto createClub(ClubCreationDto club) {
        logger.info("Creating new club: {}", club);
        try {
            ClubDto createdClub = clubMapper.toDto(clubRepository.save(clubMapper.toEntity(club)));
            logger.info("Club created successfully: {}", createdClub);
            return createdClub;
        } catch (Exception e) {
            logger.error("Error while creating club: {}", club, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<ClubDto> getAllClubs(Pageable pageable) {
        logger.info("Fetching all clubs with pageable: {}", pageable);
        try {
            Page<ClubDto> clubsPage = clubRepository.findAll(pageable).map(clubMapper::toDto);
            logger.info("Fetched {} clubs successfully", clubsPage.getTotalElements());
            return clubsPage;
        } catch (Exception e) {
            logger.error("Error while fetching all clubs", e);
            throw e;
        }
    }

    public ClubDto updateClub(Long id, ClubDto clubDto) {
        logger.info("Updating club with ID: {}, data: {}", id, clubDto);
        try {
            Club club = clubRepository.findById(id)
                    .orElseThrow(() -> new ClubNotFound("Club not found"));
            logger.debug("Club before update: {}", club);
            clubMapper.partialUpdate(clubDto, club);
            ClubDto updatedClub = clubMapper.toDto(clubRepository.save(club));
            logger.info("Club with ID {} updated successfully", id);
            return updatedClub;
        } catch (ClubNotFound e) {
            logger.warn("Club with ID {} not found for update", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error while updating club with ID: {}", id, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<ClubDto> getSortedClubs(String sortBy, String order, Pageable pageable) {
        logger.info("Fetching sorted clubs: sortBy={}, order={}, pageable={}", sortBy, order, pageable);
        try {
            Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            Page<Club> clubsPage = clubRepository.findAll(sortedPageable);
            logger.info("Sorted clubs fetched successfully. Total found: {}", clubsPage.getTotalElements());
            return clubsPage.map(club -> new ClubDto(club.getId(), club.getCreatedAt(), club.getUpdatedAt(), club.getName(),
                    club.getStadium(), club.getCountry(), club.getBudget()));
        } catch (Exception e) {
            logger.error("Error while fetching sorted clubs: sortBy={}, order={}", sortBy, order, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<ClubDto> getFilteredClubs(String name, Stadium stadium, String country, BigDecimal minBudget, BigDecimal maxBudget, Pageable pageable) {
        logger.info("Fetching filtered clubs with parameters: name={}, stadium={}, country={}, minBudget={}, maxBudget={}",
                name, stadium, country, minBudget, maxBudget);
        try {
            Specification<Club> specification = Specification.where(null);

            if (name != null && !name.isEmpty()) {
                specification = specification.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
                logger.debug("Added filter for name: {}", name);
            }
            if (stadium != null) {
                specification = specification.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("stadium"), stadium));
                logger.debug("Added filter for stadium: {}", stadium);
            }
            if (country != null && !country.isEmpty()) {
                specification = specification.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(criteriaBuilder.lower(root.get("country")), country.toLowerCase()));
                logger.debug("Added filter for country: {}", country);
            }
            if (minBudget != null) {
                specification = specification.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.greaterThanOrEqualTo(root.get("budget"), minBudget));
                logger.debug("Added filter for minBudget: {}", minBudget);
            }
            if (maxBudget != null) {
                specification = specification.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.lessThanOrEqualTo(root.get("budget"), maxBudget));
                logger.debug("Added filter for maxBudget: {}", maxBudget);
            }

            Page<Club> clubsPage = clubRepository.findAll(specification, pageable);
            logger.info("Filtered clubs fetched successfully. Total found: {}", clubsPage.getTotalElements());
            return clubsPage.map(club -> new ClubDto(club.getId(), club.getCreatedAt(), club.getUpdatedAt(), club.getName(),
                    club.getStadium(), club.getCountry(), club.getBudget()));
        } catch (Exception e) {
            logger.error("Error while fetching filtered clubs with parameters: name={}, stadium={}, country={}, minBudget={}, maxBudget={}",
                    name, stadium, country, minBudget, maxBudget, e);
            throw e;
        }
    }

    public void deleteClubById(Long id) {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Attempting to mark Club with ID: {} as deleted", id);

        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ClubNotFound("Club with ID " + id + " not found."));
        club.setDeleted(true);
        clubRepository.save(club);

        logger.info("Club with ID: {} marked as deleted successfully", id);
    }
}

