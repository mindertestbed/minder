# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table abstract_job (
  _type                     integer not null,
  id                        bigserial not null,
  name                      varchar(255) not null,
  tdl_id                    bigint,
  owner_id                  bigint,
  report_template_id        bigint,
  visibility                integer,
  http_endpoint             varchar(255),
  mtdl_parameters           TEXT,
  test_group_id             bigint,
  test_suite_id             bigint,
  constraint ck_abstract_job_visibility check (visibility in (0,1,2)),
  constraint uq_abstract_job_report_template_ unique (report_template_id),
  constraint uq_abstract_job_http_endpoint unique (http_endpoint),
  constraint pk_abstract_job primary key (id))
;

create table Adapter (
  id                        bigserial not null,
  NAME                      varchar(255),
  short_description         TEXT not null,
  description               TEXT,
  user_id                   bigint,
  constraint uq_Adapter_NAME unique (NAME),
  constraint pk_Adapter primary key (id))
;

create table AdapterParam (
  id                        bigserial not null,
  name                      varchar(255) not null,
  tdl_id                    bigint,
  constraint pk_AdapterParam primary key (id))
;

create table AdapterVersion (
  id                        bigserial not null,
  adapter_id                bigint,
  version                   varchar(255) not null,
  creation_date             timestamp,
  constraint pk_AdapterVersion primary key (id))
;

create table dbrole (
  id                        bigserial not null,
  user_id                   bigint,
  role                      integer,
  constraint ck_dbrole_role check (role in ('0','2','1')),
  constraint pk_dbrole primary key (id))
;

create table end_point_identifier (
  method                    varchar(255),
  identifier                varchar(255),
  tdl_id                    bigint)
;

