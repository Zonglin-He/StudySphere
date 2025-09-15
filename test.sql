/*
 Navicat Premium Dump SQL

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 90400 (9.4.0)
 Source Host           : localhost:3306
 Source Schema         : test

 Target Server Type    : MySQL
 Target Server Version : 90400 (9.4.0)
 File Encoding         : 65001

 Date: 15/09/2025 21:12:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for db_account
-- ----------------------------
DROP TABLE IF EXISTS `db_account`;
CREATE TABLE `db_account`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `register_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_name`(`username` ASC) USING BTREE,
  UNIQUE INDEX `unique_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of db_account
-- ----------------------------
INSERT INTO `db_account` VALUES (1, 'test', '$2a$10$e0e/FwX0GeWNVUvPaKXLzejoyRQxhDKyJu4ywJfAZrLew0aey77l6', '1449972863@qq.com', 'user', NULL, '2025-09-05 17:07:27');
INSERT INTO `db_account` VALUES (2, 'Licht', '$2a$10$PyB/JOhxKfalYNFCs1u5G.1z6V.7EG4.weU9oSYaZiJPR7/HWs.Ey', '2501103848@qq.com', 'user', NULL, '2025-09-06 14:53:13');
INSERT INTO `db_account` VALUES (3, 'HeHaiZhu', '$2a$10$6nAyTsyOw75vifVrM62enOT44IJFDcXbMSlbZzpTyf9DboY6RA./W', '1229358800@qq.com', 'user', NULL, '2025-09-06 17:56:13');

-- ----------------------------
-- Table structure for db_account_details
-- ----------------------------
DROP TABLE IF EXISTS `db_account_details`;
CREATE TABLE `db_account_details`  (
  `id` int NOT NULL,
  `gender` tinyint NULL DEFAULT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `qq` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `wx` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of db_account_details
-- ----------------------------

-- ----------------------------
-- Table structure for db_account_privacy
-- ----------------------------
DROP TABLE IF EXISTS `db_account_privacy`;
CREATE TABLE `db_account_privacy`  (
  `id` int NOT NULL,
  `phone` tinyint NULL DEFAULT NULL,
  `wx` tinyint NULL DEFAULT NULL,
  `qq` tinyint NULL DEFAULT NULL,
  `gender` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of db_account_privacy
-- ----------------------------
INSERT INTO `db_account_privacy` VALUES (2, 1, 1, 1, 1);
INSERT INTO `db_account_privacy` VALUES (3, 1, 1, 1, 1);

-- ----------------------------
-- Table structure for db_image_store
-- ----------------------------
DROP TABLE IF EXISTS `db_image_store`;
CREATE TABLE `db_image_store`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `uid` int NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of db_image_store
-- ----------------------------
INSERT INTO `db_image_store` VALUES (1, 2, '/cache20250915/6387280763964878a050f1a3178d516f', '2025-09-15 15:18:18');
INSERT INTO `db_image_store` VALUES (2, 2, '/cache20250915/dfd9696f57d34884b9c6cfaba1c8ef27', '2025-09-15 15:22:33');

-- ----------------------------
-- Table structure for db_notification
-- ----------------------------
DROP TABLE IF EXISTS `db_notification`;
CREATE TABLE `db_notification`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `uid` int NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of db_notification
-- ----------------------------

-- ----------------------------
-- Table structure for db_topic
-- ----------------------------
DROP TABLE IF EXISTS `db_topic`;
CREATE TABLE `db_topic`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `uid` int NULL DEFAULT NULL,
  `type` int NULL DEFAULT NULL,
  `time` datetime NULL DEFAULT NULL,
  `top` tinyint(1) NOT NULL DEFAULT 0 COMMENT '置顶',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of db_topic
-- ----------------------------
INSERT INTO `db_topic` VALUES (1, '一只何海猪', '{\"ops\":[{\"insert\":\"\\n\"}]}', NULL, 1, '2025-09-14 21:48:28', 0);
INSERT INTO `db_topic` VALUES (2, '111', '{\"ops\":[{\"insert\":\"111\\n\"}]}', NULL, 1, '2025-09-14 21:59:37', 0);
INSERT INTO `db_topic` VALUES (3, '111', '{\"ops\":[{\"insert\":\"1111\\n\"}]}', NULL, 1, '2025-09-14 22:06:21', 0);
INSERT INTO `db_topic` VALUES (4, '我是送一番', '{\"ops\":[{\"insert\":\"我是傻狗\\n\"}]}', NULL, 1, '2025-09-15 12:49:14', 0);
INSERT INTO `db_topic` VALUES (5, '我是大二ACCT何海猪', '{\"ops\":[{\"insert\":{\"image\":\"http://localhost:8080/images/cache20250915/dfd9696f57d34884b9c6cfaba1c8ef27\"}},{\"insert\":\"我的肌肉很大\\n\"}]}', NULL, 2, '2025-09-15 15:22:48', 0);

-- ----------------------------
-- Table structure for db_topic_comment
-- ----------------------------
DROP TABLE IF EXISTS `db_topic_comment`;
CREATE TABLE `db_topic_comment`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `uid` int NULL DEFAULT NULL,
  `tid` int NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `time` datetime NULL DEFAULT NULL,
  `quote` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of db_topic_comment
-- ----------------------------

-- ----------------------------
-- Table structure for db_topic_interact_collect
-- ----------------------------
DROP TABLE IF EXISTS `db_topic_interact_collect`;
CREATE TABLE `db_topic_interact_collect`  (
  `tid` int NOT NULL,
  `uid` int NULL DEFAULT NULL,
  `time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`tid`) USING BTREE,
  UNIQUE INDEX `tid_uid_collect`(`tid` ASC, `uid` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of db_topic_interact_collect
-- ----------------------------

-- ----------------------------
-- Table structure for db_topic_interact_like
-- ----------------------------
DROP TABLE IF EXISTS `db_topic_interact_like`;
CREATE TABLE `db_topic_interact_like`  (
  `tid` int NOT NULL,
  `uid` int NULL DEFAULT NULL,
  `time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`tid`) USING BTREE,
  UNIQUE INDEX `tid_uid_like`(`tid` ASC, `uid` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of db_topic_interact_like
-- ----------------------------

-- ----------------------------
-- Table structure for db_topic_type
-- ----------------------------
DROP TABLE IF EXISTS `db_topic_type`;
CREATE TABLE `db_topic_type`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `color` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of db_topic_type
-- ----------------------------
INSERT INTO `db_topic_type` VALUES (1, 'Daily chat', 'Share your daily life here', '#1E90FF');
INSERT INTO `db_topic_type` VALUES (2, 'Make friends', 'Find friends in campus', '#CE1EFF');
INSERT INTO `db_topic_type` VALUES (3, 'Problem feedback', 'Point out problems in campus', '#E07373');
INSERT INTO `db_topic_type` VALUES (4, 'Relationships', 'Show your partners here', '#E0CE73');
INSERT INTO `db_topic_type` VALUES (5, 'Experience', 'Share some experience', '#3BB62A');

SET FOREIGN_KEY_CHECKS = 1;
