/*
 智能健身推荐系统数据库脚本（修订版）
 说明：
 1. 本版本移除了所有物理外键约束，统一采用逻辑外键。
 2. 所有关联关系通过字段 + 索引 + Service 层校验实现。
 3. 新增计划明细表 fitness_plan_item。
 4. 动作表与食材表使用 status 字段支持逻辑删除。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码，如ROLE_USER、ROLE_ADMIN',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `description` varchar(255) DEFAULT NULL COMMENT '角色说明',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password_hash` varchar(255) NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别',
  `age` int DEFAULT NULL COMMENT '年龄',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `height_cm` decimal(5,2) DEFAULT NULL COMMENT '身高(cm)',
  `weight_kg` decimal(5,2) DEFAULT NULL COMMENT '体重(kg)',
  `bmi` decimal(5,2) DEFAULT NULL COMMENT 'BMI',
  `fitness_goal` varchar(50) DEFAULT NULL COMMENT '健身目标，如减脂、增肌、塑形',
  `activity_level` varchar(50) DEFAULT NULL COMMENT '运动水平，如初级、中级、高级',
  `diet_preference` varchar(100) DEFAULT NULL COMMENT '饮食偏好',
  `role_id` bigint NOT NULL COMMENT '角色ID（逻辑外键：关联 sys_role.id）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ----------------------------
-- Table structure for fit_exercise
-- ----------------------------
DROP TABLE IF EXISTS `fit_exercise`;
CREATE TABLE `fit_exercise` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exercise_name` varchar(100) NOT NULL COMMENT '动作名称',
  `category` varchar(50) NOT NULL COMMENT '动作分类，如有氧、力量、拉伸',
  `body_part` varchar(50) DEFAULT NULL COMMENT '训练部位，如胸、背、腿',
  `difficulty` varchar(20) DEFAULT NULL COMMENT '难度等级',
  `equipment` varchar(100) DEFAULT NULL COMMENT '器械要求',
  `calories_per_hour` int DEFAULT NULL COMMENT '每小时消耗热量',
  `default_sets` int DEFAULT NULL COMMENT '默认组数',
  `default_reps` int DEFAULT NULL COMMENT '默认次数',
  `duration_min` int DEFAULT NULL COMMENT '建议时长(分钟)',
  `description` text COMMENT '动作说明',
  `caution` varchar(255) DEFAULT NULL COMMENT '注意事项',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用（逻辑删除）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_body_part` (`body_part`),
  KEY `idx_difficulty` (`difficulty`),
  KEY `idx_exercise_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健身动作表';

-- ----------------------------
-- Table structure for fit_food
-- ----------------------------
DROP TABLE IF EXISTS `fit_food`;
CREATE TABLE `fit_food` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `food_name` varchar(100) NOT NULL COMMENT '食材名称',
  `category` varchar(50) DEFAULT NULL COMMENT '食材分类',
  `calories_per_100g` decimal(6,2) DEFAULT NULL COMMENT '每100g热量',
  `protein_per_100g` decimal(6,2) DEFAULT NULL COMMENT '每100g蛋白质',
  `fat_per_100g` decimal(6,2) DEFAULT NULL COMMENT '每100g脂肪',
  `carb_per_100g` decimal(6,2) DEFAULT NULL COMMENT '每100g碳水',
  `suitable_goal` varchar(50) DEFAULT NULL COMMENT '适用目标，如减脂、增肌',
  `suitable_time` varchar(50) DEFAULT NULL COMMENT '适用时间，如早餐、训练后',
  `description` varchar(255) DEFAULT NULL COMMENT '食材说明',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用（逻辑删除）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_food_category` (`category`),
  KEY `idx_suitable_goal` (`suitable_goal`),
  KEY `idx_food_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食材表';

-- ----------------------------
-- Table structure for appointment_info
-- ----------------------------
DROP TABLE IF EXISTS `appointment_info`;
CREATE TABLE `appointment_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（逻辑外键：关联 sys_user.id）',
  `name` varchar(50) NOT NULL COMMENT '预约人姓名',
  `phone` varchar(20) NOT NULL COMMENT '联系电话',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别',
  `age` int DEFAULT NULL COMMENT '年龄',
  `appointment_type` varchar(50) NOT NULL COMMENT '预约类型，如健身咨询、营养咨询',
  `reserve_date` date NOT NULL COMMENT '预约日期',
  `reserve_time` time NOT NULL COMMENT '预约时间',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING、CONFIRMED、CANCELLED、DONE',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_appointment_user_id` (`user_id`),
  KEY `idx_appointment_date` (`reserve_date`),
  KEY `idx_appointment_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约信息表';

-- ----------------------------
-- Table structure for fitness_plan
-- ----------------------------
DROP TABLE IF EXISTS `fitness_plan`;
CREATE TABLE `fitness_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（逻辑外键：关联 sys_user.id）',
  `plan_type` varchar(20) NOT NULL COMMENT '计划类型：TRAINING、DIET',
  `plan_name` varchar(100) NOT NULL COMMENT '计划名称',
  `cycle_type` varchar(20) NOT NULL COMMENT '周期类型：DAILY、WEEKLY',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `target_goal` varchar(50) DEFAULT NULL COMMENT '目标，如减脂、增肌',
  `source_type` varchar(20) DEFAULT NULL COMMENT '来源：SYSTEM、USER_ADJUST',
  `status` varchar(20) DEFAULT NULL COMMENT '状态：进行中、已完成、已取消',
  `notes` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_plan_user_id` (`user_id`),
  KEY `idx_plan_type` (`plan_type`),
  KEY `idx_plan_date` (`start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划主表';

-- ----------------------------
-- Table structure for fitness_plan_item
-- ----------------------------
DROP TABLE IF EXISTS `fitness_plan_item`;
CREATE TABLE `fitness_plan_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_id` bigint NOT NULL COMMENT '计划ID（逻辑外键：关联 fitness_plan.id）',
  `item_type` varchar(20) NOT NULL COMMENT '明细类型：EXERCISE、FOOD',
  `target_id` bigint NOT NULL COMMENT '目标对象ID（逻辑外键：关联 fit_exercise.id 或 fit_food.id）',
  `target_name` varchar(100) DEFAULT NULL COMMENT '目标对象名称，创建时冗余保存',
  `item_date` date DEFAULT NULL COMMENT '执行日期',
  `item_time` time DEFAULT NULL COMMENT '执行时间',
  `sets_count` int DEFAULT NULL COMMENT '训练组数',
  `reps_count` int DEFAULT NULL COMMENT '训练次数',
  `duration_min` int DEFAULT NULL COMMENT '训练时长（分钟）',
  `intake_grams` decimal(6,2) DEFAULT NULL COMMENT '摄入克数',
  `calories` int DEFAULT NULL COMMENT '参考热量/消耗热量',
  `completion_status` varchar(20) DEFAULT NULL COMMENT '完成状态：PENDING、DONE、SKIPPED',
  `sort_no` int DEFAULT NULL COMMENT '排序号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_plan_item_plan_id` (`plan_id`),
  KEY `idx_plan_item_type` (`item_type`),
  KEY `idx_plan_item_target_id` (`target_id`),
  KEY `idx_plan_item_date` (`item_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划明细表';

-- ----------------------------
-- Table structure for recommendation_record
-- ----------------------------
DROP TABLE IF EXISTS `recommendation_record`;
CREATE TABLE `recommendation_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（逻辑外键：关联 sys_user.id）',
  `rec_type` varchar(20) NOT NULL COMMENT '推荐类型：EXERCISE、FOOD、PLAN',
  `target_id` bigint NOT NULL COMMENT '推荐对象ID',
  `target_name` varchar(100) NOT NULL COMMENT '推荐对象名称',
  `algorithm_type` varchar(50) DEFAULT NULL COMMENT '算法类型，如RULE_BASED、CONTENT_MATCH',
  `score` decimal(5,2) DEFAULT NULL COMMENT '推荐得分',
  `reason` varchar(255) DEFAULT NULL COMMENT '推荐原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_rec_user_id` (`user_id`),
  KEY `idx_rec_type` (`rec_type`),
  KEY `idx_rec_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐记录表';

-- ----------------------------
-- Table structure for user_workout_record
-- ----------------------------
DROP TABLE IF EXISTS `user_workout_record`;
CREATE TABLE `user_workout_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（逻辑外键：关联 sys_user.id）',
  `exercise_id` bigint NOT NULL COMMENT '动作ID（逻辑外键：关联 fit_exercise.id）',
  `workout_date` date NOT NULL COMMENT '训练日期',
  `duration_min` int DEFAULT NULL COMMENT '训练时长(分钟)',
  `sets_count` int DEFAULT NULL COMMENT '完成组数',
  `reps_count` int DEFAULT NULL COMMENT '完成次数',
  `calories_burned` int DEFAULT NULL COMMENT '消耗热量',
  `completion_status` varchar(20) DEFAULT NULL COMMENT '完成状态，如已完成、未完成',
  `feedback_score` int DEFAULT NULL COMMENT '用户评分，1-5',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_workout_user_id` (`user_id`),
  KEY `idx_workout_exercise_id` (`exercise_id`),
  KEY `idx_workout_date` (`workout_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户训练记录表';

-- ----------------------------
-- Table structure for user_diet_record
-- ----------------------------
DROP TABLE IF EXISTS `user_diet_record`;
CREATE TABLE `user_diet_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（逻辑外键：关联 sys_user.id）',
  `food_id` bigint NOT NULL COMMENT '食材ID（逻辑外键：关联 fit_food.id）',
  `diet_date` date NOT NULL COMMENT '饮食日期',
  `meal_type` varchar(20) DEFAULT NULL COMMENT '餐次：早餐、午餐、晚餐、加餐',
  `intake_grams` decimal(6,2) DEFAULT NULL COMMENT '摄入克数',
  `calories_intake` int DEFAULT NULL COMMENT '摄入热量',
  `is_recommended` tinyint NOT NULL DEFAULT 0 COMMENT '是否来自系统推荐：1是，0否',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_diet_user_id` (`user_id`),
  KEY `idx_diet_food_id` (`food_id`),
  KEY `idx_diet_date` (`diet_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户饮食记录表';

-- ----------------------------
-- 逻辑外键实现建议（非建表约束）
-- ----------------------------
-- 1. 新增/修改 user_workout_record 时：先校验 sys_user.id、fit_exercise.id 是否存在且动作 status=1。
-- 2. 新增/修改 user_diet_record 时：先校验 sys_user.id、fit_food.id 是否存在且食材 status=1。
-- 3. 新增/修改 appointment_info 时：先校验 sys_user.id 是否存在且状态正常。
-- 4. 新增/修改 fitness_plan 时：先校验 sys_user.id 是否存在。
-- 5. 新增/修改 fitness_plan_item 时：先校验 fitness_plan.id 存在；再按 item_type 校验 fit_exercise 或 fit_food 是否存在且可用。
-- 6. 删除 fit_exercise / fit_food 时，不做物理删除，统一执行 status=0 的逻辑删除。
-- 7. 删除 fitness_plan 时，应在事务中先删 fitness_plan_item，再删 fitness_plan。

SET FOREIGN_KEY_CHECKS = 1;
