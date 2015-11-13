# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table abstract_job (
  _type                     integer not null,
  id                        bigserial not null,
  name                      varchar(255) not null,
  tdl_id                    bigint,
  owner_id                  bigint,
  mtdl_parameters           varchar(255),
  test_suite_id             bigint,
  constraint uq_abstract_job_name unique (name),
  constraint pk_abstract_job primary key (id))
;

create table dbrole (
  id                        bigserial not null,
  user_id                   bigint,
  role                      integer,
  constraint ck_dbrole_role check (role in ('0','2','1')),
  constraint pk_dbrole primary key (id))
;

create table GitbEndpoint (
  id                        bigserial not null,
  wrapper_version_id        bigint not null,
  name                      varchar(255) not null,
  description               TEXT,
  constraint pk_GitbEndpoint primary key (id))
;

create table GitbParameter (
  id                        bigserial not null,
  gitb_endpoint_id          bigint not null,
  name                      varchar(255) not null,
  value                     varchar(255) not null,
  use                       integer,
  kind                      integer,
  description               TEXT,
  constraint ck_GitbParameter_use check (use in (0,1)),
  constraint ck_GitbParameter_kind check (kind in (0,1)),
  constraint pk_GitbParameter primary key (id))
;

create table MappedWrapper (
  id                        bigserial not null,
  parameter_id              bigint,
  wrapper_version_id        bigint,
  job_id                    bigint,
  constraint pk_MappedWrapper primary key (id))
;

create table ParamSignature (
  id                        bigserial not null,
  name                      varchar(255),
  wrapper_param_id          bigint,
  constraint pk_ParamSignature primary key (id))
;

create table OperationType (
  id                        bigserial not null,
  OPERATION_TYPE            varchar(16),
  constraint ck_OperationType_OPERATION_TYPE check (OPERATION_TYPE in ('CREATE_TEST_CASE','EDIT_TEST_CASE','RUN_TEST_CASE')),
  constraint pk_OperationType primary key (id))
;

create table TSignal (
  id                        bigserial not null,
  wrapper_version_id        bigint,
  signature                 varchar(255),
  constraint pk_TSignal primary key (id))
;

create table TSlot (
  id                        bigserial not null,
  wrapper_version_id        bigint,
  signature                 varchar(255),
  constraint pk_TSlot primary key (id))
;

create table Tdl (
  id                        bigserial not null,
  test_case_id              bigint,
  tdl                       TEXT not null,
  version                   varchar(255) not null,
  creation_date             timestamp,
  constraint pk_Tdl primary key (id))
;

create table TestAssertion (
  id                        bigserial not null,
  test_group_id             bigint,
  ta_id                     varchar(255) not null,
  normative_source          TEXT not null,
  target                    varchar(255) not null,
  prerequisites             TEXT,
  predicate                 TEXT not null,
  variables                 varchar(1024),
  tag                       varchar(1024),
  description               TEXT,
  short_description         TEXT not null,
  prescription_level        integer,
  owner_id                  bigint,
  constraint ck_TestAssertion_prescription_level check (prescription_level in (0,1,2)),
  constraint uq_TestAssertion_ta_id unique (ta_id),
  constraint pk_TestAssertion primary key (id))
;

create table TestAsset (
  id                        bigserial not null,
  test_group_id             bigint,
  name                      varchar(255) not null,
  short_description         TEXT,
  description               TEXT,
  constraint pk_TestAsset primary key (id))
;

create table TestCase (
  id                        bigserial not null,
  test_assertion_id         bigint,
  name                      varchar(255) not null,
  short_description         TEXT not null,
  description               TEXT,
  owner_id                  bigint,
  constraint uq_TestCase_name unique (name),
  constraint pk_TestCase primary key (id))
;

create table TestGroup (
  id                        bigserial not null,
  name                      varchar(255) not null,
  owner_id                  bigint,
  description               TEXT,
  short_description         TEXT not null,
  dependency_string         varchar(255),
  constraint uq_TestGroup_name unique (name),
  constraint pk_TestGroup primary key (id))
;

create table TestRun (
  id                        bigserial not null,
  job_id                    bigint,
  date                      timestamp,
  runner_id                 bigint,
  history_id                bigint,
  report                    bytea,
  sut_names                 TEXT,
  success                   boolean not null,
  number                    integer,
  error_message             TEXT,
  constraint uq_TestRun_history_id unique (history_id),
  constraint pk_TestRun primary key (id))
;

