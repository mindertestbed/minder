# --- !Ups

alter table TestAsset rename group_id to test_group_id;

alter table Job rename to abstract_job;

ALTER TABLE abstract_job ADD COLUMN _type integer;
ALTER TABLE abstract_job ALTER COLUMN _type SET NOT NULL;
ALTER TABLE abstract_job ADD COLUMN test_suite_id bigint;

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

alter table abstract_job add constraint fk_abstract_job_testSuite_3 foreign key (test_suite_id) references TestSuite (id);
create index ix_abstract_job_testSuite_3 on abstract_job (test_suite_id);

alter table GitbEndpoint add constraint fk_GitbEndpoint_WrapperVersion_5 foreign key (wrapper_version_id) references WrapperVersion (id);
create index ix_GitbEndpoint_WrapperVersion_5 on GitbEndpoint (wrapper_version_id);
alter table GitbParameter add constraint fk_GitbParameter_GitbEndpoint_6 foreign key (gitb_endpoint_id) references GitbEndpoint (id);
create index ix_GitbParameter_GitbEndpoint_6 on GitbParameter (gitb_endpoint_id);
alter table TestSuite add constraint fk_TestSuite_owner_23 foreign key (owner_id) references Users (id);
create index ix_TestSuite_owner_23 on TestSuite (owner_id);
alter table TestSuite add constraint fk_TestSuite_testGroup_24 foreign key (test_group_id) references TestGroup (id);
create index ix_TestSuite_testGroup_24 on TestSuite (test_group_id);
alter table UserAuthentication add constraint fk_UserAuthentication_user_25 foreign key (user_id) references Users (id);
create index ix_UserAuthentication_user_25 on UserAuthentication (user_id);

# --- !Downs


alter table TestAsset rename test_group_id to group_id;

ALTER TABLE abstract_job DROP CONSTRAINT fk_abstract_job_testsuite_3;
ALTER TABLE abstract_job DROP COLUMN test_suite_id;
ALTER TABLE abstract_job DROP COLUMN _type;
alter table abstract_job rename to Job;

drop table if exists GitbEndpoint cascade;

drop table if exists GitbParameter cascade;

drop table if exists TestSuite cascade;

drop table if exists UserAuthentication cascade;

