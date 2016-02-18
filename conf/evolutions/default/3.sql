# --- !Ups
alter table abstract_job  add column test_group_id bigint;

alter table abstract_job add constraint fk_abstract_job_testGroup_1 foreign key (test_group_id) references TestGroup (id);
create index ix_abstract_job_testGroup_1 on abstract_job (test_group_id);

# --- !Downs
drop table if exists JobTemplate cascade;

