# --- !Ups
ALTER TABLE testsuite ADD COLUMN visibility INTEGER default 0;
ALTER TABLE testsuite ADD COLUMN preemption_policy INTEGER default 0;

# --- !Downs
ALTER TABLE testsuite DROP COLUMN visibility;
ALTER TABLE testsuite DROP COLUMN preemption_policy;



