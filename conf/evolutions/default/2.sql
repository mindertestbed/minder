# --- !Ups
ALTER TABLE runconfiguration RENAME TO job;
ALTER TABLE testrun RENAME COLUMN run_configuration_id TO job_id;
ALTER TABLE mappedwrapper RENAME COLUMN run_configuration_id TO job_id;
ALTER SEQUENCE runconfiguration_seq RENAME TO job_seq;

# --- !Downs
ALTER TABLE job RENAME TO runconfiguration;
ALTER TABLE testrun RENAME COLUMN job_id TO run_configuration_id;
ALTER TABLE mappedwrapper RENAME COLUMN job_id TO run_configuration_id;
ALTER SEQUENCE job_seq RENAME TO runconfiguration_seq;