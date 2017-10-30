# --- !Ups

create table job_schedule (
  id                        bigserial not null,
  test_group_id             bigint,
  name                      varchar(255) not null,
  cron_expression          TEXT not null,
  short_description         TEXT,
  owner_id                  bigint,
  next_job_id               bigint,
  constraint uq_job_schedule_1 unique (test_group_id,name),
  constraint pk_job_schedule primary key (id))
;


create table job_schedule_abstract_job (
  job_schedule_id                bigint not null,
  abstract_job_id                bigint not null,
  constraint pk_job_schedule_abstract_job primary key (job_schedule_id, abstract_job_id))
;

create table job_schedule_test_suite (
  job_schedule_id                bigint not null,
  test_suite_id                bigint not null,
  constraint pk_job_schedule_test_suite primary key (job_schedule_id, test_suite_id))
;


alter table job_schedule add constraint fk_job_schedule_testGroup_8 foreign key (test_group_id) references TestGroup (id);
create index ix_job_schedule_testGroup_8 on job_schedule (test_group_id);
alter table job_schedule add constraint fk_job_schedule_owner_9 foreign key (owner_id) references Users (id);
create index ix_job_schedule_owner_9 on job_schedule (owner_id);
alter table job_schedule add constraint fk_job_schedule_nextJob_10 foreign key (next_job_id) references job_schedule (id);
create index ix_job_schedule_nextJob_10 on job_schedule (next_job_id);


alter table job_schedule_abstract_job add constraint fk_job_schedule_abstract_job__01 foreign key (job_schedule_id) references job_schedule (id);

alter table job_schedule_abstract_job add constraint fk_job_schedule_abstract_job__02 foreign key (abstract_job_id) references abstract_job (id);

alter table job_schedule_test_suite add constraint fk_job_schedule_test_suite__01 foreign key (job_schedule_id) references job_schedule (id);

alter table job_schedule_test_suite add constraint fk_job_schedule_test_suite__02 foreign key (test_suite_id) references testsuite (id);


# --- !Downs


DROP TABLE IF EXISTS job_schedule CASCADE;

DROP TABLE IF EXISTS job_schedule_abstract_job CASCADE;

DROP TABLE IF EXISTS job_schedule_test_suite CASCADE;

