package org.agency.course_work.mapper;

import org.agency.course_work.dto.ContractCreationDto;
import org.agency.course_work.dto.ContractDto;
import org.agency.course_work.entity.Contract;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContractMapper {
    Contract toEntity(ContractDto contractDto);

    ContractDto toDto(Contract contract);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Contract partialUpdate(ContractDto contractDto, @MappingTarget Contract contract);

    Contract toEntity(ContractCreationDto contractCreationDto);

}