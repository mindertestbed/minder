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

create table MappedWrapper (
  id                        bigint not null,
  parameter_id              bigint,
  wrapper_id                bigint,
  run_configuration_id      bigint,
  constraint pk_MappedWrapper primary key (id))
;

create table ParamSignature (
  id                        bigint not null,
  name                      varchar(255),
  wrapper_param_id          bigint,
  constraint pk_ParamSignature primary key (id))
;

create table RunConfiguration (
  id                        bigint not null,
  name                      varchar(255) not null,
  test_case_id              bigint,
  obsolete                  boolean,
  tdl                       varchar(20000),
  constraint uq_RunConfiguration_name unique (name),
  constraint pk_RunConfiguration primary key (id))
;

create table SecurityRole (
  id                        bigint not null,
  role_name                 varchar(255),
  constraint pk_SecurityRole primary key (id))
;

create table OperationType (
  id                        bigint not null,
  OPERATION_TYPE            varchar(16),
  constraint ck_OperationType_OPERATION_TYPE check (OPERATION_TYPE in ('CREATE_TEST_CASE','EDIT_TEST_CASE','RUN_TEST_CASE')),
  constraint pk_OperationType primary key (id))
;

create table TSignal (
  id                        bigint not null,
  wrapper_id                bigint,
  signature                 varchar(255),
  constraint pk_TSignal primary key (id))
;

create table TSlot (
  id                        bigint not null,
  wrapper_id                bigint,
  signature                 varchar(255),
  constraint pk_TSlot primary key (id))
;

create table TestAssertion (
  id                        bigint not null,
  test_group_id             bigint,
  ta_id                     varchar(255) not null,
  normative_source          varchar(250) not null,
  target                    varchar(255) not null,
  prerequisites             varchar(250),
  predicate                 varchar(255) not null,
  variables                 varchar(250),
  tag                       varchar(250),
  description               varchar(1024),
  short_description         varchar(50) not null,
  prescription_level        integer,
  constraint ck_TestAssertion_prescription_level check (prescription_level in (0,1,2)),
  constraint uq_TestAssertion_ta_id unique (ta_id),
  constraint pk_TestAssertion primary key (id))
;

create table TestAsset (
  id                        bigint not null,
  owner_id                  bigint,
  name                      varchar(255) not null,
  short_description         varchar(50),
  description               varchar(1024),
  constraint pk_TestAsset primary key (id))
;

create table TestCase (
  id                        bigint not null,
  test_assertion_id         bigint,
  name                      varchar(255) not null,
  short_description         varchar(50) not null,
  description               varchar(1024),
  tdl                       varchar(20480) not null,
  constraint uq_TestCase_name unique (name),
  constraint pk_TestCase primary key (id))
;

create table TestGroup (
  id                        bigint not null,
  name                      varchar(255) not null,
  owner_id                  bigint,
  description               varchar(1024),
  short_description         varchar(50) not null,
  prst                      integer,
  constraint uq_TestGroup_name unique (name),
  constraint pk_TestGroup primary key (id))
;

