package com.example.ziovpo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ziovpo.model.Users;
import com.example.ziovpo.repository.UsersRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @GetMapping
    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }

    @PostMapping
    public Users createUser(@RequestBody Users user) {
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            throw new RuntimeException("Error: Пароль должен быть минимум 8 символов!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        String role = user.getRole();
        if (role == null || role.isEmpty()) {
            user.setRole("ROLE_USER");
        } else if (!role.equals("ROLE_USER") && !role.equals("ROLE_ADMIN")) {
            throw new RuntimeException("Недопустимая роль: " + role);
        }

        return usersRepository.save(user);
    }
    @PutMapping("/{id}")
    public Users updateUser(@PathVariable UUID id, @RequestBody Users updatedUser) {
        return usersRepository.findById(id)
                .map(user -> {
                    user.setUsername(updatedUser.getUsername());
                    user.setEmail(updatedUser.getEmail());
                    user.setDepartment(updatedUser.getDepartment());
                    return usersRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("Пользователь с таким id не найден: " + id));
    }
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        usersRepository.deleteById(id);
    }
    @GetMapping("/{id}")
    public Users getUserById(@PathVariable UUID id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с таким id не найден: " + id));
    }
}
