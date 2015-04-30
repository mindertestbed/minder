# --- !Ups
ALTER TABLE testassertion ALTER COLUMN description TYPE character varying(20480);
ALTER TABLE testassertion ALTER COLUMN prerequisites TYPE character varying(20480);
ALTER TABLE testassertion ALTER COLUMN variables TYPE character varying(1024);
ALTER TABLE testassertion ALTER COLUMN tag TYPE character varying(1024);
ALTER TABLE testassertion ALTER COLUMN predicate TYPE character varying(20480);
ALTER TABLE testassertion ALTER COLUMN normative_source TYPE character varying(20480);
# --- !Downs
ALTER TABLE testassertion ALTER COLUMN description TYPE character varying(1024);
ALTER TABLE testassertion ALTER COLUMN prerequisites TYPE character varying(250);
ALTER TABLE testassertion ALTER COLUMN variables TYPE character varying(250);
ALTER TABLE testassertion ALTER COLUMN tag TYPE character varying(250);
ALTER TABLE testassertion ALTER COLUMN predicate TYPE character varying(255);
ALTER TABLE testassertion ALTER COLUMN normative_source TYPE character varying(250);
