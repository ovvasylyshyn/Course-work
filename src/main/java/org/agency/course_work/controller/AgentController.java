package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.AgentCreationDto;
import org.agency.course_work.dto.AgentDto;
import org.agency.course_work.exception.AgentNotFound;
import org.agency.course_work.service.AgentService;
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
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/agents")
@AllArgsConstructor
public class AgentController {
    private final AgentService agentService;
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

    @GetMapping("{id}")
    @Cacheable(value = "agents", key = "#id")
    public ResponseEntity<AgentDto> getAgentById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(agentService.getAgentById(id));
    }

    @PostMapping
    @CacheEvict(value = "agents", allEntries = true)
    public ResponseEntity<AgentDto> createAgent(@Valid @RequestBody AgentCreationDto agentCreationtDto) {
        return new ResponseEntity(agentService.createAgent(agentCreationtDto), HttpStatus.CREATED);
    }

    @GetMapping
    @Cacheable(value = "agents")
    public ResponseEntity<?> getAllAgents(@PageableDefault Pageable pageable) {
        Page<AgentDto> agentDtos = agentService.getAllAgents(pageable);
        if (agentDtos.isEmpty()) {
            return new ResponseEntity<>("No agents found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(agentDtos, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @CacheEvict(value = "agents", allEntries = true)
    public ResponseEntity<AgentDto> updateAgent(@PathVariable Long id, @RequestBody @Valid AgentDto agentDto) {
        AgentDto updatedAgent = agentService.updateAgent(id, agentDto);
        return ResponseEntity.ok(updatedAgent);
    }

    @GetMapping("/sort")
    public ResponseEntity<?> getSortedAgents(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<AgentDto> sortedAgents = agentService.getSortedAgents(sortBy, order, pageable);
        if (sortedAgents.isEmpty()) {
            return new ResponseEntity<>("No agents found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedAgents, HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredAgents(@RequestParam(required = false) String firstName, @RequestParam(required = false) String lastName, @RequestParam(required = false) String phoneNumber,
                                               @RequestParam(required = false) CommissionRate commissionRate,@RequestParam(required = false) Boolean isDeleted,  @PageableDefault Pageable pageable) {
        Page<AgentDto> filteredAgents = agentService.getFilteredAgents(firstName, lastName, phoneNumber, commissionRate,isDeleted, pageable);
        if (filteredAgents.isEmpty()) {
            return new ResponseEntity<>("No agents found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredAgents, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
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

}

