package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.AgentCreationDto;
import org.agency.course_work.dto.AgentDto;
import org.agency.course_work.service.AgentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
