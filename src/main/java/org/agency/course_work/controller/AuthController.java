package org.agency.course_work.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agency.course_work.dto.JwtAuthenticationResponse;
import org.agency.course_work.dto.SignInRequest;
import org.agency.course_work.dto.SignUpRequest;
import org.agency.course_work.service.AuthenticationService;
import org.agency.course_work.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {
    private final AuthenticationService authenticationService;
    private final UserService service;

    @Operation(summary = "User registration", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping("/sign-up")
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @Operation(summary = "User login", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }
}
