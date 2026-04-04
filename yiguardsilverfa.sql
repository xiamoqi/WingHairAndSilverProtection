/*
SQLyog Ultimate v11.25 (64 bit)
MySQL - 8.0.26 : Database - yifa_silver_guard
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`yifa_silver_guard` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `yifa_silver_guard`;

/*Table structure for table `device_sensor_data` */

DROP TABLE IF EXISTS `device_sensor_data`;

CREATE TABLE `device_sensor_data` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `device_id` varchar(64) NOT NULL COMMENT '设备编号',
  `elder_id` bigint DEFAULT NULL COMMENT '老人ID',
  `temperature` decimal(4,1) DEFAULT NULL COMMENT '环境温度',
  `humidity` int DEFAULT NULL COMMENT '环境湿度%',
  `gas_status` tinyint DEFAULT '0' COMMENT '0-正常 1-异常',
  `is_move` tinyint DEFAULT '0' COMMENT '0-静止 1-移动',
  `fall_status` tinyint DEFAULT '0' COMMENT '0-正常 1-跌倒',
  `report_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_device_time` (`device_id`,`report_time`),
  KEY `idx_elder_id` (`elder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备传感器数据';

/*Data for the table `device_sensor_data` */

/*Table structure for table `elder_info` */

DROP TABLE IF EXISTS `elder_info`;

CREATE TABLE `elder_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '老人ID',
  `user_id` bigint NOT NULL COMMENT '对应用户ID',
  `age` int DEFAULT NULL COMMENT '年龄',
  `gender` tinyint DEFAULT NULL COMMENT '性别 1-男 2-女',
  `height` int DEFAULT NULL COMMENT '身高cm',
  `weight` int DEFAULT NULL COMMENT '体重kg',
  `medical_history` text COMMENT '既往病史',
  `allergy` varchar(255) DEFAULT NULL COMMENT '过敏史',
  `emergency_contact` varchar(50) DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(20) DEFAULT NULL COMMENT '紧急联系电话',
  `address` varchar(255) DEFAULT NULL COMMENT '家庭住址',
  `device_id` varchar(64) DEFAULT NULL COMMENT '绑定设备编号（树莓派）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` tinyint DEFAULT '1' COMMENT '是否存在',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='老人信息档案';

/*Data for the table `elder_info` */

/*Table structure for table `family_bind` */

DROP TABLE IF EXISTS `family_bind`;

CREATE TABLE `family_bind` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_user_id` bigint NOT NULL COMMENT '家属用户ID',
  `elder_id` bigint NOT NULL COMMENT '老人ID',
  `relation` varchar(30) DEFAULT NULL COMMENT '关系 子女/配偶/其他',
  `status` tinyint DEFAULT '1' COMMENT '0-解绑 1-绑定',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_elder` (`family_user_id`,`elder_id`),
  KEY `idx_elder_id` (`elder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='家属老人绑定';

/*Data for the table `family_bind` */

/*Table structure for table `health_data` */

DROP TABLE IF EXISTS `health_data`;

CREATE TABLE `health_data` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `elder_id` bigint NOT NULL,
  `heart_rate` int DEFAULT NULL COMMENT '心率',
  `blood_pressure_high` int DEFAULT NULL COMMENT '收缩压',
  `blood_pressure_low` int DEFAULT NULL COMMENT '舒张压',
  `blood_sugar` decimal(4,1) DEFAULT NULL COMMENT '血糖',
  `temperature` decimal(3,1) DEFAULT NULL COMMENT '体温',
  `record_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  `source` tinyint DEFAULT '1' COMMENT '1-手动 2-设备自动',
  PRIMARY KEY (`id`),
  KEY `idx_elder_time` (`elder_id`,`record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='健康数据';

/*Data for the table `health_data` */

/*Table structure for table `health_qa` */

DROP TABLE IF EXISTS `health_qa`;

CREATE TABLE `health_qa` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `elder_id` bigint NOT NULL,
  `question` text NOT NULL COMMENT '问题',
  `answer` text NOT NULL COMMENT '回答',
  `is_emergency` tinyint DEFAULT '0' COMMENT '0-普通 1-紧急',
  `ask_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_elder_time` (`elder_id`,`ask_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='健康问答记录';

/*Data for the table `health_qa` */

insert  into `health_qa`(`id`,`elder_id`,`question`,`answer`,`is_emergency`,`ask_time`) values (1,1,'用户没有输入问题','您好，请问有什么可以帮您？',0,'2026-04-02 23:26:45');

/*Table structure for table `medicine_remind` */

DROP TABLE IF EXISTS `medicine_remind`;

CREATE TABLE `medicine_remind` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `elder_id` bigint NOT NULL COMMENT '老人ID',
  `medicine_name` varchar(100) NOT NULL COMMENT '药品名称',
  `dosage` varchar(100) DEFAULT NULL COMMENT '用量',
  `usage` varchar(100) DEFAULT NULL COMMENT '用法',
  `remind_time` time NOT NULL COMMENT '提醒时间',
  `remind_days` varchar(50) DEFAULT '1,2,3,4,5,6,7' COMMENT '提醒日期 1-7',
  `status` tinyint DEFAULT '1' COMMENT '0-关闭 1-开启',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_elder_id` (`elder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用药提醒';

/*Data for the table `medicine_remind` */

/*Table structure for table `service_order` */

DROP TABLE IF EXISTS `service_order`;

CREATE TABLE `service_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `elder_id` bigint NOT NULL,
  `service_type` tinyint NOT NULL COMMENT '1-上门送餐 2-陪诊 3-保洁 4-其他',
  `content` varchar(255) DEFAULT NULL COMMENT '需求说明',
  `appoint_time` datetime NOT NULL COMMENT '预约时间',
  `status` tinyint DEFAULT '0' COMMENT '0-待接单 1-已接单 2-已完成 3-取消',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_elder_time` (`elder_id`,`appoint_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务预约单';

/*Data for the table `service_order` */

/*Table structure for table `sys_config` */

DROP TABLE IF EXISTS `sys_config`;

CREATE TABLE `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` varchar(255) DEFAULT NULL COMMENT '配置值',
  `remark` varchar(255) DEFAULT NULL COMMENT '说明',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置';

/*Data for the table `sys_config` */

/*Table structure for table `sys_user` */

DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '账号',
  `email` varchar(50) NOT NULL COMMENT '邮箱',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '姓名',
  `role` tinyint NOT NULL COMMENT '角色 1-老人 2-家属 3-管理员',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `status` tinyint DEFAULT '1' COMMENT '状态 0-禁用 1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `phone` varchar(50) DEFAULT NULL COMMENT 'phone',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户';

/*Data for the table `sys_user` */

insert  into `sys_user`(`id`,`username`,`email`,`password`,`nickname`,`role`,`avatar`,`status`,`create_time`,`update_time`,`phone`) values (1,'夏夏','3074964566@qq.com','49cbc143897d40a775f04737d1caa81a83bb26eadd313e5679b75033db016ce3','测试用户',1,NULL,1,'2026-03-31 23:11:35','2026-03-31 23:11:35',NULL);

/*Table structure for table `warning_event` */

DROP TABLE IF EXISTS `warning_event`;

CREATE TABLE `warning_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `elder_id` bigint NOT NULL,
  `type` tinyint NOT NULL COMMENT '1-跌倒 2-紧急求助 3-环境异常 4-健康异常',
  `content` varchar(255) NOT NULL COMMENT '预警内容',
  `location` varchar(255) DEFAULT NULL COMMENT '位置',
  `handle_status` tinyint DEFAULT '0' COMMENT '0-未处理 1-已处理',
  `handle_user` bigint DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_elder_status` (`elder_id`,`handle_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预警事件';

/*Data for the table `warning_event` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
