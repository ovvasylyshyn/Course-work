package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.AgentCreationDto;
import org.agency.course_work.dto.AgentDto;
import org.agency.course_work.service.AgentService;
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

    @GetMapping("{id}")
    public ResponseEntity<AgentDto> getAgentById(@PathVariable("id")Long id) {
    return ResponseEntity.ok(agentService.getAgentById(id));
    }

    @PostMapping
    public ResponseEntity<AgentDto> createAgent(@Valid @RequestBody AgentCreationDto agentCreationtDto) {
        return new ResponseEntity(agentService.createAgent(agentCreationtDto), HttpStatus.CREATED);
    }

@GetMapping
public ResponseEntity<?> getAllAgents(@PageableDefault Pageable pageable) {
    Page<AgentDto> agentDtos = agentService.getAllAgents(pageable);
    if (agentDtos.isEmpty()) {
        return new ResponseEntity<>("No agents found.", HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(agentDtos, HttpStatus.OK);
}

    @PutMapping("/{id}")
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
        @RequestParam(required = false) CommissionRate commissionRate, @PageableDefault Pageable pageable) {
    Page<AgentDto> filteredAgents = agentService.getFilteredAgents(firstName, lastName, phoneNumber, commissionRate, pageable);
    if (filteredAgents.isEmpty()) {
        return new ResponseEntity<>("No agents found.", HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(filteredAgents, HttpStatus.OK);
}


}
