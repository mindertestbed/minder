# --- !Ups
ALTER TABLE testassertion ALTER COLUMN variables TYPE VARCHAR(20480) USING variables :: VARCHAR(20480);
ALTER TABLE testrun ALTER COLUMN error_message TYPE BYTEA USING error_message :: BYTEA;
ALTER TABLE testsuite ADD COLUMN visibility INTEGER DEFAULT 0;
ALTER TABLE testsuite ADD COLUMN preemption_policy INTEGER DEFAULT 0;
ALTER TABLE testsuite DROP COLUMN description;
ALTER TABLE abstract_job add column is_obsolete BOOLEAN DEFAULT FALSE ;

# --- !Downs
ALTER TABLE abstract_job DROP COLUMN is_obsolete;
ALTER TABLE testsuite ADD COLUMN description TEXT;
ALTER TABLE testsuite DROP COLUMN visibility;
ALTER TABLE testsuite DROP COLUMN preemption_policy;
ALTER TABLE testassertion ALTER COLUMN variables TYPE VARCHAR(255) USING variables :: VARCHAR(255);
ALTER TABLE testrun ALTER COLUMN error_message TYPE VARCHAR(255) USING error_message :: BYTEA;
