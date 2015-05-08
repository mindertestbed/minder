# --- !Ups
ALTER TABLE testrun ADD COLUMN status integer not null DEFAULT 2;
ALTER TABLE testrun ADD COLUMN progress_data bytea DEFAULT null;
# --- !Downs
ALTER TABLE testrun DROP COLUMN status;
ALTER TABLE testrun DROP COLUMN progress_data;
