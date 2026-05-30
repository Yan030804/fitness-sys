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

 Date: 12/04/2026 19:56:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user_workout_record
-- ----------------------------
DROP TABLE IF EXISTS `user_workout_record`;
CREATE TABLE `user_workout_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `exercise_id` bigint NOT NULL COMMENT '动作ID',
  `workout_date` date NOT NULL COMMENT '训练日期',
  `duration_min` int NULL DEFAULT NULL COMMENT '训练时长(分钟)',
  `sets_count` int NULL DEFAULT NULL COMMENT '完成组数',
  `reps_count` int NULL DEFAULT NULL COMMENT '完成次数',
  `calories_burned` int NULL DEFAULT NULL COMMENT '消耗热量',
  `completion_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '完成状态，如已完成、未完成',
  `feedback_score` int NULL DEFAULT NULL COMMENT '用户评分，1-5',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_workout_user_id`(`user_id`) USING BTREE,
  INDEX `idx_workout_exercise_id`(`exercise_id`) USING BTREE,
  INDEX `idx_workout_date`(`workout_date`) USING BTREE,
  CONSTRAINT `fk_workout_exercise` FOREIGN KEY (`exercise_id`) REFERENCES `fit_exercise` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_workout_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户训练记录表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
