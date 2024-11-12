package org.agency.course_work.mapper;

import org.agency.course_work.dto.ClubCreationDto;
import org.agency.course_work.dto.ClubDto;
import org.agency.course_work.entity.Club;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClubMapper {
    Club toEntity(ClubDto clubDto);

    ClubDto toDto(Club club);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Club partialUpdate(ClubDto clubDto, @MappingTarget Club club);

    Club toEntity(ClubCreationDto clubCreationDto);

}