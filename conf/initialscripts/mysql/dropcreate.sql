SET FOREIGN_KEY_CHECKS=0;

drop table dbrole;

drop table Job;

drop table MappedWrapper;

drop table ParamSignature;

drop table OperationType;

drop table TSignal;

drop table TSlot;

drop table Tdl;

drop table TestAssertion;

drop table TestAsset;

drop table TestCase;

drop table TestGroup;

drop table TestRun;

drop table TokenAction;

drop table Users;

drop table UserHistory;

drop table UtilClass;

drop table Wrapper;

drop table WrapperParam;

drop table WrapperVersion;

SET FOREIGN_KEY_CHECKS=1;

create table dbrole (
  id                        bigint auto_increment not null,
  user_id                   bigint,
  role                      integer,
  constraint ck_dbrole_role check (role in ('0','2','1')),
  constraint pk_dbrole primary key (id))
;

create table Job (
  id                        bigint auto_increment not null,
  name                      varchar(255) not null,
  tdl_id                    bigint,
  owner_id                  bigint,
  mtdl_parameters           varchar(255),
  constraint uq_Job_name unique (name),
  constraint pk_Job primary key (id))
;

create table MappedWrapper (
  id                        bigint auto_increment not null,
  parameter_id              bigint,
  wrapper_version_id        bigint,
  job_id                    bigint,
  constraint uq_MappedWrapper_wrapper_version_id unique (wrapper_version_id),
  constraint pk_MappedWrapper primary key (id))
;

create table ParamSignature (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  wrapper_param_id          bigint,
  constraint pk_ParamSignature primary key (id))
;

create table OperationType (
  id                        bigint auto_increment not null,
  OPERATION_TYPE            varchar(16),
  constraint ck_OperationType_OPERATION_TYPE check (OPERATION_TYPE in ('CREATE_TEST_CASE','EDIT_TEST_CASE','RUN_TEST_CASE')),
  constraint pk_OperationType primary key (id))
;

create table TSignal (
  id                        bigint auto_increment not null,
  wrapper_version_id        bigint,
  signature                 varchar(255),
  constraint pk_TSignal primary key (id))
;

create table TSlot (
  id                        bigint auto_increment not null,
  wrapper_version_id        bigint,
  signature                 varchar(255),
  constraint pk_TSlot primary key (id))
;

create table Tdl (
  id                        bigint auto_increment not null,
  test_case_id              bigint,
  tdl                       TEXT not null,
  version                   varchar(255) not null,
  creation_date             datetime(6),
  constraint pk_Tdl primary key (id))
;

create table TestAssertion (
  id                        bigint auto_increment not null,
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
  id                        bigint auto_increment not null,
  group_id                  bigint,
  name                      varchar(255) not null,
  short_description         TEXT,
  description               TEXT,
  constraint pk_TestAsset primary key (id))
;

create table TestCase (
  id                        bigint auto_increment not null,
  test_assertion_id         bigint,
  name                      varchar(255) not null,
  short_description         TEXT not null,
  description               TEXT,
  owner_id                  bigint,
  constraint uq_TestCase_name unique (name),
  constraint pk_TestCase primary key (id))
;

create table TestGroup (
  id                        bigint auto_increment not null,
  name                      varchar(255) not null,
  owner_id                  bigint,
  description               TEXT,
  short_description         TEXT not null,
  dependency_string         varchar(255),
  constraint uq_TestGroup_name unique (name),
  constraint pk_TestGroup primary key (id))
;

create table TestRun (
  id                        bigint auto_increment not null,
  job_id                    bigint,
  date                      datetime(6),
  runner_id                 bigint,
  history_id                bigint,
  report                    varbinary(40960),
  sut_names                 TEXT,
  success                   tinyint(1) default 0 not null,
  number                    integer,
  error_message             TEXT,
  constraint uq_TestRun_history_id unique (history_id),
  constraint pk_TestRun primary key (id))
