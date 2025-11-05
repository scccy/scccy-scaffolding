-- 修复 oauth2_registered_client 表的 del_flag 字段
-- 将 deleted 字段改为 del_flag，类型从 varchar(1) 改为 tinyint，值从 'N'/'Y' 改为 0/1

-- 1. 添加新字段 del_flag
ALTER TABLE oauth2_registered_client 
ADD COLUMN del_flag tinyint NOT NULL DEFAULT 0 COMMENT '是否已删除：0-未删除，1-已删除' 
AFTER token_settings;

-- 2. 迁移数据：将 deleted 字段的值转换为 del_flag
-- 'N' -> 0 (未删除)
-- 'Y' -> 1 (已删除)
UPDATE oauth2_registered_client 
SET del_flag = CASE 
    WHEN deleted = 'N' THEN 0 
    WHEN deleted = 'Y' THEN 1 
    ELSE 0 
END;

-- 3. 删除旧字段 deleted
ALTER TABLE oauth2_registered_client 
DROP COLUMN deleted;

