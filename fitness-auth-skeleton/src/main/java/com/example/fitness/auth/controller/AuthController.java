package com.example.fitness.auth.controller;

import com.example.fitness.auth.dto.LoginRequest;
import com.example.fitness.auth.dto.RegisterRequest;
import com.example.fitness.auth.service.AuthService;
import com.example.fitness.auth.vo.LoginVO;
import com.example.fitness.auth.vo.UserProfileVO;
import com.example.fitness.common.api.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return Result.success(null);
    }

    @GetMapping("/me")
    public Result<UserProfileVO> getCurrentUser() {
        return Result.success(authService.getCurrentUser());
    }
}
