SELECT 
  wrapperparam.name, 
  wrapperparam.test_case_id, 
  paramsignature.name, 
  testcase.name, testcase.id
FROM 
  public.testcase, 
  public.wrapperparam, 
  public.paramsignature
WHERE 
  testcase.id = wrapperparam.test_case_id AND
  wrapperparam.id = paramsignature.wrapper_param_id AND
  testcase.id = 2 AND
  wrapperparam.name in (select wp.name from wrapperparam wp where wp.test_case_id=1);