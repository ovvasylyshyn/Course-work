package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.AgentCreationDto;
import org.agency.course_work.dto.AgentDto;
import org.agency.course_work.service.AgentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.agency.course_work.enums.CommissionRate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/agent")
@AllArgsConstructor
public class AgentController {
    private final AgentService agentService;

    @GetMapping("{id}")
    public ResponseEntity<AgentDto> getAgentById(@PathVariable("id")Long id) {
    return ResponseEntity.ok(agentService.getAgentById(id));
    }

    @PostMapping
    public ResponseEntity<AgentDto> createAgent(@Valid @RequestBody AgentCreationDto agentCreationtDto) {
        return new ResponseEntity(agentService.createAgent(agentCreationtDto), HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<AgentDto>> getAllAgents() {
        return ResponseEntity.ok(agentService.getAllAgents());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgentDto> updateAgent(@PathVariable Long id, @RequestBody @Valid AgentDto agentDto) {
        AgentDto updatedAgent = agentService.updateAgent(id, agentDto);
        return ResponseEntity.ok(updatedAgent);
    }

    @GetMapping("/sort")
    public ResponseEntity<List<AgentDto>> getSortedAgents(@RequestParam String sortBy, @RequestParam(defaultValue = "asc") String order) {
        try {
            List<AgentDto> sortedAgents = agentService.getSortedAgents(sortBy, order);
            return ResponseEntity.ok(sortedAgents);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<Object> getFilteredAgents(@RequestParam(required = false) String firstName, @RequestParam(required = false) String lastName,
                                                    @RequestParam(required = false) String phoneNumber, @RequestParam(required = false) CommissionRate commissionRate) {
        List<AgentDto> filteredAgents = agentService.getFilteredAgents(firstName, lastName, phoneNumber, commissionRate);
        if (filteredAgents.isEmpty()) {
            return new ResponseEntity<>("No agents found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredAgents, HttpStatus.OK);
    }

}