create table TestSuite (
  id                        bigserial not null,
  name                      varchar(255) not null,
  short_description         TEXT not null,
  description               TEXT,
  mtdl_parameters           varchar(255),
  owner_id                  bigint,
  test_group_id             bigint,
  constraint uq_TestSuite_name unique (name),
  constraint pk_TestSuite primary key (id))
;

create table Users (
  id                        bigserial not null,
  email                     varchar(255),
  name                      varchar(255),
  password                  bytea,
  last_login                timestamp,
  constraint uq_Users_email unique (email),
  constraint pk_Users primary key (id))
;

create table UserAuthentication (
  id                        bigserial not null,
  user_id                   bigint,
  realm                     varchar(255),
  server_nonce              varchar(255),
  issue_time                timestamp,
  expiry_time               timestamp,
  request_counter           integer,
  constraint pk_UserAuthentication primary key (id))
;

create table UserHistory (
  id                        bigserial not null,
  email                     varchar(255),
  operation_type_id         bigint,
  SYSLOG                    bytea,
  constraint uq_UserHistory_operation_type_id unique (operation_type_id),
  constraint pk_UserHistory primary key (id))
;

create table UtilClass (
  id                        bigserial not null,
  test_group_id             bigint,
  name                      varchar(255) not null,
  short_description         TEXT not null,
  source                    TEXT not null,
  owner_id                  bigint,
  constraint pk_UtilClass primary key (id))
;

create table Wrapper (
  id                        bigserial not null,
  NAME                      varchar(255),
  short_description         TEXT not null,
  description               TEXT,
  user_id                   bigint,
  constraint uq_Wrapper_NAME unique (NAME),
  constraint pk_Wrapper primary key (id))
;

create table WrapperParam (
  id                        bigserial not null,
  name                      varchar(255) not null,
  tdl_id                    bigint,
  constraint pk_WrapperParam primary key (id))
;

create table WrapperVersion (
  id                        bigserial not null,
  wrapper_id                bigint,
  version                   varchar(255) not null,
  creation_date             timestamp,
  constraint pk_WrapperVersion primary key (id))
;

