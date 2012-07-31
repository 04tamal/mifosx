CREATE TABLE `portfolio_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKJPWG000000000003` (`createdby_id`),
  KEY `FKJPWG000000000004` (`lastmodifiedby_id`),
  CONSTRAINT `FKJPWG000000000003` FOREIGN KEY (`createdby_id`) REFERENCES `admin_appuser` (`id`),
  KEY `external_id` (`external_id`),
  CONSTRAINT `FKJPWG000000000004` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `admin_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;