CREATE DATABASE IF NOT EXISTS `AGG` CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci';

USE `AGG`;

CREATE TABLE IF NOT EXISTS `AGGREGATE_FRAMEWORK` (
  `ID` bigint NOT NULL AUTO_INCREMENT,
  `DOMAIN` varchar(100) DEFAULT NULL,
  `XID` varchar(64) NOT NULL,
  `CONTENT` varbinary(8000) DEFAULT NULL,
  `RETRIED_COUNT` int(11) DEFAULT NULL,
  `REQUEST_ID` int(11) DEFAULT NULL,
  `IS_DELETE` tinyint(1) DEFAULT 0 NOT NULL,
  `CREATE_TIME` datetime DEFAULT NULL,
  `LAST_UPDATE_TIME` datetime DEFAULT NULL,
  `VERSION` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UX_XID` (`XID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `AGG_DOMAIN` (
  `ID` bigint NOT NULL AUTO_INCREMENT,
  `DOMAIN` varchar(100) NOT NULL,
  `PHONE_NUMBERS` varchar(250),
  `ALERT_TYPE` varchar(10),
  `THRESHOLD` integer DEFAULT 0,
  `INTERVAL_MINUTES` integer DEFAULT 0,
  `LAST_ALERT_TIME` datetime DEFAULT NULL,
  `DING_ROBOT_URL` varchar(250) DEFAULT NULL,
  `CREATE_TIME` datetime DEFAULT NULL,
  `LAST_UPDATE_TIME` datetime DEFAULT NULL,
  `VERSION` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UX_DOMAIN` (`DOMAIN`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
