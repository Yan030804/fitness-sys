package com.example.fitness.user.service;

import com.example.fitness.auth.vo.UserProfileVO;
import com.example.fitness.user.dto.ChangePasswordRequest;
import com.example.fitness.user.dto.UpdateCurrentUserRequest;

public interface UserService {

    UserProfileVO getCurrentUserProfile();

    UserProfileVO updateCurrentUserProfile(UpdateCurrentUserRequest request);

    void changePassword(ChangePasswordRequest request);
}
