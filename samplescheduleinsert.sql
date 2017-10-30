select id from abstract_job;
insert into job_schedule (test_group_id, name, short_description, owner_id, cron_expression) VALUES (1, 'job_schedulem 6','why short?', 1, '* 1 * * *');
INSERT into job_schedule_abstract_job (job_schedule_id, abstract_job_id) VALUES (currval('job_schedule_id_seq'::regclass), 1);
INSERT into job_schedule_abstract_job (job_schedule_id, abstract_job_id) VALUES (currval('job_schedule_id_seq'::regclass), 2);
INSERT into job_schedule_abstract_job (job_schedule_id, abstract_job_id) VALUES (currval('job_schedule_id_seq'::regclass), 3);

INSERT into job_schedule_test_suite (job_schedule_id, test_suite_id) VALUES (currval('job_schedule_id_seq'::regclass), 1);
INSERT into job_schedule_test_suite (job_schedule_id, test_suite_id) VALUES (currval('job_schedule_id_seq'::regclass), 2);
INSERT into job_schedule_test_suite (job_schedule_id, abstract_job_id) VALUES (currval('job_schedule_id_seq'::regclass), 3);

