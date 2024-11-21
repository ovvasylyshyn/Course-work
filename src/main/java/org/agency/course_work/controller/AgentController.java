package org.agency.course_work.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.AgentCreationDto;
import org.agency.course_work.dto.AgentDto;
import org.agency.course_work.exception.AgentNotFound;
import org.agency.course_work.service.AgentService;
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
                                               @RequestParam(required = false) CommissionRate commissionRate, @PageableDefault Pageable pageable) {
        Page<AgentDto> filteredAgents = agentService.getFilteredAgents(firstName, lastName, phoneNumber, commissionRate, pageable);
        if (filteredAgents.isEmpty()) {
            return new ResponseEntity<>("No agents found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredAgents, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteAgentById(@PathVariable("id") Long id) {
        try {
            agentService.deleteAgentById(id);
            return ResponseEntity.noContent().build();
        } catch (AgentNotFound ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}

//@RestController
//@RequestMapping("/api/agents")
//@RequiredArgsConstructor
//@Tag(name = "Агенты", description = "Операции с агентами")
//public class AgentController {
//
//    private final AgentService agentService;
//
//    @GetMapping("/{id}")
//    @Operation(summary = "Получить агента по ID", description = "Доступен только авторизованным пользователям.")
//    @Cacheable(value = "agents", key = "#id")
//    public ResponseEntity<AgentDto> getAgentById(@PathVariable("id") Long id) {
//        return ResponseEntity.ok(agentService.getAgentById(id));
//    }
//
//    @PostMapping("/admin")
//    @Operation(summary = "Создать нового агента", description = "Доступен только пользователям с ролью ADMIN.")
//    @PreAuthorize("hasRole('ADMIN')")
//    @CacheEvict(value = "agents", allEntries = true)
//    public ResponseEntity<AgentDto> createAgent(@Valid @RequestBody AgentCreationDto agentCreationtDto) {
//        return new ResponseEntity<>(agentService.createAgent(agentCreationtDto), HttpStatus.CREATED);
//    }
//
//    @GetMapping
//    @Operation(summary = "Получить список всех агентов", description = "Доступен только авторизованным пользователям.")
//    @Cacheable(value = "agents")
//    public ResponseEntity<?> getAllAgents(@PageableDefault Pageable pageable) {
//        Page<AgentDto> agentDtos = agentService.getAllAgents(pageable);
//        if (agentDtos.isEmpty()) {
//            return new ResponseEntity<>("No agents found.", HttpStatus.NOT_FOUND);
//        }
//        return new ResponseEntity<>(agentDtos, HttpStatus.OK);
//    }
//
//    @PutMapping("/{id}")
//    @Operation(summary = "Обновить данные агента", description = "Доступен только пользователям с ролью ADMIN.")
//    @PreAuthorize("hasRole('ADMIN')")
//    @CacheEvict(value = "agents", allEntries = true)
//    public ResponseEntity<AgentDto> updateAgent(@PathVariable Long id, @RequestBody @Valid AgentDto agentDto) {
//        AgentDto updatedAgent = agentService.updateAgent(id, agentDto);
//        return ResponseEntity.ok(updatedAgent);
//    }
//
//    @GetMapping("/sort")
//    @Operation(summary = "Получить отсортированный список агентов", description = "Доступен только авторизованным пользователям.")
//    public ResponseEntity<?> getSortedAgents(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
//        Page<AgentDto> sortedAgents = agentService.getSortedAgents(sortBy, order, pageable);
//        if (sortedAgents.isEmpty()) {
//            return new ResponseEntity<>("No agents found.", HttpStatus.NOT_FOUND);
//        }
//        return new ResponseEntity<>(sortedAgents, HttpStatus.OK);
//    }
//
//    @GetMapping("/filter")
//    @Operation(summary = "Получить фильтрованный список агентов", description = "Доступен только авторизованным пользователям.")
//    public ResponseEntity<?> getFilteredAgents(
//            @RequestParam(required = false) String firstName,
//            @RequestParam(required = false) String lastName,
//            @RequestParam(required = false) String phoneNumber,
//            @RequestParam(required = false) CommissionRate commissionRate,
//            @PageableDefault Pageable pageable) {
//        Page<AgentDto> filteredAgents = agentService.getFilteredAgents(firstName, lastName, phoneNumber, commissionRate, pageable);
//        if (filteredAgents.isEmpty()) {
//            return new ResponseEntity<>("No agents found.", HttpStatus.NOT_FOUND);
//        }
//        return new ResponseEntity<>(filteredAgents, HttpStatus.OK);
//    }
//}

