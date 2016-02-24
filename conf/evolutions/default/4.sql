# --- !Ups
ALTER TABLE testassertion ALTER COLUMN variables TYPE VARCHAR(20480) USING variables::VARCHAR(20480);
ALTER TABLE testrun ALTER COLUMN error_message TYPE bytea USING error_message::bytea;

# --- !Downs
ALTER TABLE testassertion ALTER COLUMN variables TYPE VARCHAR(255) USING variables::VARCHAR(255);
ALTER TABLE testrun ALTER COLUMN error_message TYPE VARCHAR(255) USING error_message::bytea;
