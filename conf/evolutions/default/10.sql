
# --- !Ups
ALTER TABLE testgroup ADD COLUMN dependency_string character varying(20480);

# --- !Downs
ALTER TABLE testgroup DROP COLUMN dependency_string;



