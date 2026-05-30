package com.example.fitness.system.mapper;

import com.example.fitness.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysRoleMapper {

    SysRole selectById(@Param("id") Long id);

    SysRole selectByRoleCode(@Param("roleCode") String roleCode);
}
