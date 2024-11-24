package org.agency.course_work.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.agency.course_work.entity.User;
import org.agency.course_work.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Отримати список всіх користувачів", description = "Повертає список всіх зареєстрованих користувачів.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список користувачів успішно отримано"),
            @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера")
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити користувача за ID", description = "Видаляє користувача за його ID. Потрібно бути авторизованим адміністраторами.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Користувач успішно видалений"),
            @ApiResponse(responseCode = "404", description = "Користувач не знайдений"),
            @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUserById(@PathVariable Long id) {
        userService.deleteById(id);
    }
}