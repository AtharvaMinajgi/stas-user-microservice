package com.stas.user_service.serviceImpl;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.stas.user_service.dto.UserChangePasswordDto;
import com.stas.user_service.dto.UserLoginDto;
import com.stas.user_service.dto.UserSignupDto;
import com.stas.user_service.dto.UserUpdateDto;
import com.stas.user_service.entity.Role;
import com.stas.user_service.entity.User;
import com.stas.user_service.repository.RoleRepository;
import com.stas.user_service.repository.UserRepository;
import com.stas.user_service.service.UserService;
import com.stas.user_service.utils.JwtUtils;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    // ✅ Signup
    @Override
    public User signup(UserSignupDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists!");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Assign roles
        List<Role> roles = dto.getRoles() != null && !dto.getRoles().isEmpty()
                ? dto.getRoles().stream()
                        .map(roleName -> roleRepository.findByName(roleName.toUpperCase())
                                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                        .collect(Collectors.toList())
                : List.of(roleRepository.findByName("USER")
                        .orElseThrow(() -> new RuntimeException("Default role USER not found")));

        user.setRoles(new HashSet<>(roles));

        return userRepository.save(user);
    }

    //  Login (returns JWT)
    @Override
    public String login(UserLoginDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // Generate JWT with email + roles
        return jwtUtils.generateToken(user.getEmail(), roles);
    }

    //  Get all users (admin only)
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user profile by email
    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    // Update profile details
    @Override
    public User updateDetails(String email, UserUpdateDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            user.setEmail(dto.getEmail());
        }

        return userRepository.save(user);
    }

    //  Change password
    @Override
    public void changePassword(String email, UserChangePasswordDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    // For Spring Security context
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User appUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                appUser.getEmail(),
                appUser.getPassword(),
                appUser.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName())) // 🔥 Prefix "ROLE_"
                        .collect(Collectors.toList())
        );
    }
}