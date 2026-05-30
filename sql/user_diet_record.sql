/*
 Navicat Premium Data Transfer

 Source Server         : 1234
 Source Server Type    : MySQL
 Source Server Version : 80037
 Source Host           : localhost:3306
 Source Schema         : fitness_recommend_system

 Target Server Type    : MySQL
 Target Server Version : 80037
 File Encoding         : 65001

 Date: 12/04/2026 19:56:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user_diet_record
-- ----------------------------
DROP TABLE IF EXISTS `user_diet_record`;
CREATE TABLE `user_diet_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `food_id` bigint NOT NULL COMMENT '食材ID',
  `diet_date` date NOT NULL COMMENT '饮食日期',
  `meal_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '餐次：早餐、午餐、晚餐、加餐',
  `intake_grams` decimal(6, 2) NULL DEFAULT NULL COMMENT '摄入克数',
  `calories_intake` int NULL DEFAULT NULL COMMENT '摄入热量',
  `is_recommended` tinyint NOT NULL DEFAULT 0 COMMENT '是否来自系统推荐：1是，0否',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_diet_user_id`(`user_id`) USING BTREE,
  INDEX `idx_diet_food_id`(`food_id`) USING BTREE,
  INDEX `idx_diet_date`(`diet_date`) USING BTREE,
  CONSTRAINT `fk_diet_food` FOREIGN KEY (`food_id`) REFERENCES `fit_food` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_diet_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户饮食记录表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
