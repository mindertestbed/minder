# --- !Ups

ALTER TABLE userhistory ADD COLUMN optype INTEGER DEFAULT 2;
ALTER TABLE userhistory DROP COLUMN operation_type_id;
ALTER TABLE userhistory ADD CONSTRAINT ck_optype CHECK (optype IN (0, 1, 2));
DROP TABLE operationtype;

# --- !Downs

ALTER TABLE userhistory DROP CONSTRAINT ck_optype;
ALTER TABLE userhistory DROP COLUMN optype;

create table OperationType (
  id                        bigserial not null,
  OPERATION_TYPE            varchar(16),
  constraint ck_OperationType_OPERATION_TYPE check (OPERATION_TYPE in ('CREATE_TEST_CASE','EDIT_TEST_CASE','RUN_TEST_CASE')),
  constraint pk_OperationType primary key (id))
;


ALTER TABLE userhistory ADD COLUMN operation_type_id INTEGER DEFAULT 0;

alter table UserHistory add constraint fk_UserHistory_operationType_26 foreign key (operation_type_id) references OperationType (id);
create index ix_UserHistory_operationType_26 on UserHistory (operation_type_id);
