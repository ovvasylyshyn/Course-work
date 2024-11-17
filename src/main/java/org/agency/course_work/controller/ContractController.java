package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.*;
import org.agency.course_work.service.ContractService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<ContractDto>> getAllContracts() {
        return ResponseEntity.ok(contractService.getAllContracts());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContractDto> updateContract(@PathVariable Long id, @RequestBody @Valid ContractDto contractDto) {
        ContractDto updatedContract = contractService.updateContract(id, contractDto);
        return ResponseEntity.ok(updatedContract);
    }
}
