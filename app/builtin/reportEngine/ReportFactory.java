package builtin.reportEngine;

public class ReportFactory {

	public ReportModel createReportModel(int reportID) {
		return new ReportModel(reportID);
	}

	public ReportParser createReportParser() {
		return new ReportParser();
	}

}
