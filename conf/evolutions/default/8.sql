
# --- !Ups
ALTER TABLE testasset ADD COLUMN group_id bigint;
ALTER TABLE testasset ADD CONSTRAINT fk_testasset_group_id_1 FOREIGN KEY (group_id)
REFERENCES testgroup (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION;

# --- !Downs
ALTER TABLE testasset DROP COLUMN group_id;
ALTER TABLE testasset DROP CONSTRAINT fk_testasset_group_id_1;
