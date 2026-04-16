CREATE TABLE IF NOT EXISTS `ai_result_cache` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `cache_key` varchar(64) NOT NULL COMMENT '请求参数哈希',
  `request_params` text COMMENT '请求参数JSON',
  `result_json` longtext COMMENT 'AI原始返回结果JSON',
  `hit_count` int(11) DEFAULT '0' COMMENT '缓存命中次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cache_key` (`cache_key`),
  KEY `idx_cache_key` (`cache_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI取名结果缓存表';
