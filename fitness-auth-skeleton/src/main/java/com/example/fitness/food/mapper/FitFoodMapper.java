package com.example.fitness.food.mapper;

import com.example.fitness.admin.food.dto.AdminFoodPageQuery;
import com.example.fitness.food.dto.FoodPageQuery;
import com.example.fitness.food.entity.FitFood;
import com.example.fitness.food.vo.FoodVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FitFoodMapper {

    FitFood selectById(@Param("id") Long id);

    FoodVO selectDetailById(@Param("id") Long id);

    long countPage(FoodPageQuery query);

    List<FoodVO> selectPage(FoodPageQuery query);

    long countAdminPage(AdminFoodPageQuery query);

    List<FoodVO> selectAdminPage(AdminFoodPageQuery query);

    int insert(FitFood fitFood);

    int updateById(FitFood fitFood);

    int updateStatusById(
            @Param("id") Long id,
            @Param("status") Integer status,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
