package com.example.fitness.workout.mapper;

import com.example.fitness.record.entity.UserWorkoutRecord;
import com.example.fitness.workout.dto.WorkoutRecordPageQuery;
import com.example.fitness.workout.vo.WorkoutRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserWorkoutRecordMapper {

    int insert(UserWorkoutRecord record);

    long countPage(@Param("userId") Long userId, @Param("query") WorkoutRecordPageQuery query);

    List<WorkoutRecordVO> selectPage(@Param("userId") Long userId, @Param("query") WorkoutRecordPageQuery query);

    WorkoutRecordVO selectDetailByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    UserWorkoutRecord selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<UserWorkoutRecord> selectAllForRecommendation();

    int updateByIdAndUserId(UserWorkoutRecord record);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
