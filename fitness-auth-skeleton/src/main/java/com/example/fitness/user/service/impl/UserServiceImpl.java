package com.example.fitness.user.service.impl;

import com.example.fitness.auth.vo.UserProfileVO;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.utils.SecurityUtils;
import com.example.fitness.system.entity.SysUser;
import com.example.fitness.system.mapper.SysUserMapper;
import com.example.fitness.user.dto.ChangePasswordRequest;
import com.example.fitness.user.dto.UpdateCurrentUserRequest;
import com.example.fitness.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileVO getCurrentUserProfile() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        UserProfileVO profile = sysUserMapper.selectUserProfileById(currentUserId);
        if (profile == null) {
            throw new BusinessException(404, "user not found");
        }
        return profile;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileVO updateCurrentUserProfile(UpdateCurrentUserRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        SysUser existing = checkUserExists(currentUserId);

        String resolvedPhone = resolveStringField(request.getPhone(), existing.getPhone());
        String resolvedEmail = resolveStringField(request.getEmail(), existing.getEmail());

        if (StringUtils.hasText(resolvedPhone) && sysUserMapper.existsByPhoneExcludeId(resolvedPhone, currentUserId) > 0) {
            throw new BusinessException(409, "phone already exists");
        }
        if (StringUtils.hasText(resolvedEmail) && sysUserMapper.existsByEmailExcludeId(resolvedEmail, currentUserId) > 0) {
            throw new BusinessException(409, "email already exists");
        }

        SysUser toUpdate = new SysUser();
        toUpdate.setId(currentUserId);
        toUpdate.setRealName(resolveStringField(request.getRealName(), existing.getRealName()));
        toUpdate.setNickname(resolveStringField(request.getNickname(), existing.getNickname()));
        toUpdate.setGender(resolveStringField(request.getGender(), existing.getGender()));
        toUpdate.setAge(request.getAge() != null ? request.getAge() : existing.getAge());
        toUpdate.setPhone(resolvedPhone);
        toUpdate.setEmail(resolvedEmail);
        toUpdate.setHeightCm(request.getHeightCm() != null ? request.getHeightCm() : existing.getHeightCm());
        toUpdate.setWeightKg(request.getWeightKg() != null ? request.getWeightKg() : existing.getWeightKg());
        toUpdate.setBmi(request.getBmi() != null ? request.getBmi() : existing.getBmi());
        toUpdate.setFitnessGoal(resolveStringField(request.getFitnessGoal(), existing.getFitnessGoal()));
        toUpdate.setActivityLevel(resolveStringField(request.getActivityLevel(), existing.getActivityLevel()));
        toUpdate.setDietPreference(resolveStringField(request.getDietPreference(), existing.getDietPreference()));
        toUpdate.setUpdatedAt(LocalDateTime.now());

        int updated = sysUserMapper.updateProfileById(toUpdate);
        if (updated <= 0) {
            throw new BusinessException(500, "update profile failed");
        }
        return getCurrentUserProfile();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        SysUser existing = checkUserExists(currentUserId);

        if (!passwordEncoder.matches(request.getOldPassword(), existing.getPasswordHash())) {
            throw new BusinessException(400, "old password is incorrect");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException(400, "new password cannot be the same as old password");
        }

        int updated = sysUserMapper.updatePasswordById(
                currentUserId,
                passwordEncoder.encode(request.getNewPassword()),
                LocalDateTime.now()
        );
        if (updated <= 0) {
            throw new BusinessException(500, "change password failed");
        }
    }

    private SysUser checkUserExists(Long userId) {
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null) {
            throw new BusinessException(404, "user not found");
        }
        if (sysUser.getStatus() == null || sysUser.getStatus() != 1) {
            throw new BusinessException(403, "user is disabled");
        }
        return sysUser;
    }

    private String resolveStringField(String requestValue, String existingValue) {
        if (requestValue == null) {
            return existingValue;
        }
        return StringUtils.hasText(requestValue) ? requestValue.trim() : null;
    }
}
