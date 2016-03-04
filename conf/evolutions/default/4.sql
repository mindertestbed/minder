# --- !Ups
ALTER TABLE testassertion ALTER COLUMN variables TYPE VARCHAR(20480) USING variables :: VARCHAR(20480);
ALTER TABLE testrun ALTER COLUMN error_message TYPE BYTEA USING error_message :: BYTEA;
ALTER TABLE testsuite ADD COLUMN visibility INTEGER DEFAULT 0;
ALTER TABLE testsuite ADD COLUMN preemption_policy INTEGER DEFAULT 0;
ALTER TABLE testsuite DROP COLUMN description;
ALTER TABLE abstract_job add column is_obsolete BOOLEAN DEFAULT FALSE ;

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

ALTER TABLE testrun ADD COLUMN suite_run_id bigint;

ALTER TABLE SuiteRun add constraint fk_SuiteRun_testSuite_12 foreign key (test_suite_id) references TestSuite (id) ON DELETE CASCADE ON UPDATE CASCADE ;
create index ix_SuiteRun_testSuite_12 on SuiteRun (test_suite_id);
ALTER TABLE SuiteRun add constraint fk_SuiteRun_runner_13 foreign key (runner_id) references Users (id);
create index ix_SuiteRun_runner_13 on SuiteRun (runner_id);
ALTER TABLE TestRun add constraint fk_TestRun_suiteRun_24 foreign key (suite_run_id) references SuiteRun (id) ON DELETE CASCADE ON UPDATE CASCADE ;
create index ix_TestRun_suiteRun_24 on TestRun (suite_run_id);

alter table abstract_job
  drop constraint fk_abstract_job_testSuite_3,
  add constraint fk_abstract_job_testSuite_3 foreign key (test_suite_id) references TestSuite (id) ON DELETE CASCADE ON UPDATE CASCADE ;

alter table testrun
  drop constraint fk_testrun_job_20,
  add constraint fk_testrun_job_20 foreign key (job_id) references abstract_job (id) ON DELETE CASCADE ON UPDATE CASCADE ;

# --- !Downs

alter table testrun
drop constraint fk_testrun_job_20,
add constraint fk_testrun_job_20 foreign key (job_id) references abstract_job (id);


alter table abstract_job
  drop constraint fk_abstract_job_testSuite_3,
  add constraint fk_abstract_job_testSuite_3 foreign key (test_suite_id) references TestSuite (id);

ALTER TABLE testrun DROP COLUMN suite_run_id;
DROP  TABLE if exists SuiteRun cascade;
ALTER TABLE abstract_job DROP COLUMN is_obsolete;
ALTER TABLE testsuite ADD COLUMN description TEXT;
ALTER TABLE testsuite DROP COLUMN visibility;
ALTER TABLE testsuite DROP COLUMN preemption_policy;
ALTER TABLE testassertion ALTER COLUMN variables TYPE VARCHAR(255) USING variables :: VARCHAR(255);
ALTER TABLE testrun ALTER COLUMN error_message TYPE VARCHAR(255) USING error_message :: BYTEA;
