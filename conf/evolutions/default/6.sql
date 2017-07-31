# --- !Ups

ALTER TABLE TestRun ADD COLUMN status INTEGER;
ALTER TABLE TestRun ADD COLUMN  finishDate timestamp;
UPDATE testrun set status=3 where success=true;
UPDATE testrun set status=4 where success=false;
UPDATE testrun set finishDate=date;
ALTER TABLE TestRun DROP column success;
ALTER TABLE TestRun ADD CONSTRAINT ck_TestRun_status CHECK (status IN (0, 1, 2, 3, 4));

# --- !Downs

ALTER TABLE TestRun DROP CONSTRAINT ck_TestRun_status;
ALTER TABLE testrun ADD COLUMN success BOOLEAN NOT NULL DEFAULT false;
UPDATE testrun set success=true where status=3;
ALTER TABLE TestRun DROP COLUMN status;
ALTER TABLE TestRun DROP COLUMN  finishDate;
