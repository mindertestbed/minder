select u.email, tc.name, tg.name, tcase.*  from Users u, TestCaseCategory tc, TestCaseGroup tg, TestCase tcase
where
u.email = 'myildiz83@gmail.com' and tcase.test_case_group_id = tg.id and tg.test_case_category_id = tc.id and tc.owner_id = u.id
order by tcase.id ASC