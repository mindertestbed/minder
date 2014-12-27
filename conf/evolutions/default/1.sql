# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table linked_account (
  id                        bigint not null,
  user_id                   integer,
  provider_user_id          varchar(255),
  provider_key              varchar(255),
  constraint pk_linked_account primary key (id))
;

create table minder_entity (
  id                        integer not null,
  constraint pk_minder_entity primary key (id))
;

create table security_role (
  id                        bigint not null,
  role_name                 varchar(255),
  constraint pk_security_role primary key (id))
;

create table test_assertion (
  id                        integer not null,
  test_assertion_id         integer not null,
  ta_id                     varchar(255) not null,
  normative_source          varchar(255) not null,
  target                    varchar(255) not null,
  prerequisites             varchar(255),
  predicate                 varchar(255) not null,
  variables                 varchar(255),
  constraint uq_test_assertion_ta_id unique (ta_id),
  constraint pk_test_assertion primary key (id))
;

create table test_case (
  id                        integer not null,
  test_case_group_id        integer not null,
  source_id                 integer,
  test_case_name            varchar(255) not null,
  short_description         varchar(50) not null,
  description               varchar(255),
  tdl                       varchar(255) not null,
  parameters                varchar(255),
  constraint uq_test_case_test_case_name unique (test_case_name),
  constraint pk_test_case primary key (id))
;

create table test_case_category (
  id                        integer not null,
  owner_id                  integer,
  name                      varchar(255) not null,
  short_description         varchar(50) not null,
  description               varchar(255),
  constraint uq_test_case_category_name unique (name),
  constraint pk_test_case_category primary key (id))
;

create table test_case_group (
  id                        integer not null,
  name                      varchar(255) not null,
  category_id               integer,
  short_description         varchar(50) not null,
  description               varchar(255),
  constraint uq_test_case_group_name unique (name),
  constraint pk_test_case_group primary key (id))
;

create table token_action (
  id                        bigint not null,
  token                     varchar(255),
  target_user_id            integer,
  type                      varchar(2),
  created                   timestamp,
  expires                   timestamp,
  constraint ck_token_action_type check (type in ('PR','EV')),
  constraint uq_token_action_token unique (token),
  constraint pk_token_action primary key (id))
;

create table users (
  id                        integer not null,
  email                     varchar(255),
  name                      varchar(255),
  first_name                varchar(255),
  last_name                 varchar(255),
  last_login                timestamp,
  active                    boolean,
  email_validated           boolean,
  constraint pk_users primary key (id))
;

create table user_permission (
  id                        bigint not null,
  value                     varchar(255),
  constraint pk_user_permission primary key (id))
;


create table users_security_role (
  users_id                       integer not null,
  security_role_id               bigint not null,
  constraint pk_users_security_role primary key (users_id, security_role_id))
;

create table users_user_permission (
  users_id                       integer not null,
  user_permission_id             bigint not null,
  constraint pk_users_user_permission primary key (users_id, user_permission_id))
;
create sequence linked_account_seq;

create sequence minder_entity_seq;

create sequence security_role_seq;

create sequence test_assertion_seq;

create sequence test_case_seq;

create sequence test_case_category_seq;

create sequence test_case_group_seq;

create sequence token_action_seq;

create sequence users_seq;

create sequence user_permission_seq;

alter table linked_account add constraint fk_linked_account_user_1 foreign key (user_id) references users (id);
create index ix_linked_account_user_1 on linked_account (user_id);
alter table test_assertion add constraint fk_test_assertion_test_asserti_2 foreign key (test_assertion_id) references test_assertion (id);
create index ix_test_assertion_test_asserti_2 on test_assertion (test_assertion_id);
alter table test_case add constraint fk_test_case_test_case_group_3 foreign key (test_case_group_id) references test_case_group (id);
create index ix_test_case_test_case_group_3 on test_case (test_case_group_id);
alter table test_case add constraint fk_test_case_source_4 foreign key (source_id) references test_assertion (id);
create index ix_test_case_source_4 on test_case (source_id);
alter table test_case_category add constraint fk_test_case_category_owner_5 foreign key (owner_id) references users (id);
create index ix_test_case_category_owner_5 on test_case_category (owner_id);
alter table test_case_group add constraint fk_test_case_group_category_6 foreign key (category_id) references test_case_category (id);
create index ix_test_case_group_category_6 on test_case_group (category_id);
alter table token_action add constraint fk_token_action_targetUser_7 foreign key (target_user_id) references users (id);
create index ix_token_action_targetUser_7 on token_action (target_user_id);



alter table users_security_role add constraint fk_users_security_role_users_01 foreign key (users_id) references users (id);

alter table users_security_role add constraint fk_users_security_role_securi_02 foreign key (security_role_id) references security_role (id);

alter table users_user_permission add constraint fk_users_user_permission_user_01 foreign key (users_id) references users (id);

alter table users_user_permission add constraint fk_users_user_permission_user_02 foreign key (user_permission_id) references user_permission (id);

# --- !Downs

drop table if exists linked_account cascade;

drop table if exists minder_entity cascade;

drop table if exists security_role cascade;

drop table if exists test_assertion cascade;

drop table if exists test_case cascade;

drop table if exists test_case_category cascade;

drop table if exists test_case_group cascade;

drop table if exists token_action cascade;

drop table if exists users cascade;

drop table if exists users_security_role cascade;

drop table if exists users_user_permission cascade;

drop table if exists user_permission cascade;

drop sequence if exists linked_account_seq;

drop sequence if exists minder_entity_seq;

drop sequence if exists security_role_seq;

drop sequence if exists test_assertion_seq;

drop sequence if exists test_case_seq;

drop sequence if exists test_case_category_seq;

drop sequence if exists test_case_group_seq;

drop sequence if exists token_action_seq;

drop sequence if exists users_seq;

drop sequence if exists user_permission_seq;

