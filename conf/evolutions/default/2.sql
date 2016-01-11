# --- !Ups

ALTER TABLE TestAsset RENAME group_id TO test_group_id;

ALTER TABLE Job RENAME TO abstract_job;

ALTER TABLE abstract_job ADD COLUMN _type INTEGER;
ALTER TABLE abstract_job ALTER COLUMN _type SET NOT NULL;
ALTER TABLE abstract_job ADD COLUMN test_suite_id BIGINT;

ALTER TABLE TestCase DROP COLUMN short_description;
ALTER TABLE TestCase DROP COLUMN description;

CREATE TABLE GitbEndpoint (
  id                 BIGSERIAL    NOT NULL,
  wrapper_version_id BIGINT       NOT NULL,
  name               VARCHAR(255) NOT NULL,
  description        TEXT,
  CONSTRAINT pk_GitbEndpoint PRIMARY KEY (id)
);

CREATE TABLE GitbParameter (
  id               BIGSERIAL    NOT NULL,
  gitb_endpoint_id BIGINT       NOT NULL,
  name             VARCHAR(255) NOT NULL,
  value            VARCHAR(255) NOT NULL,
  use              INTEGER,
  kind             INTEGER,
  description      TEXT,
  CONSTRAINT ck_GitbParameter_use CHECK (use IN (0, 1)),
  CONSTRAINT ck_GitbParameter_kind CHECK (kind IN (0, 1)),
  CONSTRAINT pk_GitbParameter PRIMARY KEY (id)
);


CREATE TABLE TestSuite (
  id                BIGSERIAL    NOT NULL,
  name              VARCHAR(255) NOT NULL,
  short_description TEXT         NOT NULL,
  description       TEXT,
  mtdl_parameters   VARCHAR(255),
  owner_id          BIGINT,
  test_group_id     BIGINT,
  CONSTRAINT uq_TestSuite_name UNIQUE (name),
  CONSTRAINT pk_TestSuite PRIMARY KEY (id)
);


CREATE TABLE UserAuthentication (
  id              BIGSERIAL NOT NULL,
  user_id         BIGINT,
  realm           VARCHAR(255),
  server_nonce    VARCHAR(255),
  issue_time      TIMESTAMP,
  expiry_time     TIMESTAMP,
  request_counter INTEGER,
  CONSTRAINT uq_UserAuthentication_user_id UNIQUE (user_id),
  CONSTRAINT pk_UserAuthentication PRIMARY KEY (id)
);

ALTER TABLE abstract_job ADD CONSTRAINT fk_abstract_job_testSuite_3 FOREIGN KEY (test_suite_id) REFERENCES TestSuite (id);
CREATE INDEX ix_abstract_job_testSuite_3 ON abstract_job (test_suite_id);

ALTER TABLE GitbEndpoint ADD CONSTRAINT fk_GitbEndpoint_WrapperVersion_5 FOREIGN KEY (wrapper_version_id) REFERENCES WrapperVersion (id);
CREATE INDEX ix_GitbEndpoint_WrapperVersion_5 ON GitbEndpoint (wrapper_version_id);
ALTER TABLE GitbParameter ADD CONSTRAINT fk_GitbParameter_GitbEndpoint_6 FOREIGN KEY (gitb_endpoint_id) REFERENCES GitbEndpoint (id);
CREATE INDEX ix_GitbParameter_GitbEndpoint_6 ON GitbParameter (gitb_endpoint_id);
ALTER TABLE TestSuite ADD CONSTRAINT fk_TestSuite_owner_23 FOREIGN KEY (owner_id) REFERENCES Users (id);
CREATE INDEX ix_TestSuite_owner_23 ON TestSuite (owner_id);
ALTER TABLE TestSuite ADD CONSTRAINT fk_TestSuite_testGroup_24 FOREIGN KEY (test_group_id) REFERENCES TestGroup (id);
CREATE INDEX ix_TestSuite_testGroup_24 ON TestSuite (test_group_id);
ALTER TABLE UserAuthentication ADD CONSTRAINT fk_UserAuthentication_user_25 FOREIGN KEY (user_id) REFERENCES Users (id);
CREATE INDEX ix_UserAuthentication_user_25 ON UserAuthentication (user_id);

# --- !Downs


ALTER TABLE TestAsset RENAME test_group_id TO group_id;

ALTER TABLE abstract_job DROP CONSTRAINT fk_abstract_job_testsuite_3;
ALTER TABLE abstract_job DROP COLUMN test_suite_id;
ALTER TABLE abstract_job DROP COLUMN _type;
ALTER TABLE abstract_job RENAME TO Job;

DROP TABLE IF EXISTS GitbEndpoint CASCADE;

DROP TABLE IF EXISTS GitbParameter CASCADE;

DROP TABLE IF EXISTS TestSuite CASCADE;

DROP TABLE IF EXISTS UserAuthentication CASCADE;


ALTER TABLE TestCase ADD COLUMN short_description TEXT NOT NULL DEFAULT " ";

ALTER TABLE TestCase ADD COLUMN description TEXT;