package org.agency.course_work.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.AgentCreationDto;
import org.agency.course_work.dto.AgentDto;
import org.agency.course_work.exception.AgentNotFound;
import org.agency.course_work.service.AgentService;
import org.agency.course_work.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.agency.course_work.enums.CommissionRate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/agents")
@AllArgsConstructor

public class AgentController {
    private final AgentService agentService;
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);
    private final UserService service;

    @Operation(
            summary = "Get agent by ID",
            description = "Fetches agent details based on provided ID",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched agent",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AgentDto.class))),
                    @ApiResponse(responseCode = "404", description = "Agent not found")
            }
    )
    @GetMapping("{id}")
    @Cacheable(value = "agents", key = "#id")
    public ResponseEntity<AgentDto> getAgentById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(agentService.getAgentById(id));
    }

    @Operation(
            summary = "Create a new agent",
            description = "Creates a new agent and stores it in the database",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Agent created successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AgentDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid agent data")
            }
    )
    @PostMapping
    @CacheEvict(value = "agents", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgentDto> createAgent(@Valid @RequestBody AgentCreationDto agentCreationtDto) {
        return new ResponseEntity<>(agentService.createAgent(agentCreationtDto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get all agents",
            description = "Fetches all agents with pagination support",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched agents",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "No agents found")
            }
    )
    @GetMapping
    @Cacheable(value = "agents")
    public ResponseEntity<?> getAllAgents(@PageableDefault Pageable pageable) {
        Page<AgentDto> agentDtos = agentService.getAllAgents(pageable);
        if (agentDtos.isEmpty()) {
            return new ResponseEntity<>("No agents found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(agentDtos, HttpStatus.OK);
    }

    @Operation(
            summary = "Update agent details",
            description = "Updates an existing agent's details by their ID",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Agent updated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AgentDto.class))),
                    @ApiResponse(responseCode = "404", description = "Agent not found")
            }
    )
    @PutMapping("/{id}")
    @CacheEvict(value = "agents", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgentDto> updateAgent(@PathVariable Long id, @RequestBody @Valid AgentDto agentDto) {
        AgentDto updatedAgent = agentService.updateAgent(id, agentDto);
        return ResponseEntity.ok(updatedAgent);
    }

    @Operation(
            summary = "Get sorted agents",
            description = "Fetches all agents sorted by specified field and order",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched sorted agents",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "No agents found")
            }
    )
    @GetMapping("/sort")
    public ResponseEntity<?> getSortedAgents(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<AgentDto> sortedAgents = agentService.getSortedAgents(sortBy, order, pageable);
        if (sortedAgents.isEmpty()) {
            return new ResponseEntity<>("No agents found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedAgents, HttpStatus.OK);
    }

    @Operation(
            summary = "Get filtered agents",
            description = "Fetches agents based on specified filters",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched filtered agents",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "No agents found")
            }
    )
    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredAgents(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) CommissionRate commissionRate,
            @RequestParam(required = false) Boolean isDeleted,
            @PageableDefault Pageable pageable) {
        Page<AgentDto> filteredAgents = agentService.getFilteredAgents(firstName, lastName, phoneNumber, commissionRate, isDeleted, pageable);
        if (filteredAgents.isEmpty()) {
            return new ResponseEntity<>("No agents found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredAgents, HttpStatus.OK);
    }

    @Operation(
            summary = "Delete agent by ID",
            description = "Marks an agent as deleted by their ID",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Agent successfully marked as deleted"),
                    @ApiResponse(responseCode = "404", description = "Agent not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @DeleteMapping("/{id}")
    @CacheEvict(value = "agents", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteAgent(@PathVariable Long id) {
        logger.info("Received request to delete Agent with ID: {}", id);
        try {
            agentService.deleteAgentById(id);
            logger.info("Agent with ID: {} marked as deleted successfully", id);
            return ResponseEntity.ok("Agent with ID " + id + " marked as deleted successfully.");
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting Agent with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while deleting Agent with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

//    @GetMapping("/admin")
//    @Operation(summary = "Доступен только авторизованным пользователям с ролью ADMIN")
//    @PreAuthorize("hasRole('ADMIN')")
//    public String exampleAdmin() {
//        return "Hello, admin!";
//    }
//
//    @GetMapping("/get-admin")
//    @Operation(summary = "Получить роль ADMIN (для демонстрации)")
//    public void getAdmin() {
//        service.getAdmin();
//    }

}

