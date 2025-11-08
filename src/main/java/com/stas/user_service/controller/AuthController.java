package com.stas.user_service.controller;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.stas.user_service.entity.Role;
import com.stas.user_service.entity.User;
import com.stas.user_service.repository.RoleRepository;
import com.stas.user_service.repository.UserRepository;
import com.stas.user_service.utils.JwtUtils;

import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/signup")
    public User registerUser(@RequestBody User userRequest) {

        // Encode password
        userRequest.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        // For each role coming in the request, find it from DB
        Set<Role> persistedRoles = new HashSet<>();
        for (Role role : userRequest.getRoles()) {
            Role existingRole = roleRepository.findByName(role.getName())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + role.getName()));
            persistedRoles.add(existingRole);
        }
        userRequest.setRoles(persistedRoles);

        // Save user
        return userRepository.save(userRequest);
    }
    
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        //  Extract user roles
        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName())
                .toList();

        // Generate token with roles
        String token = jwtUtils.generateToken(email, roles);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;
    }
}
