package com.example.fitness.exercise.mapper;

import com.example.fitness.admin.exercise.dto.AdminExercisePageQuery;
import com.example.fitness.exercise.dto.ExercisePageQuery;
import com.example.fitness.exercise.entity.FitExercise;
import com.example.fitness.exercise.vo.ExerciseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FitExerciseMapper {

    FitExercise selectById(@Param("id") Long id);

    ExerciseVO selectDetailById(@Param("id") Long id);

    long countPage(ExercisePageQuery query);

    List<ExerciseVO> selectPage(ExercisePageQuery query);

    long countAdminPage(AdminExercisePageQuery query);

    List<ExerciseVO> selectAdminPage(AdminExercisePageQuery query);

    int insert(FitExercise fitExercise);

    int updateById(FitExercise fitExercise);

    int updateStatusById(
            @Param("id") Long id,
            @Param("status") Integer status,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
