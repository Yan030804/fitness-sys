package com.example.fitness.diet.mapper;

import com.example.fitness.diet.dto.DietRecordPageQuery;
import com.example.fitness.diet.vo.DietRecordVO;
import com.example.fitness.record.entity.UserDietRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserDietRecordMapper {

    int insert(UserDietRecord record);

    long countPage(@Param("userId") Long userId, @Param("query") DietRecordPageQuery query);

    List<DietRecordVO> selectPage(@Param("userId") Long userId, @Param("query") DietRecordPageQuery query);

    DietRecordVO selectDetailByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    UserDietRecord selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<UserDietRecord> selectAllForRecommendation();

    int updateByIdAndUserId(UserDietRecord record);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
