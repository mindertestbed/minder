
# --- !Ups
CREATE TABLE utilclass
(
  id bigint NOT NULL,
  test_group_id bigint,
  name character varying(255) NOT NULL,
  short_description character varying(100) NOT NULL,
  source character varying(20480) NOT NULL,
  owner_id bigint NOT NULL DEFAULT 1,
  CONSTRAINT pk_utilclass PRIMARY KEY (id),
  CONSTRAINT fk_utilclass_group_1 FOREIGN KEY (test_group_id)
  REFERENCES testgroup (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_utilclass_owner_2 FOREIGN KEY (owner_id)
  REFERENCES users (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uq_utilclass_name UNIQUE (name)
)
WITH (
OIDS=FALSE
);
ALTER TABLE utilclass OWNER TO minderlord;

ALTER TABLE testcase DROP COLUMN istuil;

# --- !Downs
DROP TABLE utilclass;
ALTER TABLE testcase ADD COLUMN isutil boolean DEFAULT false;
