package com.example.fitness.plan.mapper;

import com.example.fitness.plan.entity.FitnessPlanItem;
import com.example.fitness.plan.vo.PlanItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FitnessPlanItemMapper {

    int insert(FitnessPlanItem item);

    List<PlanItemVO> selectByPlanId(@Param("planId") Long planId);

    FitnessPlanItem selectById(@Param("id") Long id);

    PlanItemVO selectDetailById(@Param("id") Long id);

    int updateById(FitnessPlanItem item);

    int deleteById(@Param("id") Long id);

    int deleteByPlanId(@Param("planId") Long planId);
}
