# --- !Ups
ALTER TABLE testsuite DROP CONSTRAINT uq_TestSuite_name;
ALTER TABLE testsuite ADD constraint uq_TestSuite_name UNIQUE (test_group_id, name);
ALTER TABLE testassertion DROP CONSTRAINT uq_testassertion_ta_id;
ALTER TABLE testassertion ADD CONSTRAINT uq_testassertion_ta_id UNIQUE (test_group_id, ta_id);
ALTER TABLE testcase DROP CONSTRAINT uq_testcase_name;
ALTER TABLE testcase ADD CONSTRAINT uq_testcase_name UNIQUE (test_assertion_id, name);
# --- !Downs
ALTER TABLE testsuite DROP CONSTRAINT uq_TestSuite_name;
ALTER TABLE testsuite ADD CONSTRAINT uq_TestSuite_name UNIQUE (name);
ALTER TABLE testassertion DROP CONSTRAINT uq_testassertion_ta_id;
ALTER TABLE testassertion ADD CONSTRAINT uq_testassertion_ta_id UNIQUE (ta_id);
ALTER TABLE testcase DROP CONSTRAINT uq_testcase_name;
ALTER TABLE testcase ADD CONSTRAINT uq_testcase_name UNIQUE (name);
