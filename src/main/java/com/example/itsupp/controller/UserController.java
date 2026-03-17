package com.example.itsupp.controller;

import com.example.itsupp.model.Users;
import com.example.itsupp.repository.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Получить всех пользователей
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

    // Обновить данные пользователя
    @PutMapping("/{id}")
    public Users updateUser(@PathVariable Long id, @RequestBody Users updatedUser) {
        return usersRepository.findById(id)
                .map(user -> {
                    user.setUsername(updatedUser.getUsername());
                    user.setEmail(updatedUser.getEmail());
                    user.setDepartment(updatedUser.getDepartment());
                    return usersRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("Пользователь с таким id не найден: " + id));
    }

    // Удалить пользователя
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        usersRepository.deleteById(id);
    }

    // Получить пользователя по ID
    @GetMapping("/{id}")
    public Users getUserById(@PathVariable Long id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с таким id не найден: " + id));
    }
}
