CREATE TABLE `answer_0`
(
  id       bigint(64)   not null,
  user_id  bigint(64)   not null,
  `text`   varchar(128) null comment '答案内容',
  `result` char(1)      null comment '是否正确',
  PRIMARY KEY (`id`),
  key index_user_id (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `answer_1`
(
  id       bigint(64)   not null,
  user_id  bigint(64)   not null,
  `text`   varchar(128) null comment '答案内容',
  `result` char(1)      null comment '是否正确',
  PRIMARY KEY (`id`),
  key index_user_id (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `answer_2`
(
  id       bigint(64)   not null,
  user_id  bigint(64)   not null,
  `text`   varchar(128) null comment '答案内容',
  `result` char(1)      null comment '是否正确',
  PRIMARY KEY (`id`),
  key index_user_id (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `answer_3`
(
  id       bigint(64)   not null,
  user_id  bigint(64)   not null,
  `text`   varchar(128) null comment '答案内容',
  `result` char(1)      null comment '是否正确',
  PRIMARY KEY (`id`),
  key index_user_id (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;