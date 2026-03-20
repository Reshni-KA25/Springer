package com.kanini.springer.service.User;

import com.kanini.springer.dto.Authentication.LoginResponse;

public interface IUserService {
    
    /**
     * Authenticate user with email and password
     * @param email User's email
     * @param password User's password
     * @return LoginResponse with user details and JWT token
     * @throws RuntimeException if authentication fails
     */
    LoginResponse authenticate(String email, String password);
}
