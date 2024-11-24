package org.agency.course_work.service;

import lombok.RequiredArgsConstructor;
import org.agency.course_work.entity.User;
import org.agency.course_work.enums.Role;
import org.agency.course_work.exception.UserAlreadyExists;
import org.agency.course_work.exception.UserNotFound;
import org.agency.course_work.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;


    public User save(User user) {
        return repository.save(user);
    }


    public User create(User user) {
        if (repository.existsByUsername(user.getUsername())) {
            // Заменить на свои исключения
            throw new UserAlreadyExists("User with this username already exists");
        }

        if (repository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExists("User with this email already exists");
        }

        return save(user);
    }

    public User getByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UserNotFound("User with this username not found"));

    }

    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public User getCurrentUser() {
        // Получение имени пользователя из контекста Spring Security
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }


    @Deprecated
    public void getAdmin() {
        var user = getCurrentUser();
        user.setRole(Role.ROLE_ADMIN);
        save(user);
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public void deleteById(Long id) {
        repository.findById(id).ifPresentOrElse(
                repository::delete,
                () -> { throw new UserNotFound("User with this id not found"); }
        );
    }

}