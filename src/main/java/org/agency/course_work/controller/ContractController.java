package org.agency.course_work.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("api/contracts")
@AllArgsConstructor
@Tag(name = "Contract", description = "Operations related to contracts")
public class ContractController {

    private final ContractService contractService;
    private final ContractRepository contractRepository;
    private static final Logger logger = LoggerFactory.getLogger(ContractController.class);

    @Operation(summary = "Get contract by ID", description = "Returns details of the contract with the specified ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved contract")
    @ApiResponse(responseCode = "404", description = "Contract not found")
    @GetMapping("{id}")
    @Cacheable(value = "contracts", key = "#id")
    public ResponseEntity<ContractDto> getContractById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(contractService.getContractById(id));
    }

    @Operation(summary = "Create a new contract", description = "Creates a new contract and returns its details")
    @ApiResponse(responseCode = "201", description = "Contract created successfully")
    @PostMapping
    @CacheEvict(value = "contracts", allEntries = true)
    public ResponseEntity<ContractDto> createContract(@Valid @RequestBody ContractCreationDto contractDto) {
        ContractDto createdContract = contractService.createContract(contractDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdContract);
    }

    @Operation(summary = "Get all contracts", description = "Returns a paginated list of all contracts")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of contracts")
    @ApiResponse(responseCode = "404", description = "No contracts found")
    @GetMapping
    @Cacheable(value = "contracts")
    public ResponseEntity<?> getAllContracts(@PageableDefault Pageable pageable) {
        Page<ContractDto> contractDtos = contractService.getAllContracts(pageable);
        if (contractDtos.isEmpty()) {
            return new ResponseEntity<>("No contracts found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(contractDtos, HttpStatus.OK);
    }

    @Operation(summary = "Update a contract", description = "Updates the contract with the specified ID and returns its updated details")
    @ApiResponse(responseCode = "200", description = "Contract updated successfully")
    @ApiResponse(responseCode = "404", description = "Contract not found")
    @PutMapping("/{id}")
    @CacheEvict(value = "contracts", allEntries = true)
    public ResponseEntity<ContractDto> updateContract(@PathVariable Long id, @RequestBody @Valid ContractDto contractDto) {
        ContractDto updatedContract = contractService.updateContract(id, contractDto);
        return ResponseEntity.ok(updatedContract);
    }

    @Operation(summary = "Get sorted contracts", description = "Returns a paginated and sorted list of contracts")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved sorted contracts")
    @ApiResponse(responseCode = "404", description = "No contracts found")
    @GetMapping("/sort")
    public ResponseEntity<?> getSortedContracts(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<ContractDto> sortedContracts = contractService.getSortedContracts(sortBy, order, pageable);
        if (sortedContracts.isEmpty()) {
            return new ResponseEntity<>("No contracts found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedContracts, HttpStatus.OK);
    }

    @Operation(summary = "Get filtered contracts", description = "Returns a paginated list of contracts filtered by provided parameters")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered contracts")
    @ApiResponse(responseCode = "404", description = "No contracts found")
    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredContracts(@RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate,
                                                  @RequestParam(required = false) BigDecimal minSalary, @RequestParam(required = false) BigDecimal maxSalary,
                                                  @PageableDefault Pageable pageable) {
        Page<ContractDto> filteredContracts = contractService.getFilteredContracts(startDate, endDate, minSalary, maxSalary, pageable);
        if (filteredContracts.isEmpty()) {
            return new ResponseEntity<>("No contracts found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredContracts, HttpStatus.OK);
    }

    @Operation(summary = "Send contract as PDF", description = "Sends the contract as a PDF to the specified email address")
    @ApiResponse(responseCode = "200", description = "Contract sent successfully")
    @ApiResponse(responseCode = "400", description = "Failed to send contract")
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

    @Operation(summary = "Get time left until contract end", description = "Returns the remaining time until the contract ends")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved time left")
    @ApiResponse(responseCode = "404", description = "Contract not found")
    @GetMapping("/{id}/time-left")
    @Cacheable(value = "contracts", key = "#id")
    public ResponseEntity<ContractTimeLeftDto> getTimeLeftUntilContractEnd(@PathVariable Long id) {
        ContractTimeLeftDto timeLeft = contractService.getTimeLeftUntilContractEnd(id);
        return ResponseEntity.ok(timeLeft);
    }

    @Operation(summary = "Delete a contract", description = "Deletes the contract with the specified ID")
    @ApiResponse(responseCode = "200", description = "Contract deleted successfully")
    @ApiResponse(responseCode = "404", description = "Contract not found")
    @DeleteMapping("/{id}")
    @CacheEvict(value = "contracts", allEntries = true)
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
