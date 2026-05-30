package com.example.fitness.recommendation.mapper;

import com.example.fitness.admin.recommendation.dto.AdminRecommendationPageQuery;
import com.example.fitness.exercise.entity.FitExercise;
import com.example.fitness.food.entity.FitFood;
import com.example.fitness.plan.vo.PlanVO;
import com.example.fitness.recommendation.dto.RecommendationHistoryQuery;
import com.example.fitness.recommendation.entity.RecommendationRecord;
import com.example.fitness.recommendation.vo.RecommendationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RecommendationRecordMapper {

    int insert(RecommendationRecord record);

    RecommendationRecord selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    RecommendationVO selectDetailByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    long countUserPage(@Param("userId") Long userId, @Param("query") RecommendationHistoryQuery query);

    List<RecommendationVO> selectUserPage(@Param("userId") Long userId, @Param("query") RecommendationHistoryQuery query);

    LocalDateTime selectLatestTime(@Param("userId") Long userId, @Param("recType") String recType);

    List<RecommendationVO> selectLatestList(
            @Param("userId") Long userId,
            @Param("recType") String recType,
            @Param("latestTime") LocalDateTime latestTime
    );

    long countAdminPage(@Param("query") AdminRecommendationPageQuery query, @Param("phone") String phone);

    List<RecommendationVO> selectAdminPage(@Param("query") AdminRecommendationPageQuery query, @Param("phone") String phone);

    List<FitExercise> selectActiveExercises();

    List<FitFood> selectActiveFoods();

    List<PlanVO> selectRecommendablePlansByUserId(@Param("userId") Long userId);

    List<String> selectRecentWorkoutCategories(@Param("userId") Long userId);

    List<String> selectRecentWorkoutBodyParts(@Param("userId") Long userId);

    List<String> selectRecentMealTypes(@Param("userId") Long userId);

    List<Long> selectRecentRecommendedTargetIds(
            @Param("userId") Long userId,
            @Param("recType") String recType,
            @Param("limit") Integer limit
    );
}
