-- CREATE SCHEMA `kissfarm` DEFAULT CHARACTER SET utf8 ;

CREATE  TABLE `users` (
  `uuid` CHAR(36) NOT NULL ,
  `display_name` VARCHAR(45) NOT NULL ,
  `email` VARCHAR(45) NOT NULL ,
  `time_zone` VARCHAR(20) NOT NULL DEFAULT 'GMT+0' ,
  `locale` CHAR(8) NOT NULL DEFAULT 'en_US' ,
  `registered_at` BIGINT NOT NULL ,
  `is_blocked` TINYINT(1) NOT NULL DEFAULT 0 ,
  `integration_data` VARCHAR(45) NULL ,
  PRIMARY KEY (`uuid`) ,
  UNIQUE INDEX `uuid_UNIQUE` (`uuid` ASC) ,
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) ,
  INDEX `idxDisplayName` (`display_name` ASC) 
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci;

CREATE  TABLE `users_passwords` (
  `user_uuid` CHAR(36) NOT NULL ,
  `password_hash` VARCHAR(160) NULL ,
  `restoration_token` CHAR(36) NULL ,
  PRIMARY KEY (`user_uuid`) ,
  UNIQUE INDEX `user_uuid_UNIQUE` (`user_uuid` ASC) ,
  
  INDEX `users_passwords_IDX_users` USING BTREE (`user_uuid` ASC),
  CONSTRAINT `users_passwords_FK_users` FOREIGN KEY (`user_uuid` ) REFERENCES `users` (`uuid` ) ON DELETE CASCADE ON UPDATE CASCADE  
  
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci;

CREATE  TABLE `users_auth_tokens` (
  `uuid` VARCHAR(36) NOT NULL ,
  `token_value` VARCHAR(36) NOT NULL ,
  `user_uuid` CHAR(36) NOT NULL ,
  `created_at` BIGINT NOT NULL ,
  `expires_at` BIGINT NOT NULL ,
  `last_verified_at` BIGINT NOT NULL ,
  `client_ip` VARCHAR(39) NULL ,
  PRIMARY KEY (`uuid`) ,
  UNIQUE INDEX `uuid_UNIQUE` (`uuid` ASC) ,
  INDEX `idx_expires_at` (`expires_at` ASC) ,
  INDEX `idx_user` (`user_uuid` ASC) 
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci;

CREATE  TABLE `users_permissions` (
  `domain_name` CHAR(45) NOT NULL ,
  `subject_id` CHAR(45) NOT NULL ,
  `user_uuid` CHAR(45) NOT NULL ,
  `permission_key` CHAR(45) NOT NULL ,
  PRIMARY KEY (`domain_name`, `subject_id`, `user_uuid`, `permission_key`) 
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci;


-- Default user account for admin
INSERT INTO `users` (`uuid`, `display_name`, `email`, `time_zone`, `locale`) VALUES ('647e9f66-ce3c-4b38-ac5c-4c387d818510', 'Administrator', 'admin@site.ru', 'America/New_York', 'en_US');
INSERT INTO `users_permissions` (`domain_name`, `subject_id`, `user_uuid`, `permission_key`) VALUES ('DD', 'default', '647e9f66-ce3c-4b38-ac5c-4c387d818510', 'ROLE_USER');
INSERT INTO `users_permissions` (`domain_name`, `subject_id`, `user_uuid`, `permission_key`) VALUES ('DD', 'default', '647e9f66-ce3c-4b38-ac5c-4c387d818510', 'ROLE_ADMIN');
INSERT INTO `users_passwords` (`user_uuid`, `password_hash`, `restoration_token`) VALUES ('647e9f66-ce3c-4b38-ac5c-4c387d818510', '6d3118e6cd6c5b4b25ca4eff52970cb7531afd4404d4706c99a95fb6de0cfcdd9ecddc28783c395c', NULL);

-- User account for background process priveleges elevation
INSERT INTO `users` (`uuid`, `display_name`, `email`, `time_zone`, `locale`) VALUES ('BKG00000-0000-0000-0000-PROCESS00000', 'Background process', 'background@process.id', 'America/New_York', 'en_US');
INSERT INTO `users_permissions` (`domain_name`, `subject_id`, `user_uuid`, `permission_key`) VALUES ('DD', 'default', 'BKG00000-0000-0000-0000-PROCESS00000', 'ROLE_BACKGROUND_PROCESS');

