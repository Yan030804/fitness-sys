package com.example.fitness.admin.user.controller;

import com.example.fitness.admin.user.dto.AdminUserPageQuery;
import com.example.fitness.admin.user.dto.UpdateUserStatusRequest;
import com.example.fitness.admin.user.service.AdminUserService;
import com.example.fitness.auth.vo.UserProfileVO;
import com.example.fitness.common.api.Result;
import com.example.fitness.common.model.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Result<PageResponse<UserProfileVO>> pageUsers(@Valid AdminUserPageQuery query) {
        return Result.success(adminUserService.pageUsers(query));
    }

    @GetMapping("/{id}")
    public Result<UserProfileVO> getUserDetail(@PathVariable Long id) {
        return Result.success(adminUserService.getUserDetail(id));
    }

    @PutMapping("/{id}/status")
    public Result<Map<String, Object>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return Result.success(adminUserService.updateUserStatus(id, request));
    }
}
