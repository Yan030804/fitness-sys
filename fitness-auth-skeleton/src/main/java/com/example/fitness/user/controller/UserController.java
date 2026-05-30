package com.example.fitness.user.controller;

import com.example.fitness.auth.vo.UserProfileVO;
import com.example.fitness.common.api.Result;
import com.example.fitness.user.dto.ChangePasswordRequest;
import com.example.fitness.user.dto.UpdateCurrentUserRequest;
import com.example.fitness.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserProfileVO> getCurrentUserProfile() {
        return Result.success(userService.getCurrentUserProfile());
    }

    @PutMapping("/me")
    public Result<UserProfileVO> updateCurrentUserProfile(@Valid @RequestBody UpdateCurrentUserRequest request) {
        return Result.success(userService.updateCurrentUserProfile(request));
    }

    @PutMapping("/me/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return Result.success(null);
    }
}
