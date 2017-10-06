# --- !Ups

CREATE TABLE report_template (
  id              BIGSERIAL    NOT NULL,
  name            VARCHAR(255) NOT NULL,
  owner_id        BIGINT,
  html            BYTEA        NOT NULL,
  number          INTEGER,
  is_batch_report BOOLEAN,
  test_group_id   BIGINT,
  CONSTRAINT uq_report_template_name UNIQUE (name),
  CONSTRAINT uq_report_template_1 UNIQUE (name, test_group_id),
  CONSTRAINT pk_report_template PRIMARY KEY (id)
);

ALTER TABLE report_template
  ADD CONSTRAINT fk_report_template_owner_12 FOREIGN KEY (owner_id) REFERENCES Users (id);
CREATE INDEX ix_report_template_owner_12
  ON report_template (owner_id);
ALTER TABLE report_template
  ADD CONSTRAINT fk_report_template_testGroup_13 FOREIGN KEY (test_group_id) REFERENCES TestGroup (id);

# --- !Downs


drop table if exists report_template cascade;