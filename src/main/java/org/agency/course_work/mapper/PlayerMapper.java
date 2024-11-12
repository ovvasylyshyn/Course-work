package org.agency.course_work.mapper;

import org.agency.course_work.dto.PlayerCreationDto;
import org.agency.course_work.dto.PlayerDto;
import org.agency.course_work.entity.Player;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlayerMapper {
    Player toEntity(PlayerDto playerDto);

    PlayerDto toDto(Player player);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Player partialUpdate(PlayerDto playerDto, @MappingTarget Player player);

    Player toEntity(PlayerCreationDto playerCreationDto);

}