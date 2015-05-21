# --- !Ups
ALTER TABLE testcase ADD COLUMN isutil boolean DEFAULT false;
ALTER TABLE userhistory DROP CONSTRAINT fk_userhistory_user_17;
ALTER TABLE userhistory DROP COLUMN user_id;
ALTER TABLE userhistory ADD COLUMN email character varying(255) DEFAULT  'root@minder';
# --- !Downs
ALTER TABLE testcase DROP COLUMN istuil;
ALTER TABLE userhistory DROP COLUMN email;
ALTER TABLE userhistory ADD COLUMN user_id bigint;
ALTER TABLE userhistory ADD CONSTRAINT fk_userhistory_user_17
    FOREIGN KEY (user_id)
    REFERENCES users (id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION;

