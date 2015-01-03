# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table LinkedAccount (
  id                        bigint not null,
  user_id                   bigint,
  provider_user_id          varchar(255),
  provider_key              varchar(255),
  constraint pk_LinkedAccount primary key (id))
;

create table Log (
  id                        bigint not null,
  user_id                   bigint,
  log                       varchar(10000),
  constraint pk_Log primary key (id))
;

create table SecurityRole (
  id                        bigint not null,
  role_name                 varchar(255),
  constraint pk_SecurityRole primary key (id))
;

create table TestAssertion (
  id                        bigint not null,
  test_group_id             bigint,
  ta_id                     varchar(255) not null,
  normative_source          varchar(255) not null,
  target                    varchar(255) not null,
  prerequisites             varchar(255),
  predicate                 varchar(255) not null,
  variables                 varchar(255),
  constraint uq_TestAssertion_ta_id unique (ta_id),
  constraint pk_TestAssertion primary key (id))
;

create table TestCase (
  id                        bigint not null,
  test_assertion_id         bigint,
  name                      varchar(255) not null,
  short_description         varchar(50) not null,
  description               varchar(255),
  tdl                       varchar(10000) not null,
  parameters                varchar(255),
  constraint uq_TestCase_name unique (name),
  constraint pk_TestCase primary key (id))
;

create table TestCaseGroup (
  id                        bigint not null,
  name                      varchar(255) not null,
  owner_id                  bigint,
  short_description         varchar(50) not null,
  description               varchar(255),
  constraint uq_TestCaseGroup_name unique (name),
  constraint pk_TestCaseGroup primary key (id))
;

create table TestRun (
  id                        bigint not null,
  test_case_id              bigint,
  date                      timestamp,
  runner_id                 bigint,
  history_id                bigint,
  constraint pk_TestRun primary key (id))
;

create table TokenAction (
  id                        bigint not null,
  token                     varchar(255),
  target_user_id            bigint,
  type                      varchar(2),
  created                   timestamp,
  expires                   timestamp,
  constraint ck_TokenAction_type check (type in ('PR','EV')),
  constraint uq_TokenAction_token unique (token),
  constraint pk_TokenAction primary key (id))
;

create table Users (
  id                        bigint not null,
  email                     varchar(255),
  name                      varchar(255),
  first_name                varchar(255),
  last_name                 varchar(255),
  last_login                timestamp,
  active                    boolean,
  email_validated           boolean,
  constraint pk_Users primary key (id))
;

create table UserPermission (
  id                        bigint not null,
  value                     varchar(255),
  constraint pk_UserPermission primary key (id))
;


create table Users_SecurityRole (
  Users_id                       bigint not null,
  SecurityRole_id                bigint not null,
  constraint pk_Users_SecurityRole primary key (Users_id, SecurityRole_id))
;

create table Users_UserPermission (
  Users_id                       bigint not null,
  UserPermission_id              bigint not null,
  constraint pk_Users_UserPermission primary key (Users_id, UserPermission_id))
;
create sequence LinkedAccount_seq;

create sequence Log_seq;

create sequence SecurityRole_seq;

create sequence TestAssertion_seq;

create sequence TestCase_seq;

create sequence TestCaseGroup_seq;

create sequence TestRun_seq;

create sequence TokenAction_seq;

create sequence Users_seq;

create sequence UserPermission_seq;

alter table LinkedAccount add constraint fk_LinkedAccount_user_1 foreign key (user_id) references Users (id);
create index ix_LinkedAccount_user_1 on LinkedAccount (user_id);
alter table Log add constraint fk_Log_user_2 foreign key (user_id) references Users (id);
create index ix_Log_user_2 on Log (user_id);
alter table TestAssertion add constraint fk_TestAssertion_testGroup_3 foreign key (test_group_id) references TestCaseGroup (id);
create index ix_TestAssertion_testGroup_3 on TestAssertion (test_group_id);
alter table TestCase add constraint fk_TestCase_testAssertion_4 foreign key (test_assertion_id) references TestAssertion (id);
create index ix_TestCase_testAssertion_4 on TestCase (test_assertion_id);
alter table TestCaseGroup add constraint fk_TestCaseGroup_owner_5 foreign key (owner_id) references Users (id);
create index ix_TestCaseGroup_owner_5 on TestCaseGroup (owner_id);
alter table TestRun add constraint fk_TestRun_testCase_6 foreign key (test_case_id) references TestCase (id);
create index ix_TestRun_testCase_6 on TestRun (test_case_id);
alter table TestRun add constraint fk_TestRun_runner_7 foreign key (runner_id) references Users (id);
create index ix_TestRun_runner_7 on TestRun (runner_id);
alter table TestRun add constraint fk_TestRun_history_8 foreign key (history_id) references Log (id);
create index ix_TestRun_history_8 on TestRun (history_id);
alter table TokenAction add constraint fk_TokenAction_targetUser_9 foreign key (target_user_id) references Users (id);
create index ix_TokenAction_targetUser_9 on TokenAction (target_user_id);



alter table Users_SecurityRole add constraint fk_Users_SecurityRole_Users_01 foreign key (Users_id) references Users (id);

alter table Users_SecurityRole add constraint fk_Users_SecurityRole_Securit_02 foreign key (SecurityRole_id) references SecurityRole (id);

alter table Users_UserPermission add constraint fk_Users_UserPermission_Users_01 foreign key (Users_id) references Users (id);

alter table Users_UserPermission add constraint fk_Users_UserPermission_UserP_02 foreign key (UserPermission_id) references UserPermission (id);

# --- !Downs

drop table if exists LinkedAccount cascade;

drop table if exists Log cascade;

drop table if exists SecurityRole cascade;

drop table if exists TestAssertion cascade;

drop table if exists TestCase cascade;

drop table if exists TestCaseGroup cascade;

drop table if exists TestRun cascade;

drop table if exists TokenAction cascade;

drop table if exists Users cascade;

drop table if exists Users_SecurityRole cascade;

drop table if exists Users_UserPermission cascade;

drop table if exists UserPermission cascade;

drop sequence if exists LinkedAccount_seq;

drop sequence if exists Log_seq;

drop sequence if exists SecurityRole_seq;

drop sequence if exists TestAssertion_seq;

drop sequence if exists TestCase_seq;

drop sequence if exists TestCaseGroup_seq;

drop sequence if exists TestRun_seq;

drop sequence if exists TokenAction_seq;

drop sequence if exists Users_seq;

drop sequence if exists UserPermission_seq;

