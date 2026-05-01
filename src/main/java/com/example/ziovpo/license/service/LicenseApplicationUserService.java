package com.example.ziovpo.license.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.ziovpo.model.Users;
import com.example.ziovpo.repository.UsersRepository;

@Service
public class LicenseApplicationUserService {

    private final UsersRepository usersRepository;

    public LicenseApplicationUserService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public Users getActiveUserOrFail(UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

        boolean active = user.isAccountNonExpired()
                && user.isAccountNonLocked()
                && user.isCredentialsNonExpired()
                && user.isEnabled();

        if (!active) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
        }

        return user;
    }
}