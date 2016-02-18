# --- !Ups
create table JobTemplate (
  id                        bigserial not null,
  name                      varchar(255) not null,
  test_group_id             bigint not null,
  owner_id                  bigint,
  visibility                integer,
  mtdl_parameters           varchar(255),
  constraint ck_JobTemplate_visibility check (visibility in (0,1,2)),
  constraint pk_JobTemplate primary key (id))
;

alter table JobTemplate add constraint fk_JobTemplate_testGroup_1 foreign key (test_group_id) references TestGroup (id);
create index ix_JobTemplate_testGroup_1 on JobTemplate (test_group_id);
alter table JobTemplate add constraint fk_JobTemplate_job_owner_2 foreign key (owner_id) references Users (id);
create index ix_JobTemplate_job_owner_2 on JobTemplate (owner_id);


# --- !Downs
drop table if exists JobTemplate cascade;

