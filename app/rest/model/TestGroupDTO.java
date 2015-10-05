package rest.model;

import models.TestGroup;

/**
 * @author: yerlibilgin
 * @date: 23/09/15.
 */
public class TestGroupDTO extends BaseDTO {
  public String name;

  public static TestGroupDTO dto2model(TestGroup testGroup) {
    TestGroupDTO dto = new TestGroupDTO();
    //BaseDTO.dto2model(dto, testGroup);
    return null;
  }
}
