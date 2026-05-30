package com.example.fitness.security.service;

import com.example.fitness.security.model.LoginUser;
import com.example.fitness.system.entity.SysRole;
import com.example.fitness.system.entity.SysUser;
import com.example.fitness.system.mapper.SysRoleMapper;
import com.example.fitness.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserMapper.selectByUsername(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException("user not found");
        }
        if (sysUser.getStatus() == null || sysUser.getStatus() != 1) {
            throw new DisabledException("user is disabled");
        }

        SysRole sysRole = sysRoleMapper.selectById(sysUser.getRoleId());
        if (sysRole == null || sysRole.getStatus() == null || sysRole.getStatus() != 1) {
            throw new UsernameNotFoundException("role not found or disabled");
        }

        return LoginUser.builder()
                .userId(sysUser.getId())
                .username(sysUser.getUsername())
                .password(sysUser.getPasswordHash())
                .status(sysUser.getStatus())
                .roleCode(sysRole.getRoleCode())
                .build();
    }
}