create table TestRun (
  id                        bigint not null,
  run_configuration_id      bigint,
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

create table UserHistory (
  id                        bigint not null,
  user_id                   bigint,
  operation_type_id         bigint,
  LOG                       varchar(51200),
  constraint pk_UserHistory primary key (id))
;

create table UserPermission (
  id                        bigint not null,
  value                     varchar(255),
  constraint pk_UserPermission primary key (id))
;

create table Wrapper (
  id                        bigint not null,
  NAME                      varchar(255),
  short_description         varchar(50),
  user_id                   bigint,
  constraint uq_Wrapper_NAME unique (NAME),
  constraint pk_Wrapper primary key (id))
;

create table WrapperParam (
  id                        bigint not null,
  name                      varchar(255) not null,
  test_case_id              bigint,
  constraint pk_WrapperParam primary key (id))
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

create sequence MappedWrapper_seq;

create sequence ParamSignature_seq;

create sequence RunConfiguration_seq;

create sequence SecurityRole_seq;

create sequence OperationType_seq;

create sequence TSignal_seq;

create sequence TSlot_seq;

create sequence TestAssertion_seq;

create sequence TestAsset_seq;

create sequence TestCase_seq;

create sequence TestGroup_seq;

create sequence TestRun_seq;

create sequence TokenAction_seq;

create sequence Users_seq;

create sequence UserHistory_seq;

create sequence UserPermission_seq;

create sequence Wrapper_seq;

create sequence WrapperParam_seq;

alter table LinkedAccount add constraint fk_LinkedAccount_user_1 foreign key (user_id) references Users (id);
create index ix_LinkedAccount_user_1 on LinkedAccount (user_id);
alter table MappedWrapper add constraint fk_MappedWrapper_parameter_2 foreign key (parameter_id) references WrapperParam (id);
create index ix_MappedWrapper_parameter_2 on MappedWrapper (parameter_id);
alter table MappedWrapper add constraint fk_MappedWrapper_wrapper_3 foreign key (wrapper_id) references Wrapper (id);
create index ix_MappedWrapper_wrapper_3 on MappedWrapper (wrapper_id);
alter table MappedWrapper add constraint fk_MappedWrapper_runConfigurat_4 foreign key (run_configuration_id) references RunConfiguration (id);
create index ix_MappedWrapper_runConfigurat_4 on MappedWrapper (run_configuration_id);
alter table ParamSignature add constraint fk_ParamSignature_wrapperParam_5 foreign key (wrapper_param_id) references WrapperParam (id);
create index ix_ParamSignature_wrapperParam_5 on ParamSignature (wrapper_param_id);
alter table RunConfiguration add constraint fk_RunConfiguration_testCase_6 foreign key (test_case_id) references TestCase (id);
create index ix_RunConfiguration_testCase_6 on RunConfiguration (test_case_id);
alter table TSignal add constraint fk_TSignal_wrapper_7 foreign key (wrapper_id) references Wrapper (id);
create index ix_TSignal_wrapper_7 on TSignal (wrapper_id);
alter table TSlot add constraint fk_TSlot_wrapper_8 foreign key (wrapper_id) references Wrapper (id);
create index ix_TSlot_wrapper_8 on TSlot (wrapper_id);
alter table TestAssertion add constraint fk_TestAssertion_testGroup_9 foreign key (test_group_id) references TestGroup (id);
create index ix_TestAssertion_testGroup_9 on TestAssertion (test_group_id);
alter table TestAsset add constraint fk_TestAsset_owner_10 foreign key (owner_id) references Users (id);
create index ix_TestAsset_owner_10 on TestAsset (owner_id);
alter table TestCase add constraint fk_TestCase_testAssertion_11 foreign key (test_assertion_id) references TestAssertion (id);
create index ix_TestCase_testAssertion_11 on TestCase (test_assertion_id);
alter table TestGroup add constraint fk_TestGroup_owner_12 foreign key (owner_id) references Users (id);
create index ix_TestGroup_owner_12 on TestGroup (owner_id);
alter table TestRun add constraint fk_TestRun_runConfiguration_13 foreign key (run_configuration_id) references RunConfiguration (id);
create index ix_TestRun_runConfiguration_13 on TestRun (run_configuration_id);
alter table TestRun add constraint fk_TestRun_runner_14 foreign key (runner_id) references Users (id);
create index ix_TestRun_runner_14 on TestRun (runner_id);
alter table TestRun add constraint fk_TestRun_history_15 foreign key (history_id) references UserHistory (id);
create index ix_TestRun_history_15 on TestRun (history_id);
alter table TokenAction add constraint fk_TokenAction_targetUser_16 foreign key (target_user_id) references Users (id);
create index ix_TokenAction_targetUser_16 on TokenAction (target_user_id);
alter table UserHistory add constraint fk_UserHistory_user_17 foreign key (user_id) references Users (id);
create index ix_UserHistory_user_17 on UserHistory (user_id);
alter table UserHistory add constraint fk_UserHistory_operationType_18 foreign key (operation_type_id) references OperationType (id);
create index ix_UserHistory_operationType_18 on UserHistory (operation_type_id);
alter table Wrapper add constraint fk_Wrapper_user_19 foreign key (user_id) references Users (id);
create index ix_Wrapper_user_19 on Wrapper (user_id);
alter table WrapperParam add constraint fk_WrapperParam_testCase_20 foreign key (test_case_id) references TestCase (id);
create index ix_WrapperParam_testCase_20 on WrapperParam (test_case_id);



alter table Users_SecurityRole add constraint fk_Users_SecurityRole_Users_01 foreign key (Users_id) references Users (id);

alter table Users_SecurityRole add constraint fk_Users_SecurityRole_Securit_02 foreign key (SecurityRole_id) references SecurityRole (id);

alter table Users_UserPermission add constraint fk_Users_UserPermission_Users_01 foreign key (Users_id) references Users (id);

alter table Users_UserPermission add constraint fk_Users_UserPermission_UserP_02 foreign key (UserPermission_id) references UserPermission (id);

# --- !Downs

drop table if exists LinkedAccount cascade;

drop table if exists MappedWrapper cascade;

drop table if exists ParamSignature cascade;

drop table if exists RunConfiguration cascade;

drop table if exists SecurityRole cascade;

drop table if exists OperationType cascade;

drop table if exists TSignal cascade;

drop table if exists TSlot cascade;

drop table if exists TestAssertion cascade;

drop table if exists TestAsset cascade;

drop table if exists TestCase cascade;

drop table if exists TestGroup cascade;

drop table if exists TestRun cascade;

drop table if exists TokenAction cascade;

drop table if exists Users cascade;

drop table if exists Users_SecurityRole cascade;

drop table if exists Users_UserPermission cascade;

drop table if exists UserHistory cascade;

drop table if exists UserPermission cascade;

drop table if exists Wrapper cascade;

drop table if exists WrapperParam cascade;

drop sequence if exists LinkedAccount_seq;

drop sequence if exists MappedWrapper_seq;

drop sequence if exists ParamSignature_seq;

drop sequence if exists RunConfiguration_seq;

drop sequence if exists SecurityRole_seq;

drop sequence if exists OperationType_seq;

drop sequence if exists TSignal_seq;

drop sequence if exists TSlot_seq;

drop sequence if exists TestAssertion_seq;

drop sequence if exists TestAsset_seq;

drop sequence if exists TestCase_seq;

drop sequence if exists TestGroup_seq;

drop sequence if exists TestRun_seq;

drop sequence if exists TokenAction_seq;

drop sequence if exists Users_seq;

drop sequence if exists UserHistory_seq;

drop sequence if exists UserPermission_seq;

drop sequence if exists Wrapper_seq;

drop sequence if exists WrapperParam_seq;

