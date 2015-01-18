package builtin.reportEngine;

import org.w3c.dom.Document;

public class Report {

	public Report() {
		reportModel = new ReportModel();
	}

	public ReportModel reportModel;

	public Document templateDomDocument;

	public String templateXml;

	public ReportModel getReportModel() {
		return reportModel;
	}

	public void setReportModel(ReportModel reportModel) {
		this.reportModel = reportModel;
	}

	public Document getTemplateDomDocument() {
		return templateDomDocument;
	}

	public void setTemplateDomDocument(Document templateDomDocument) {
		this.templateDomDocument = templateDomDocument;
	}

	public String getTemplateXml() {
		return templateXml;
	}

	public void setTemplateXml(String templateXml) {
		this.templateXml = templateXml;
	}

}
