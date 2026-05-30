package com.example.fitness.system.mapper;

import com.example.fitness.admin.user.dto.AdminUserPageQuery;
import com.example.fitness.auth.vo.UserProfileVO;
import com.example.fitness.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SysUserMapper {

    SysUser selectById(@Param("id") Long id);

    SysUser selectByUsername(@Param("username") String username);

    int existsByUsername(@Param("username") String username);

    int existsByPhone(@Param("phone") String phone);

    int existsByEmail(@Param("email") String email);

    int existsByPhoneExcludeId(@Param("phone") String phone, @Param("excludeId") Long excludeId);

    int existsByEmailExcludeId(@Param("email") String email, @Param("excludeId") Long excludeId);

    int insert(SysUser sysUser);

    int updateProfileById(SysUser sysUser);

    int updatePasswordById(
            @Param("id") Long id,
            @Param("passwordHash") String passwordHash,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    int updateStatusById(
            @Param("id") Long id,
            @Param("status") Integer status,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    UserProfileVO selectUserProfileById(@Param("id") Long id);

    long countAdminUserPage(AdminUserPageQuery query);

    List<UserProfileVO> selectAdminUserPage(AdminUserPageQuery query);
}
