package org.agency.course_work.service;

import lombok.AllArgsConstructor;
import org.agency.course_work.dto.ClubCreationDto;
import org.agency.course_work.dto.ClubDto;
import org.agency.course_work.entity.Club;
import org.agency.course_work.enums.Stadium;
import org.agency.course_work.exception.ClubNotFound;
import org.agency.course_work.mapper.ClubMapper;
import org.agency.course_work.repository.ClubRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
@Transactional
public class ClubService {
    private final ClubRepository clubRepository;
    private final ClubMapper clubMapper;

    @Transactional(readOnly = true)
    public ClubDto getClubById(Long id) {
        Club club = clubRepository.findById(id).orElseThrow(() -> new ClubNotFound("Club not found"));
        return clubMapper.toDto(club);
    }

    public ClubDto createClub(ClubCreationDto club) {
        return clubMapper.toDto(clubRepository.save(clubMapper.toEntity(club)));
    }

    @Transactional(readOnly = true)
    public Page<ClubDto> getAllClubs(Pageable pageable) {
        return clubRepository.findAll(pageable).map(clubMapper::toDto);
    }

    public ClubDto updateClub(Long id, ClubDto clubDto) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ClubNotFound("Club not found"));
        clubMapper.partialUpdate(clubDto, club);
        return clubMapper.toDto(clubRepository.save(club));
    }

    @Transactional(readOnly = true)
    public Page<ClubDto> getSortedClubs(String sortBy, String order, Pageable pageable) {
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Club> clubsPage = clubRepository.findAll(sortedPageable);
        return clubsPage.map(club -> new ClubDto(club.getId(), club.getCreatedAt(), club.getUpdatedAt(), club.getName(),
                club.getStadium(), club.getCountry(), club.getBudget()));
    }

    @Transactional(readOnly = true)
    public Page<ClubDto> getFilteredClubs(String name, Stadium stadium, String country, BigDecimal minBudget, BigDecimal maxBudget, Pageable pageable) {
        Specification<Club> specification = Specification.where(null);
        if (name != null && !name.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (stadium != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("stadium"), stadium));
        }
        if (country != null && !country.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.lower(root.get("country")), country.toLowerCase()));
        }
        if (minBudget != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("budget"), minBudget));
        }
        if (maxBudget != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("budget"), maxBudget));
        }
        Page<Club> clubsPage = clubRepository.findAll(specification, pageable);
        return clubsPage.map(club -> new ClubDto(club.getId(), club.getCreatedAt(), club.getUpdatedAt(), club.getName(),
                club.getStadium(), club.getCountry(), club.getBudget()));
    }


}