alter table abstract_job add constraint fk_abstract_job_tdl_1 foreign key (tdl_id) references Tdl (id);
create index ix_abstract_job_tdl_1 on abstract_job (tdl_id);
alter table abstract_job add constraint fk_abstract_job_owner_2 foreign key (owner_id) references Users (id);
create index ix_abstract_job_owner_2 on abstract_job (owner_id);
alter table abstract_job add constraint fk_abstract_job_testSuite_3 foreign key (test_suite_id) references TestSuite (id);
create index ix_abstract_job_testSuite_3 on abstract_job (test_suite_id);
alter table dbrole add constraint fk_dbrole_user_4 foreign key (user_id) references Users (id);
create index ix_dbrole_user_4 on dbrole (user_id);
alter table GitbEndpoint add constraint fk_GitbEndpoint_WrapperVersion_5 foreign key (wrapper_version_id) references WrapperVersion (id);
create index ix_GitbEndpoint_WrapperVersion_5 on GitbEndpoint (wrapper_version_id);
alter table GitbParameter add constraint fk_GitbParameter_GitbEndpoint_6 foreign key (gitb_endpoint_id) references GitbEndpoint (id);
create index ix_GitbParameter_GitbEndpoint_6 on GitbParameter (gitb_endpoint_id);
alter table MappedWrapper add constraint fk_MappedWrapper_parameter_7 foreign key (parameter_id) references WrapperParam (id);
create index ix_MappedWrapper_parameter_7 on MappedWrapper (parameter_id);
alter table MappedWrapper add constraint fk_MappedWrapper_wrapperVersio_8 foreign key (wrapper_version_id) references WrapperVersion (id);
create index ix_MappedWrapper_wrapperVersio_8 on MappedWrapper (wrapper_version_id);
alter table MappedWrapper add constraint fk_MappedWrapper_job_9 foreign key (job_id) references abstract_job (id);
create index ix_MappedWrapper_job_9 on MappedWrapper (job_id);
alter table ParamSignature add constraint fk_ParamSignature_wrapperPara_10 foreign key (wrapper_param_id) references WrapperParam (id);
create index ix_ParamSignature_wrapperPara_10 on ParamSignature (wrapper_param_id);
alter table TSignal add constraint fk_TSignal_wrapperVersion_11 foreign key (wrapper_version_id) references WrapperVersion (id);
create index ix_TSignal_wrapperVersion_11 on TSignal (wrapper_version_id);
alter table TSlot add constraint fk_TSlot_wrapperVersion_12 foreign key (wrapper_version_id) references WrapperVersion (id);
create index ix_TSlot_wrapperVersion_12 on TSlot (wrapper_version_id);
alter table Tdl add constraint fk_Tdl_testCase_13 foreign key (test_case_id) references TestCase (id);
create index ix_Tdl_testCase_13 on Tdl (test_case_id);
alter table TestAssertion add constraint fk_TestAssertion_testGroup_14 foreign key (test_group_id) references TestGroup (id);
create index ix_TestAssertion_testGroup_14 on TestAssertion (test_group_id);
alter table TestAssertion add constraint fk_TestAssertion_owner_15 foreign key (owner_id) references Users (id);
create index ix_TestAssertion_owner_15 on TestAssertion (owner_id);
alter table TestAsset add constraint fk_TestAsset_testGroup_16 foreign key (test_group_id) references TestGroup (id);
create index ix_TestAsset_testGroup_16 on TestAsset (test_group_id);
alter table TestCase add constraint fk_TestCase_testAssertion_17 foreign key (test_assertion_id) references TestAssertion (id);
create index ix_TestCase_testAssertion_17 on TestCase (test_assertion_id);
alter table TestCase add constraint fk_TestCase_owner_18 foreign key (owner_id) references Users (id);
create index ix_TestCase_owner_18 on TestCase (owner_id);
alter table TestGroup add constraint fk_TestGroup_owner_19 foreign key (owner_id) references Users (id);
create index ix_TestGroup_owner_19 on TestGroup (owner_id);
alter table TestRun add constraint fk_TestRun_job_20 foreign key (job_id) references abstract_job (id);
create index ix_TestRun_job_20 on TestRun (job_id);
alter table TestRun add constraint fk_TestRun_runner_21 foreign key (runner_id) references Users (id);
create index ix_TestRun_runner_21 on TestRun (runner_id);
alter table TestRun add constraint fk_TestRun_history_22 foreign key (history_id) references UserHistory (id);
create index ix_TestRun_history_22 on TestRun (history_id);
alter table TestSuite add constraint fk_TestSuite_owner_23 foreign key (owner_id) references Users (id);
create index ix_TestSuite_owner_23 on TestSuite (owner_id);
alter table TestSuite add constraint fk_TestSuite_testGroup_24 foreign key (test_group_id) references TestGroup (id);
create index ix_TestSuite_testGroup_24 on TestSuite (test_group_id);
alter table UserAuthentication add constraint fk_UserAuthentication_user_25 foreign key (user_id) references Users (id);
create index ix_UserAuthentication_user_25 on UserAuthentication (user_id);
alter table UserHistory add constraint fk_UserHistory_operationType_26 foreign key (operation_type_id) references OperationType (id);
create index ix_UserHistory_operationType_26 on UserHistory (operation_type_id);
alter table UtilClass add constraint fk_UtilClass_testGroup_27 foreign key (test_group_id) references TestGroup (id);
create index ix_UtilClass_testGroup_27 on UtilClass (test_group_id);
alter table UtilClass add constraint fk_UtilClass_owner_28 foreign key (owner_id) references Users (id);
create index ix_UtilClass_owner_28 on UtilClass (owner_id);
alter table Wrapper add constraint fk_Wrapper_user_29 foreign key (user_id) references Users (id);
create index ix_Wrapper_user_29 on Wrapper (user_id);
alter table WrapperParam add constraint fk_WrapperParam_tdl_30 foreign key (tdl_id) references Tdl (id);
create index ix_WrapperParam_tdl_30 on WrapperParam (tdl_id);
alter table WrapperVersion add constraint fk_WrapperVersion_wrapper_31 foreign key (wrapper_id) references Wrapper (id);
create index ix_WrapperVersion_wrapper_31 on WrapperVersion (wrapper_id);



# --- !Downs

drop table if exists abstract_job cascade;

drop table if exists dbrole cascade;

drop table if exists GitbEndpoint cascade;

drop table if exists GitbParameter cascade;

drop table if exists MappedWrapper cascade;

drop table if exists ParamSignature cascade;

drop table if exists OperationType cascade;

drop table if exists TSignal cascade;

drop table if exists TSlot cascade;

drop table if exists Tdl cascade;

drop table if exists TestAssertion cascade;

drop table if exists TestAsset cascade;

drop table if exists TestCase cascade;

drop table if exists TestGroup cascade;

drop table if exists TestRun cascade;

drop table if exists TestSuite cascade;

drop table if exists Users cascade;

drop table if exists UserAuthentication cascade;

drop table if exists UserHistory cascade;

drop table if exists UtilClass cascade;

drop table if exists Wrapper cascade;

drop table if exists WrapperParam cascade;

drop table if exists WrapperVersion cascade;

