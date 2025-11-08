package com.stas.user_service.service;

import java.util.List;

import com.stas.user_service.dto.UserChangePasswordDto;
import com.stas.user_service.dto.UserLoginDto;
import com.stas.user_service.dto.UserSignupDto;
import com.stas.user_service.dto.UserUpdateDto;
import com.stas.user_service.entity.User;

public interface UserService {

    //  Registration
    User signup(UserSignupDto dto);

    //  Login + return JWT
    String login(UserLoginDto dto);

    //  Get all users (admin)
    List<User> getAllUsers();

    //  Get specific user by email (for logged-in user)
    User getUserByEmail(String email);

    // Update profile
    User updateDetails(String email, UserUpdateDto dto);

    // Change password
    void changePassword(String email, UserChangePasswordDto dto);
}