
CREATE TABLE `sm_data` (
  `id` VARCHAR(36) NOT NULL ,
  `created_at` BIGINT NOT NULL ,
  `modified_at` BIGINT NOT NULL ,
  `created_by` CHAR(36) NOT NULL ,
  `modified_by` CHAR(36) NOT NULL ,
  
  `machine_type` VARCHAR(128) NOT NULL,
  
  `subject_id` CHAR(36) NULL,
  `current_state_name` VARCHAR(128) NULL,
  `current_state_id` CHAR(36) NULL,
  `finished` BIT DEFAULT 0 NOT NULL,
  `exception` BIT DEFAULT 0 NOT NULL,

  `vars` MEDIUMTEXT NULL,
  
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC)
  
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE `sm_state_data` (
  `id` VARCHAR(36) NOT NULL ,
  `created_at` BIGINT NOT NULL ,
  `modified_at` BIGINT NOT NULL ,
  `created_by` CHAR(36) NOT NULL ,
  `modified_by` CHAR(36) NOT NULL ,

  `machine_type` VARCHAR(128) NOT NULL,
  `machine_id` CHAR(36) NOT NULL,
  `state_name` VARCHAR(128) NOT NULL,
  
  `result_message` VARCHAR(254) NULL,
  `exception` BIT DEFAULT 0 NOT NULL,
  `to_state` VARCHAR(128) NULL,
  `to_state_id` CHAR(36) NULL,

  `params` MEDIUMTEXT NULL,
  `state` MEDIUMTEXT NULL,
  `result` MEDIUMTEXT NULL,
  
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  
  INDEX `sm_state_data_IDX_machine` USING BTREE (`machine_id` ASC),
  CONSTRAINT `sm_state_data_FK_machine` FOREIGN KEY (`machine_id` ) REFERENCES `sm_data` (`id` ) ON DELETE CASCADE ON UPDATE CASCADE,
  
  INDEX `sm_state_data_IDX_state` USING BTREE (`to_state_id` ASC),
  CONSTRAINT `sm_state_data_FK_state` FOREIGN KEY (`to_state_id` ) REFERENCES `sm_state_data` (`id` ) ON DELETE CASCADE ON UPDATE CASCADE
  
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;


ALTER TABLE `sm_data`
  ADD INDEX `sm_data_IDX_state` USING BTREE (`current_state_id` ASC),
  ADD CONSTRAINT `sm_data_FK_state` FOREIGN KEY (`current_state_id` ) REFERENCES `sm_state_data` (`id` ) ON DELETE CASCADE ON UPDATE CASCADE;


