package org.agency.course_work.service;

import lombok.RequiredArgsConstructor;
import org.agency.course_work.dto.JwtAuthenticationResponse;
import org.agency.course_work.dto.SignInRequest;
import org.agency.course_work.dto.SignUpRequest;
import org.agency.course_work.entity.User;
import org.agency.course_work.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${admin.system.password}")
    private String adminSystemPassword;

    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        Role role = adminSystemPassword.equals(request.getPassword())
                ? Role.ROLE_ADMIN
                : Role.ROLE_USER;

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userService.create(user);

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        var user = userService
                .userDetailsService()
                .loadUserByUsername(request.getUsername());

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }
}