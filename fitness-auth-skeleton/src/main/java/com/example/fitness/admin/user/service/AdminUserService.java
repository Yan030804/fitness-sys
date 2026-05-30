package com.example.fitness.admin.user.service;

import com.example.fitness.admin.user.dto.AdminUserPageQuery;
import com.example.fitness.admin.user.dto.UpdateUserStatusRequest;
import com.example.fitness.auth.vo.UserProfileVO;
import com.example.fitness.common.model.PageResponse;

import java.util.Map;

public interface AdminUserService {

    PageResponse<UserProfileVO> pageUsers(AdminUserPageQuery query);

    UserProfileVO getUserDetail(Long id);

    Map<String, Object> updateUserStatus(Long id, UpdateUserStatusRequest request);
}
