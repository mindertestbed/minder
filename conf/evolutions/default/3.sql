# --- !Ups

ALTER TABLE abstract_job ADD COLUMN visibility INTEGER NOT NULL DEFAULT 0;
ALTER TABLE testrun ADD COLUMN visibility INTEGER NOT NULL DEFAULT 0;
ALTER TABLE abstract_job DROP CONSTRAINT uq_job_name;

# --- !Downs

ALTER TABLE abstract_job DROP COLUMN visibility;
ALTER TABLE testrun DROP COLUMN visibility;
ALTER TABLE abstract_job ADD CONSTRAINT uq_job_name UNIQUE(name);