# --- !Ups
ALTER TABLE testassertion ADD COLUMN owner_id bigint not null
  DEFAULT 1;
ALTER TABLE testassertion
  ADD CONSTRAINT fk_testassertion_owner_13 FOREIGN KEY (owner_id)
      REFERENCES users (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE testcase ADD COLUMN owner_id bigint not null
  DEFAULT 1;
ALTER TABLE testcase
ADD CONSTRAINT fk_testcase_owner_14 FOREIGN KEY (owner_id)
REFERENCES users (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE job ADD COLUMN owner_id bigint not null
  DEFAULT 1;
ALTER TABLE job
ADD CONSTRAINT fk_job_owner_15 FOREIGN KEY (owner_id)
REFERENCES users (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION;




# --- !Downs
ALTER TABLE testassertion DROP COLUMN owner_id;
ALTER TABLE testassertion
DROP CONSTRAINT fk_testassertion_owner_13;

ALTER TABLE testcase DROP COLUMN owner_id;
ALTER TABLE testcase
DROP CONSTRAINT fk_testcase_owner_14;

ALTER TABLE job DROP COLUMN owner_id;
ALTER TABLE job
DROP CONSTRAINT fk_job_owner_15;
