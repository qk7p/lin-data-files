CREATE TABLE IF NOT EXISTS `clan_notices` (
  `clan_id` INT NOT NULL DEFAULT 0,
  `enabled` BOOL DEFAULT FALSE NOT NULL,
  `notice` TEXT NOT NULL,
  PRIMARY KEY  (`clan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;