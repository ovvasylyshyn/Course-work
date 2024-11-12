package org.agency.course_work.mapper;

import org.agency.course_work.dto.MatchCreationDto;
import org.agency.course_work.dto.MatchDto;
import org.agency.course_work.entity.Match;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MatchMapper {
    Match toEntity(MatchDto matchDto);

    MatchDto toDto(Match match);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Match partialUpdate(MatchDto matchDto, @MappingTarget Match match);

    Match toEntity(MatchCreationDto matchCreationDto);

}