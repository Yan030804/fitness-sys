package com.example.fitness.plan.mapper;

import com.example.fitness.admin.plan.dto.AdminPlanPageQuery;
import com.example.fitness.plan.dto.PlanPageQuery;
import com.example.fitness.plan.entity.FitnessPlan;
import com.example.fitness.plan.vo.PlanVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FitnessPlanMapper {

    int insert(FitnessPlan plan);

    long countUserPage(@Param("userId") Long userId, @Param("query") PlanPageQuery query);

    List<PlanVO> selectUserPage(@Param("userId") Long userId, @Param("query") PlanPageQuery query);

    PlanVO selectDetailByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    FitnessPlan selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    FitnessPlan selectById(@Param("id") Long id);

    int updateByIdAndUserId(FitnessPlan plan);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    long countAdminPage(@Param("query") AdminPlanPageQuery query, @Param("phone") String phone);

    List<PlanVO> selectAdminPage(@Param("query") AdminPlanPageQuery query, @Param("phone") String phone);
}
