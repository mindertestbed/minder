# --- !Ups
ALTER TABLE abstract_job ALTER COLUMN mtdl_parameters TYPE VARCHAR(20480) USING mtdl_parameters::VARCHAR(20480);

# --- !Downs
ALTER TABLE abstract_job ALTER COLUMN mtdl_parameters TYPE VARCHAR(255) USING mtdl_parameters::VARCHAR(255);
