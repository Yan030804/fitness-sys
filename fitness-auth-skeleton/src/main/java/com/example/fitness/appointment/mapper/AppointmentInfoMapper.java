package com.example.fitness.appointment.mapper;

import com.example.fitness.admin.appointment.dto.AdminAppointmentPageQuery;
import com.example.fitness.appointment.dto.AppointmentPageQuery;
import com.example.fitness.appointment.entity.AppointmentInfo;
import com.example.fitness.appointment.vo.AppointmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppointmentInfoMapper {

    int insert(AppointmentInfo appointmentInfo);

    long countUserPage(@Param("userId") Long userId, @Param("query") AppointmentPageQuery query);

    List<AppointmentVO> selectUserPage(@Param("userId") Long userId, @Param("query") AppointmentPageQuery query);

    AppointmentVO selectDetailByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    AppointmentInfo selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int updateByIdAndUserId(AppointmentInfo appointmentInfo);

    long countAdminPage(@Param("query") AdminAppointmentPageQuery query, @Param("phone") String phone);

    List<AppointmentVO> selectAdminPage(@Param("query") AdminAppointmentPageQuery query, @Param("phone") String phone);

    AppointmentVO selectDetailById(@Param("id") Long id);

    AppointmentInfo selectById(@Param("id") Long id);

    int updateStatusById(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("remark") String remark
    );
}
