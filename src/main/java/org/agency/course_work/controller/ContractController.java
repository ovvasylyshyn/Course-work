package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.*;
import org.agency.course_work.entity.Contract;
import org.agency.course_work.exception.AgentNotFound;
import org.agency.course_work.repository.ContractRepository;
import org.agency.course_work.service.AgentService;
import org.agency.course_work.service.ContractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@AllArgsConstructor
@RequestMapping("api/contracts")
public class ContractController {
    private final ContractService contractService;
    private final ContractRepository contractRepository;
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

    @GetMapping("{id}")
    @Cacheable(value = "contracts", key = "#id")
    public ResponseEntity<ContractDto> getContractById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(contractService.getContractById(id));
    }

    @PostMapping
    @CacheEvict(value = "contracts", allEntries = true)
    public ResponseEntity<ContractDto> createContract(@Valid @RequestBody ContractCreationDto contractDto) {
        ContractDto createdContract = contractService.createContract(contractDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdContract);
    }

    @GetMapping
    @Cacheable(value = "contracts")
    public ResponseEntity<?> getAllContracts(@PageableDefault Pageable pageable) {
        Page<ContractDto> contractDtos = contractService.getAllContracts(pageable);
        if (contractDtos.isEmpty()) {
            return new ResponseEntity<>("No contracts found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(contractDtos, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @CacheEvict(value = "contracts", allEntries = true)
    public ResponseEntity<ContractDto> updateContract(@PathVariable Long id, @RequestBody @Valid ContractDto contractDto) {
        ContractDto updatedContract = contractService.updateContract(id, contractDto);
        return ResponseEntity.ok(updatedContract);
    }

    @GetMapping("/sort")
    public ResponseEntity<?> getSortedContracts(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<ContractDto> sortedContracts = contractService.getSortedContracts(sortBy, order, pageable);
        if (sortedContracts.isEmpty()) {
            return new ResponseEntity<>("No contracts found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedContracts, HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredContracts(@RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate, @RequestParam(required = false) BigDecimal minSalary,
                                                  @RequestParam(required = false) BigDecimal maxSalary, @PageableDefault Pageable pageable) {
        Page<ContractDto> filteredContracts = contractService.getFilteredContracts(startDate, endDate, minSalary, maxSalary, pageable);
        if (filteredContracts.isEmpty()) {
            return new ResponseEntity<>("No contracts found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredContracts, HttpStatus.OK);
    }

    @PostMapping("/{contractId}/send")
    public ResponseEntity<String> sendContractAsPdf(@PathVariable Long contractId, @RequestParam String recipientEmail) {
        try {
            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new RuntimeException("Contract with ID " + contractId + " not found."));
            contractService.sendContractAsPdf(contract, recipientEmail);
            return ResponseEntity.ok("Contract sent to " + recipientEmail + " successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send contract: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/time-left")
    @Cacheable(value = "contracts", key = "#id")
    public ResponseEntity<ContractTimeLeftDto> getTimeLeftUntilContractEnd(@PathVariable Long id) {
        ContractTimeLeftDto timeLeft = contractService.getTimeLeftUntilContractEnd(id);
        return ResponseEntity.ok(timeLeft);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContract(@PathVariable Long id) {
        logger.info("Received request to mark Contract with ID: {} as deleted", id);

        try {
            contractService.deleteContractById(id);
            logger.info("Contract with ID: {} marked as deleted successfully", id);
            return ResponseEntity.ok("Contract with ID " + id + " marked as deleted successfully.");
        } catch (IllegalArgumentException e) {
            logger.error("Error marking Contract with ID: {} as deleted", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while marking Contract with ID: {} as deleted", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }


}