;

create table TokenAction (
  id                        bigint auto_increment not null,
  token                     varchar(255),
  target_user_id            bigint,
  type                      varchar(2),
  created                   datetime(6),
  expires                   datetime(6),
  constraint ck_TokenAction_type check (type in ('PR','EV')),
  constraint uq_TokenAction_token unique (token),
  constraint pk_TokenAction primary key (id))
;

create table Users (
  id                        bigint auto_increment not null,
  email                     varchar(255),
  name                      varchar(255),
  password                  varbinary(255),
  last_login                datetime(6),
  constraint uq_Users_email unique (email),
  constraint pk_Users primary key (id))
;

create table UserHistory (
  id                        bigint auto_increment not null,
  email                     varchar(255),
  operation_type_id         bigint,
  SYSLOG                    varbinary(51200),
  constraint uq_UserHistory_operation_type_id unique (operation_type_id),
  constraint pk_UserHistory primary key (id))
;

create table UtilClass (
  id                        bigint auto_increment not null,
  test_group_id             bigint,
  name                      varchar(255) not null,
  short_description         TEXT not null,
  source                    TEXT not null,
  owner_id                  bigint,
  constraint pk_UtilClass primary key (id))
;

create table Wrapper (
  id                        bigint auto_increment not null,
  NAME                      varchar(255),
  short_description         TEXT not null,
  description               TEXT,
  user_id                   bigint,
  constraint uq_Wrapper_NAME unique (NAME),
  constraint pk_Wrapper primary key (id))
;

create table WrapperParam (
  id                        bigint auto_increment not null,
  name                      varchar(255) not null,
  tdl_id                    bigint,
  constraint pk_WrapperParam primary key (id))
;

create table WrapperVersion (
  id                        bigint auto_increment not null,
  wrapper_id                bigint,
  version                   varchar(255) not null,
  creation_date             datetime(6),
  constraint pk_WrapperVersion primary key (id))
;

