-- AI智能取名小程序数据库初始化脚本
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS ai_naming CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_naming;

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` varchar(64) DEFAULT NULL COMMENT '微信openid',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `free_count` int DEFAULT '3' COMMENT '免费取名次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 取名记录表
CREATE TABLE IF NOT EXISTS `naming_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `father_surname` varchar(16) DEFAULT NULL COMMENT '父姓',
  `mother_surname` varchar(16) DEFAULT NULL COMMENT '母姓',
  `gender` varchar(8) DEFAULT NULL COMMENT '性别：boy/girl',
  `prompt` varchar(500) DEFAULT NULL COMMENT '家长期望描述',
  `style` varchar(32) DEFAULT NULL COMMENT '风格：modern/classic/literary/dignified',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='取名记录表';

-- 名字结果表
CREATE TABLE IF NOT EXISTS `name_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_id` bigint DEFAULT NULL COMMENT '关联的取名记录ID',
  `name` varchar(16) DEFAULT NULL COMMENT '名字',
  `pinyin` varchar(64) DEFAULT NULL COMMENT '拼音',
  `meaning` text COMMENT '寓意解析',
  `wuxing` varchar(64) DEFAULT NULL COMMENT '五行分析',
  `yinyun_score` int DEFAULT NULL COMMENT '音韵评分(1-10)',
  `times_score` int DEFAULT NULL COMMENT '时代感评分(1-10)',
  `total_score` int DEFAULT NULL COMMENT '综合评分(1-100)',
  `reason` text COMMENT '取名理由',
  `is_hot` tinyint DEFAULT '0' COMMENT '是否热门：0否 1是',
  `hot_index` int DEFAULT '0' COMMENT '热度排名',
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_is_hot` (`is_hot`, `hot_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='名字结果表';

-- 用户收藏表
CREATE TABLE IF NOT EXISTS `user_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `name_result_id` bigint DEFAULT NULL COMMENT '名字结果ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_name` (`user_id`, `name_result_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- 热度名字表（预置热门名字）
CREATE TABLE IF NOT EXISTS `hot_name` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(16) DEFAULT NULL COMMENT '名字',
  `gender` varchar(8) DEFAULT NULL COMMENT '性别',
  `meaning` varchar(255) DEFAULT NULL COMMENT '寓意',
  `search_count` int DEFAULT '0' COMMENT '搜索次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name_gender` (`name`, `gender`),
  KEY `idx_search_count` (`search_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热度名字表';

-- 插入一些示例热门名字（男宝）
INSERT INTO `hot_name` (`name`, `gender`, `meaning`, `search_count`) VALUES
('浩然', 'boy', '正大刚直，气度不凡', 9856),
('子轩', 'boy', '温文尔雅，气宇轩昂', 9234),
('宇航', 'boy', '胸怀广阔，志存高远', 8912),
('泽楷', 'boy', '温润如玉，楷模典范', 8654),
('睿思', 'boy', '聪慧深思，才思敏捷', 8432),
('俊杰', 'boy', '英俊杰出，出类拔萃', 8210),
('博文', 'boy', '博学多才，文采飞扬', 7988),
('天佑', 'boy', '上天庇佑，平安顺遂', 7654),
('嘉懿', 'boy', '美好高尚，品德优良', 7432),
('煜城', 'boy', '光明磊落，坚如磐石', 7210)
ON DUPLICATE KEY UPDATE `search_count` = VALUES(`search_count`);

-- 插入一些示例热门名字（女宝）
INSERT INTO `hot_name` (`name`, `gender`, `meaning`, `search_count`) VALUES
('梓涵', 'girl', '涵养深厚，温婉动人', 9567),
('诗涵', 'girl', '诗情画意，内涵丰富', 9345),
('欣怡', 'girl', '欢欣愉悦，怡然自得', 9123),
('雨桐', 'girl', '清新脱俗，高洁雅致', 8891),
('梦瑶', 'girl', '美梦成真，瑶池仙子', 8679),
('思琪', 'girl', '心思细腻，如玉温润', 8457),
('若曦', 'girl', '如晨光般温暖明亮', 8235),
('语嫣', 'girl', '言语嫣然，巧笑倩兮', 8013),
('佳怡', 'girl', '佳人怡然，美好愉悦', 7791),
('婉儿', 'girl', '温婉可人，柔美的样子', 7569)
ON DUPLICATE KEY UPDATE `search_count` = VALUES(`search_count`);
