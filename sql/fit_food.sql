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

 Date: 12/04/2026 19:55:49
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for fit_food
-- ----------------------------
DROP TABLE IF EXISTS `fit_food`;
CREATE TABLE `fit_food`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `food_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '食材名称',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '食材分类',
  `calories_per_100g` decimal(6, 2) NULL DEFAULT NULL COMMENT '每100g热量',
  `protein_per_100g` decimal(6, 2) NULL DEFAULT NULL COMMENT '每100g蛋白质',
  `fat_per_100g` decimal(6, 2) NULL DEFAULT NULL COMMENT '每100g脂肪',
  `carb_per_100g` decimal(6, 2) NULL DEFAULT NULL COMMENT '每100g碳水',
  `suitable_goal` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '适用目标，如减脂、增肌',
  `suitable_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '适用时间，如早餐、训练后',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '食材说明',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_food_category`(`category`) USING BTREE,
  INDEX `idx_suitable_goal`(`suitable_goal`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '食材表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