alter table dbrole add constraint fk_dbrole_user_1 foreign key (user_id) references Users (id) on delete restrict on update restrict;
create index ix_dbrole_user_1 on dbrole (user_id);
alter table Job add constraint fk_Job_tdl_2 foreign key (tdl_id) references Tdl (id) on delete restrict on update restrict;
create index ix_Job_tdl_2 on Job (tdl_id);
alter table Job add constraint fk_Job_owner_3 foreign key (owner_id) references Users (id) on delete restrict on update restrict;
create index ix_Job_owner_3 on Job (owner_id);
alter table MappedWrapper add constraint fk_MappedWrapper_parameter_4 foreign key (parameter_id) references WrapperParam (id) on delete restrict on update restrict;
create index ix_MappedWrapper_parameter_4 on MappedWrapper (parameter_id);
alter table MappedWrapper add constraint fk_MappedWrapper_wrapperVersion_5 foreign key (wrapper_version_id) references WrapperVersion (id) on delete restrict on update restrict;
create index ix_MappedWrapper_wrapperVersion_5 on MappedWrapper (wrapper_version_id);
alter table MappedWrapper add constraint fk_MappedWrapper_job_6 foreign key (job_id) references Job (id) on delete restrict on update restrict;
create index ix_MappedWrapper_job_6 on MappedWrapper (job_id);
alter table ParamSignature add constraint fk_ParamSignature_wrapperParam_7 foreign key (wrapper_param_id) references WrapperParam (id) on delete restrict on update restrict;
create index ix_ParamSignature_wrapperParam_7 on ParamSignature (wrapper_param_id);
alter table TSignal add constraint fk_TSignal_wrapperVersion_8 foreign key (wrapper_version_id) references WrapperVersion (id) on delete restrict on update restrict;
create index ix_TSignal_wrapperVersion_8 on TSignal (wrapper_version_id);
alter table TSlot add constraint fk_TSlot_wrapperVersion_9 foreign key (wrapper_version_id) references WrapperVersion (id) on delete restrict on update restrict;
create index ix_TSlot_wrapperVersion_9 on TSlot (wrapper_version_id);
alter table Tdl add constraint fk_Tdl_testCase_10 foreign key (test_case_id) references TestCase (id) on delete restrict on update restrict;
create index ix_Tdl_testCase_10 on Tdl (test_case_id);
alter table TestAssertion add constraint fk_TestAssertion_testGroup_11 foreign key (test_group_id) references TestGroup (id) on delete restrict on update restrict;
create index ix_TestAssertion_testGroup_11 on TestAssertion (test_group_id);
alter table TestAssertion add constraint fk_TestAssertion_owner_12 foreign key (owner_id) references Users (id) on delete restrict on update restrict;
create index ix_TestAssertion_owner_12 on TestAssertion (owner_id);
alter table TestAsset add constraint fk_TestAsset_group_13 foreign key (group_id) references TestGroup (id) on delete restrict on update restrict;
create index ix_TestAsset_group_13 on TestAsset (group_id);
alter table TestCase add constraint fk_TestCase_testAssertion_14 foreign key (test_assertion_id) references TestAssertion (id) on delete restrict on update restrict;
create index ix_TestCase_testAssertion_14 on TestCase (test_assertion_id);
alter table TestCase add constraint fk_TestCase_owner_15 foreign key (owner_id) references Users (id) on delete restrict on update restrict;
create index ix_TestCase_owner_15 on TestCase (owner_id);
alter table TestGroup add constraint fk_TestGroup_owner_16 foreign key (owner_id) references Users (id) on delete restrict on update restrict;
create index ix_TestGroup_owner_16 on TestGroup (owner_id);
alter table TestRun add constraint fk_TestRun_job_17 foreign key (job_id) references Job (id) on delete restrict on update restrict;
create index ix_TestRun_job_17 on TestRun (job_id);
alter table TestRun add constraint fk_TestRun_runner_18 foreign key (runner_id) references Users (id) on delete restrict on update restrict;
create index ix_TestRun_runner_18 on TestRun (runner_id);
alter table TestRun add constraint fk_TestRun_history_19 foreign key (history_id) references UserHistory (id) on delete restrict on update restrict;
create index ix_TestRun_history_19 on TestRun (history_id);
alter table TokenAction add constraint fk_TokenAction_targetUser_20 foreign key (target_user_id) references Users (id) on delete restrict on update restrict;
create index ix_TokenAction_targetUser_20 on TokenAction (target_user_id);
alter table UserHistory add constraint fk_UserHistory_operationType_21 foreign key (operation_type_id) references OperationType (id) on delete restrict on update restrict;
create index ix_UserHistory_operationType_21 on UserHistory (operation_type_id);
alter table UtilClass add constraint fk_UtilClass_testGroup_22 foreign key (test_group_id) references TestGroup (id) on delete restrict on update restrict;
create index ix_UtilClass_testGroup_22 on UtilClass (test_group_id);
alter table UtilClass add constraint fk_UtilClass_owner_23 foreign key (owner_id) references Users (id) on delete restrict on update restrict;
create index ix_UtilClass_owner_23 on UtilClass (owner_id);
alter table Wrapper add constraint fk_Wrapper_user_24 foreign key (user_id) references Users (id) on delete restrict on update restrict;
create index ix_Wrapper_user_24 on Wrapper (user_id);
alter table WrapperParam add constraint fk_WrapperParam_tdl_25 foreign key (tdl_id) references Tdl (id) on delete restrict on update restrict;
create index ix_WrapperParam_tdl_25 on WrapperParam (tdl_id);
alter table WrapperVersion add constraint fk_WrapperVersion_wrapper_26 foreign key (wrapper_id) references Wrapper (id) on delete restrict on update restrict;
create index ix_WrapperVersion_wrapper_26 on WrapperVersion (wrapper_id);
