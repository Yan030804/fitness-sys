package com.example.fitness.admin.user.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AdminUserPageQuery {

    @Min(value = 1, message = "pageNum must be >= 1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "pageSize must be >= 1")
    private Integer pageSize = 10;

    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    private String roleCode;

    public int getOffset() {
        int currentPageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int currentPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        return (currentPageNum - 1) * currentPageSize;
    }
}
