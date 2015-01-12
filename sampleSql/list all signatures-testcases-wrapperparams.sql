SELECT 
  testcase.id, 
  testcase.name, 
  wrapperparam.id, 
  wrapperparam.name, 
  paramsignature.id, 
  paramsignature.name, 
  paramsignature.wrapper_param_id, 
  wrapperparam.test_case_id
FROM 
  public.testcase, 
  public.wrapperparam, 
  public.paramsignature
WHERE 
  testcase.id = wrapperparam.test_case_id AND
  wrapperparam.id = paramsignature.wrapper_param_id;
