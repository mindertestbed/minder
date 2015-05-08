# --- !Ups
ALTER TABLE testgroup ALTER COLUMN description TYPE character varying(20480);
ALTER TABLE testrun ADD COLUMN number integer default 0;
# --- !Downs
ALTER TABLE testgroup ALTER COLUMN description TYPE character varying(1024);
ALTER TABLE testrun DROP COLUMN number;