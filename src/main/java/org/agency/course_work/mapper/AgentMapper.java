package org.agency.course_work.mapper;

import org.agency.course_work.dto.AgentCreationDto;
import org.agency.course_work.dto.AgentDto;
import org.agency.course_work.entity.Agent;
import org.mapstruct.*;


@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AgentMapper {
    Agent toEntity(AgentDto agentDto);

    AgentDto toDto(Agent agent);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Agent partialUpdate(AgentDto agentDto, @MappingTarget Agent agent);

    Agent toEntity(AgentCreationDto agentCreationDto);

}