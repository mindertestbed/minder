# --- !Ups
ALTER TABLE testgroup ALTER COLUMN description TYPE character varying(20480);
# --- !Downs
ALTER TABLE testgroup ALTER COLUMN description TYPE character varying(1024);