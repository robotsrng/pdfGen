CREATE DATABASE IF NOT EXISTS `spring_security` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `spring_security` ;
CREATE TABLE IF NOT EXISTS `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
CREATE TABLE IF NOT EXISTS `role` (
  `role_id` int(11) NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  `role` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE TABLE IF NOT EXISTS `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
 `pass_conf` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=0 DEFAULT CHARSET=latin1;
INSERT INTO `user` VALUES(user_id, 'a', 'a', 'a') ;
CREATE TABLE IF NOT EXISTS `user_role` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL,
  `role_id` varchar(45) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uni_username_role` (`role_id`,`username`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=latin1;
INSERT INTO `user_role` VALUES(user_id, 'a', 1);
INSERT INTO `role` VALUES( 1, 'ADMIN', 'ADMIN') ;
INSERT INTO `role` VALUES( 2, 'USER', 'USER') ;
CREATE DATABASE `IDLOCAL` /*!40100 DEFAULT CHARACTER SET latin1 */;