create table GitbEndpoint (
  id                        bigserial not null,
  adapter_version_id        bigint not null,
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

create table job_schedule (
  id                        bigserial not null,
  test_group_id             bigint,
  name                      varchar(255) not null,
  cron_expression           varchar(255) not null,
  short_description         TEXT,
  owner_id                  bigint,
  next_job_id               bigint,
  constraint uq_job_schedule_1 unique (test_group_id,name),
  constraint pk_job_schedule primary key (id))
;

create table MappedAdapter (
  id                        bigserial not null,
  parameter_id              bigint,
  adapter_version_id        bigint,
  job_id                    bigint,
  constraint pk_MappedAdapter primary key (id))
;

create table ParamSignature (
  id                        bigserial not null,
  name                      varchar(255),
  adapter_param_id          bigint,
  constraint pk_ParamSignature primary key (id))
;

create table report_template (
  id                        bigserial not null,
  name                      varchar(255) not null,
  owner_id                  bigint,
  html                      bytea not null,
  number                    integer,
  is_batch_report           boolean,
  test_group_id             bigint,
  constraint uq_report_template_1 unique (test_group_id,name),
  constraint pk_report_template primary key (id))
;

create table SuiteRun (
  id                        bigserial not null,
  test_suite_id             bigint,
  date                      timestamp,
  runner_id                 bigint,
  number                    integer,
  visibility                integer,
  constraint ck_SuiteRun_visibility check (visibility in (0,1,2)),
  constraint pk_SuiteRun primary key (id))
;

create table TSignal (
  id                        bigserial not null,
  adapter_version_id        bigint,
  signature                 varchar(255),
  constraint pk_TSignal primary key (id))
;

create table TSlot (
  id                        bigserial not null,
  adapter_version_id        bigint,
  signature                 varchar(255),
  constraint pk_TSlot primary key (id))
;

create table Tdl (
  id                        bigserial not null,
  test_case_id              bigint,
  tdl                       TEXT not null,
  version                   varchar(255) not null,
  creation_date             timestamp,
  is_http_endpoint          boolean,
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
  variables                 varchar(20480),
  tag                       varchar(1024),
  description               TEXT,
  short_description         TEXT not null,
  prescription_level        integer,
  owner_id                  bigint,
  constraint ck_TestAssertion_prescription_level check (prescription_level in (0,1,2)),
  constraint uq_TestAssertion_1 unique (test_group_id,ta_id),
  constraint pk_TestAssertion primary key (id))
;

create table TestAsset (
  id                        bigserial not null,
  test_group_id             bigint,
  name                      varchar(255) not null,
  short_description         TEXT,
  description               TEXT,
  constraint uq_TestAsset_1 unique (test_group_id,name),
  constraint pk_TestAsset primary key (id))
;

create table TestCase (
  id                        bigserial not null,
  test_assertion_id         bigint,
  name                      varchar(255) not null,
  owner_id                  bigint,
  constraint uq_TestCase_1 unique (test_assertion_id,name),
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
  suite_run_id              bigint,
  date                      timestamp,
  finishdate                timestamp,
  runner_id                 bigint,
  history_id                bigint,
  report_metadata           bytea,
  sut_names                 TEXT,
  status                    integer not null,
  number                    integer,
  error_message             TEXT,
  visibility                integer,
  constraint ck_TestRun_status check (status in (0,1,2,3,4)),
  constraint ck_TestRun_visibility check (visibility in (0,1,2)),
  constraint uq_TestRun_history_id unique (history_id),
  constraint pk_TestRun primary key (id))
;

create table TestSuite (
  id                        bigserial not null,
  test_group_id             bigint,
  name                      varchar(255) not null,
  short_description         TEXT not null,
  mtdl_parameters           TEXT,
  owner_id                  bigint,
  visibility                integer,
  preemption_policy         integer,
  constraint ck_TestSuite_visibility check (visibility in (0,1,2)),
  constraint ck_TestSuite_preemption_policy check (preemption_policy in (0,1)),
  constraint uq_TestSuite_1 unique (test_group_id,name),
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
  constraint uq_UserAuthentication_user_id unique (user_id),
  constraint pk_UserAuthentication primary key (id))
;

create table UserHistory (
  id                        bigserial not null,
  email                     varchar(255),
  optype                    integer,
  SYSLOG                    bytea,
  constraint ck_UserHistory_optype check (optype in (0,1,2)),
  constraint pk_UserHistory primary key (id))
;

create table UtilClass (
  id                        bigserial not null,
  test_group_id             bigint,
  name                      varchar(255) not null,
  short_description         TEXT not null,
  source                    TEXT not null,
  owner_id                  bigint,
  constraint uq_UtilClass_1 unique (test_group_id,name),
  constraint pk_UtilClass primary key (id))
;


create table job_schedule_abstract_job (
  job_schedule_id                bigint not null,
  abstract_job_id                bigint not null,
  constraint pk_job_schedule_abstract_job primary key (job_schedule_id, abstract_job_id))
;

create table job_schedule_TestSuite (
  job_schedule_id                bigint not null,
  TestSuite_id                   bigint not null,
  constraint pk_job_schedule_TestSuite primary key (job_schedule_id, TestSuite_id))
;
alter table abstract_job add constraint fk_abstract_job_tdl_1 foreign key (tdl_id) references Tdl (id);
create index ix_abstract_job_tdl_1 on abstract_job (tdl_id);
alter table abstract_job add constraint fk_abstract_job_owner_2 foreign key (owner_id) references Users (id);
create index ix_abstract_job_owner_2 on abstract_job (owner_id);
alter table abstract_job add constraint fk_abstract_job_reportTemplate_3 foreign key (report_template_id) references report_template (id);
create index ix_abstract_job_reportTemplate_3 on abstract_job (report_template_id);
alter table abstract_job add constraint fk_abstract_job_testGroup_4 foreign key (test_group_id) references TestGroup (id);
create index ix_abstract_job_testGroup_4 on abstract_job (test_group_id);
alter table abstract_job add constraint fk_abstract_job_testSuite_5 foreign key (test_suite_id) references TestSuite (id);
create index ix_abstract_job_testSuite_5 on abstract_job (test_suite_id);
alter table Adapter add constraint fk_Adapter_user_6 foreign key (user_id) references Users (id);
create index ix_Adapter_user_6 on Adapter (user_id);
alter table AdapterParam add constraint fk_AdapterParam_tdl_7 foreign key (tdl_id) references Tdl (id);
create index ix_AdapterParam_tdl_7 on AdapterParam (tdl_id);
alter table AdapterVersion add constraint fk_AdapterVersion_adapter_8 foreign key (adapter_id) references Adapter (id);
create index ix_AdapterVersion_adapter_8 on AdapterVersion (adapter_id);
alter table dbrole add constraint fk_dbrole_user_9 foreign key (user_id) references Users (id);
create index ix_dbrole_user_9 on dbrole (user_id);
alter table end_point_identifier add constraint fk_end_point_identifier_tdl_10 foreign key (tdl_id) references Tdl (id);
create index ix_end_point_identifier_tdl_10 on end_point_identifier (tdl_id);
alter table GitbEndpoint add constraint fk_GitbEndpoint_AdapterVersio_11 foreign key (adapter_version_id) references AdapterVersion (id);
create index ix_GitbEndpoint_AdapterVersio_11 on GitbEndpoint (adapter_version_id);
alter table GitbParameter add constraint fk_GitbParameter_GitbEndpoint_12 foreign key (gitb_endpoint_id) references GitbEndpoint (id);
create index ix_GitbParameter_GitbEndpoint_12 on GitbParameter (gitb_endpoint_id);
alter table job_schedule add constraint fk_job_schedule_testGroup_13 foreign key (test_group_id) references TestGroup (id);
create index ix_job_schedule_testGroup_13 on job_schedule (test_group_id);
alter table job_schedule add constraint fk_job_schedule_owner_14 foreign key (owner_id) references Users (id);
create index ix_job_schedule_owner_14 on job_schedule (owner_id);
alter table job_schedule add constraint fk_job_schedule_nextJob_15 foreign key (next_job_id) references job_schedule (id);
create index ix_job_schedule_nextJob_15 on job_schedule (next_job_id);
alter table MappedAdapter add constraint fk_MappedAdapter_parameter_16 foreign key (parameter_id) references AdapterParam (id);
create index ix_MappedAdapter_parameter_16 on MappedAdapter (parameter_id);
alter table MappedAdapter add constraint fk_MappedAdapter_adapterVersi_17 foreign key (adapter_version_id) references AdapterVersion (id);
create index ix_MappedAdapter_adapterVersi_17 on MappedAdapter (adapter_version_id);
alter table MappedAdapter add constraint fk_MappedAdapter_job_18 foreign key (job_id) references abstract_job (id);
create index ix_MappedAdapter_job_18 on MappedAdapter (job_id);
alter table ParamSignature add constraint fk_ParamSignature_adapterPara_19 foreign key (adapter_param_id) references AdapterParam (id);
create index ix_ParamSignature_adapterPara_19 on ParamSignature (adapter_param_id);
alter table report_template add constraint fk_report_template_owner_20 foreign key (owner_id) references Users (id);
create index ix_report_template_owner_20 on report_template (owner_id);
alter table report_template add constraint fk_report_template_testGroup_21 foreign key (test_group_id) references TestGroup (id);
create index ix_report_template_testGroup_21 on report_template (test_group_id);
alter table SuiteRun add constraint fk_SuiteRun_testSuite_22 foreign key (test_suite_id) references TestSuite (id);
create index ix_SuiteRun_testSuite_22 on SuiteRun (test_suite_id);
alter table SuiteRun add constraint fk_SuiteRun_runner_23 foreign key (runner_id) references Users (id);
create index ix_SuiteRun_runner_23 on SuiteRun (runner_id);
alter table TSignal add constraint fk_TSignal_adapterVersion_24 foreign key (adapter_version_id) references AdapterVersion (id);
create index ix_TSignal_adapterVersion_24 on TSignal (adapter_version_id);
alter table TSlot add constraint fk_TSlot_adapterVersion_25 foreign key (adapter_version_id) references AdapterVersion (id);
create index ix_TSlot_adapterVersion_25 on TSlot (adapter_version_id);
alter table Tdl add constraint fk_Tdl_testCase_26 foreign key (test_case_id) references TestCase (id);
create index ix_Tdl_testCase_26 on Tdl (test_case_id);
alter table TestAssertion add constraint fk_TestAssertion_testGroup_27 foreign key (test_group_id) references TestGroup (id);
create index ix_TestAssertion_testGroup_27 on TestAssertion (test_group_id);
alter table TestAssertion add constraint fk_TestAssertion_owner_28 foreign key (owner_id) references Users (id);
create index ix_TestAssertion_owner_28 on TestAssertion (owner_id);
alter table TestAsset add constraint fk_TestAsset_testGroup_29 foreign key (test_group_id) references TestGroup (id);
create index ix_TestAsset_testGroup_29 on TestAsset (test_group_id);
alter table TestCase add constraint fk_TestCase_testAssertion_30 foreign key (test_assertion_id) references TestAssertion (id);
create index ix_TestCase_testAssertion_30 on TestCase (test_assertion_id);
alter table TestCase add constraint fk_TestCase_owner_31 foreign key (owner_id) references Users (id);
create index ix_TestCase_owner_31 on TestCase (owner_id);
alter table TestGroup add constraint fk_TestGroup_owner_32 foreign key (owner_id) references Users (id);
create index ix_TestGroup_owner_32 on TestGroup (owner_id);
alter table TestRun add constraint fk_TestRun_job_33 foreign key (job_id) references abstract_job (id);
create index ix_TestRun_job_33 on TestRun (job_id);
alter table TestRun add constraint fk_TestRun_suiteRun_34 foreign key (suite_run_id) references SuiteRun (id);
create index ix_TestRun_suiteRun_34 on TestRun (suite_run_id);
alter table TestRun add constraint fk_TestRun_runner_35 foreign key (runner_id) references Users (id);
create index ix_TestRun_runner_35 on TestRun (runner_id);
alter table TestRun add constraint fk_TestRun_history_36 foreign key (history_id) references UserHistory (id);
create index ix_TestRun_history_36 on TestRun (history_id);
alter table TestSuite add constraint fk_TestSuite_testGroup_37 foreign key (test_group_id) references TestGroup (id);
create index ix_TestSuite_testGroup_37 on TestSuite (test_group_id);
alter table TestSuite add constraint fk_TestSuite_owner_38 foreign key (owner_id) references Users (id);
create index ix_TestSuite_owner_38 on TestSuite (owner_id);
alter table UserAuthentication add constraint fk_UserAuthentication_user_39 foreign key (user_id) references Users (id);
create index ix_UserAuthentication_user_39 on UserAuthentication (user_id);
alter table UtilClass add constraint fk_UtilClass_testGroup_40 foreign key (test_group_id) references TestGroup (id);
create index ix_UtilClass_testGroup_40 on UtilClass (test_group_id);
alter table UtilClass add constraint fk_UtilClass_owner_41 foreign key (owner_id) references Users (id);
create index ix_UtilClass_owner_41 on UtilClass (owner_id);



alter table job_schedule_abstract_job add constraint fk_job_schedule_abstract_job__01 foreign key (job_schedule_id) references job_schedule (id);

alter table job_schedule_abstract_job add constraint fk_job_schedule_abstract_job__02 foreign key (abstract_job_id) references abstract_job (id);

alter table job_schedule_TestSuite add constraint fk_job_schedule_TestSuite_job_01 foreign key (job_schedule_id) references job_schedule (id);

alter table job_schedule_TestSuite add constraint fk_job_schedule_TestSuite_Tes_02 foreign key (TestSuite_id) references TestSuite (id);

# --- !Downs

drop table if exists abstract_job cascade;

drop table if exists Adapter cascade;

drop table if exists AdapterParam cascade;

drop table if exists AdapterVersion cascade;

drop table if exists dbrole cascade;

drop table if exists end_point_identifier cascade;

drop table if exists GitbEndpoint cascade;

drop table if exists GitbParameter cascade;

drop table if exists job_schedule cascade;

drop table if exists job_schedule_abstract_job cascade;

drop table if exists job_schedule_TestSuite cascade;

drop table if exists MappedAdapter cascade;

drop table if exists ParamSignature cascade;

drop table if exists report_template cascade;

drop table if exists SuiteRun cascade;

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

