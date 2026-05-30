package com.example.fitness.admin.user.service.impl;

import com.example.fitness.admin.user.dto.AdminUserPageQuery;
import com.example.fitness.admin.user.dto.UpdateUserStatusRequest;
import com.example.fitness.admin.user.service.AdminUserService;
import com.example.fitness.auth.vo.UserProfileVO;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.common.utils.SecurityUtils;
import com.example.fitness.system.entity.SysUser;
import com.example.fitness.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final SysUserMapper sysUserMapper;

    @Override
    public PageResponse<UserProfileVO> pageUsers(AdminUserPageQuery query) {
        long total = sysUserMapper.countAdminUserPage(query);
        List<UserProfileVO> list = total == 0
                ? List.of()
                : sysUserMapper.selectAdminUserPage(query);
        return PageResponse.of(list, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    public UserProfileVO getUserDetail(Long id) {
        UserProfileVO userProfile = sysUserMapper.selectUserProfileById(id);
        if (userProfile == null) {
            throw new BusinessException(404, "user not found");
        }
        return userProfile;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateUserStatus(Long id, UpdateUserStatusRequest request) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "user not found");
        }
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.equals(id) && request.getStatus() == 0) {
            throw new BusinessException(409, "cannot disable current logged-in admin");
        }
        int updated = sysUserMapper.updateStatusById(id, request.getStatus(), LocalDateTime.now());
        if (updated <= 0) {
            throw new BusinessException(500, "update user status failed");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("status", request.getStatus());
        return data;
    }
}
