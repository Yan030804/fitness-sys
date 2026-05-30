package com.example.fitness.auth.service;

import com.example.fitness.auth.dto.LoginRequest;
import com.example.fitness.auth.dto.RegisterRequest;
import com.example.fitness.auth.vo.LoginVO;
import com.example.fitness.auth.vo.UserProfileVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface AuthService {

    Map<String, Object> register(RegisterRequest request);

    LoginVO login(LoginRequest request);

    void logout(HttpServletRequest request);

    UserProfileVO getCurrentUser();
}
