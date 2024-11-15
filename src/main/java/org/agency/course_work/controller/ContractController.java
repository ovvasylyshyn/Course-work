package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.ContractCreationDto;
import org.agency.course_work.dto.ContractDto;
import org.agency.course_work.dto.PlayerCreationDto;
import org.agency.course_work.dto.PlayerDto;
import org.agency.course_work.service.ContractService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/contract")
public class ContractController {
    private final ContractService contractService;

    @GetMapping("{id}")
    public ResponseEntity<ContractDto>getContractById(@PathVariable("id") Long id){
        return ResponseEntity.ok(contractService.getContractById(id));
    }

    @PostMapping
    public ResponseEntity<ContractDto> createContract(@Valid @RequestBody ContractCreationDto contractDto) {
        ContractDto createdContract = contractService.createContract(contractDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdContract);
    }
}
