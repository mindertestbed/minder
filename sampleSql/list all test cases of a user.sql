select u.email, tg.name, ta.ta_id, tc.*  from Users u, TestCaseGroup tg, TestAssertion ta, TestCase tc
where
u.email = 'myildiz83@gmail.com' and tg.owner_id = u.id and ta.test_group_id = tg.id and tc.test_assertion_Id = ta.id
order by tc.id ASC