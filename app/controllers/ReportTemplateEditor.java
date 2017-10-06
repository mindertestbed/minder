package controllers;

import play.data.validation.Constraints;

/**
 * @author: yerlibilgin
 * @date: 25/09/17.
 */
public class ReportTemplateEditor {
  @Constraints.Required
  public String template;
  @Constraints.Required
  public String name;
  public int number;
  public long groupId;
  public long id;
  public boolean isBatchReport;

  public ReportTemplateEditor() {

  }
}
