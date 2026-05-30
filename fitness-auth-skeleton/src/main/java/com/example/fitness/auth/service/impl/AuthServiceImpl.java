package com.example.fitness.auth.service.impl;

import com.example.fitness.auth.dto.LoginRequest;
import com.example.fitness.auth.dto.RegisterRequest;
import com.example.fitness.auth.service.AuthService;
import com.example.fitness.auth.vo.LoginVO;
import com.example.fitness.auth.vo.UserProfileVO;
import com.example.fitness.common.constants.RoleConstants;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.utils.JwtUtils;
import com.example.fitness.security.model.LoginUser;
import com.example.fitness.system.entity.SysRole;
import com.example.fitness.system.entity.SysUser;
import com.example.fitness.system.mapper.SysRoleMapper;
import com.example.fitness.system.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> register(RegisterRequest request) {
        if (sysUserMapper.existsByUsername(request.getUsername()) > 0) {
            throw new BusinessException(409, "username already exists");
        }
        if (StringUtils.hasText(request.getPhone()) && sysUserMapper.existsByPhone(request.getPhone()) > 0) {
            throw new BusinessException(409, "phone already exists");
        }
        if (StringUtils.hasText(request.getEmail()) && sysUserMapper.existsByEmail(request.getEmail()) > 0) {
            throw new BusinessException(409, "email already exists");
        }

        SysRole defaultRole = sysRoleMapper.selectByRoleCode(RoleConstants.ROLE_USER);
        if (defaultRole == null || defaultRole.getStatus() == null || defaultRole.getStatus() != 1) {
            throw new BusinessException(500, "default role not found or disabled");
        }

        SysUser sysUser = new SysUser();
        sysUser.setUsername(request.getUsername());
        sysUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        sysUser.setRealName(request.getRealName());
        sysUser.setNickname(request.getNickname());
        sysUser.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone() : null);
        sysUser.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail() : null);
        sysUser.setRoleId(defaultRole.getId());
        sysUser.setStatus(1);
        sysUser.setCreatedAt(LocalDateTime.now());
        sysUser.setUpdatedAt(LocalDateTime.now());

        sysUserMapper.insert(sysUser);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", sysUser.getId());
        data.put("username", sysUser.getUsername());
        return data;
    }

    @Override
    public LoginVO login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            SysRole sysRole = sysRoleMapper.selectByRoleCode(loginUser.getRoleCode());
            String token = jwtUtils.generateToken(loginUser.getUserId(), loginUser.getUsername(), loginUser.getRoleCode());
            return LoginVO.builder()
                    .token(token)
                    .userId(loginUser.getUserId())
                    .username(loginUser.getUsername())
                    .roleCode(loginUser.getRoleCode())
                    .roleName(sysRole != null ? sysRole.getRoleName() : null)
                    .build();
        } catch (BadCredentialsException ex) {
            throw new BusinessException(401, "username or password is incorrect");
        } catch (DisabledException ex) {
            throw new BusinessException(403, "user is disabled");
        }
    }

    @Override
    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
    }

    @Override
    public UserProfileVO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException(401, "unauthorized");
        }
        UserProfileVO profile = sysUserMapper.selectUserProfileById(loginUser.getUserId());
        if (profile == null) {
            throw new BusinessException(404, "user not found");
        }
        return profile;
    }
}
