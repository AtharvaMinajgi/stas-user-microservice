package com.stas.user_service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.stas.user_service.dto.UserChangePasswordDto;
import com.stas.user_service.dto.UserUpdateDto;
import com.stas.user_service.entity.User;
import com.stas.user_service.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Accessible only by ADMIN
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Accessible by logged-in user (ADMIN or USER)
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN','DEVELOPER','CLIENT')")
    public ResponseEntity<User> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    //  Update profile (self)
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN','DEVELOPER','CLIENT')")
    public ResponseEntity<User> updateProfile(@RequestBody UserUpdateDto dto, Authentication authentication) {
        String email = authentication.getName();
        User updated = userService.updateDetails(email, dto);
        return ResponseEntity.ok(updated);
    }

    // Change password
    @PutMapping("/me/change-password")
    @PreAuthorize("hasAnyRole('USER','ADMIN','DEVELOPER','CLIENT')")
    public ResponseEntity<?> changePassword(@RequestBody UserChangePasswordDto dto, Authentication authentication) {
        String email = authentication.getName();
        try {
            userService.changePassword(email, dto);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}