
CREATE TABLE `agent_auth_tokens` (
  `id` CHAR(36) NOT NULL ,
  `created_at` BIGINT NOT NULL ,
  `modified_at` BIGINT NOT NULL ,
  `created_by` CHAR(36) NOT NULL ,
  `modified_by` CHAR(36) NOT NULL ,
  
  `comment` VARCHAR(128) NULL,
  `enabled` BIT DEFAULT 1 NOT NULL,
  
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC)
  
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;


CREATE TABLE `nodes` (
  `id` VARCHAR(36) NOT NULL ,
  `created_at` BIGINT NOT NULL ,
  `modified_at` BIGINT NOT NULL ,
  `created_by` CHAR(36) NOT NULL ,
  `modified_by` CHAR(36) NOT NULL ,
  
  `agent_auth_token` VARCHAR(64) NOT NULL,
  `password` VARCHAR(64) NOT NULL,
  `host_name` VARCHAR(128) NOT NULL,
  `public_ip` VARCHAR(24) NOT NULL,
  `blocked` BIT DEFAULT 0 NOT NULL,
  
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC)
  
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;


CREATE TABLE `node_tags` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `created_at` BIGINT NOT NULL ,
  `modified_at` BIGINT NOT NULL ,
  `created_by` CHAR(36) NOT NULL ,
  `modified_by` CHAR(36) NOT NULL ,
  
  `subject_id` VARCHAR(36) NOT NULL,
  `tag` VARCHAR(64) NOT NULL,
  
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),

  INDEX `node_tags_IDX_duplicateTag` USING BTREE (`subject_id` ASC, `tag` ASC),
  
  INDEX `node_tags_IDX_nodes` USING BTREE (`subject_id` ASC),
  CONSTRAINT `node_tags_FK_nodes` FOREIGN KEY (`subject_id` ) REFERENCES `nodes` (`id` ) ON DELETE CASCADE ON UPDATE CASCADE
  
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;


CREATE TABLE `node_status` (
  `id` VARCHAR(36) NOT NULL ,
  `created_at` BIGINT NOT NULL ,
  `modified_at` BIGINT NOT NULL ,
  `online` BIT NOT NULL,
  
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
