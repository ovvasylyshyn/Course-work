package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.*;
import org.agency.course_work.service.ContractService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @GetMapping("/sort")
    public ResponseEntity<Object> getSortedContracts(@RequestParam String sortBy, @RequestParam String order) {
        List<ContractDto> sortedContracts = contractService.getSortedContracts(sortBy, order);
        if (sortedContracts.isEmpty()) {
            return new ResponseEntity<>("No contracts found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedContracts, HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<Object> getFilteredContracts(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                       @RequestParam(required = false) BigDecimal minSalary, @RequestParam(required = false) BigDecimal maxSalary) {
        List<ContractDto> filteredContracts = contractService.getFilteredContracts(startDate, endDate, minSalary, maxSalary);
        if (filteredContracts.isEmpty()) {
            return new ResponseEntity<>("No contracts found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredContracts, HttpStatus.OK);
    }


}
