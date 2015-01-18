package builtin.reportEngine;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class that models the Report Schema Report ID Header : Text, Date, Time,
 * Name, Email
 * 
 * The root object of the report
 */

public class ReportModel {

	public int ReportID;

	public HashMap<String, String> Header;

	public String Title;

	public HashMap<String, String> Body;

	public ReportModel() {
		super();
		Header = new HashMap<String, String>();
		Body = new HashMap<String, String>();
	}

	public ReportModel(int reportID) {
		super();
		ReportID = reportID;
	}

	public ArrayList<String> Footer;

	public int getReportID() {
		return ReportID;
	}

	public void setReportID(int reportID) {
		ReportID = reportID;
	}

	public HashMap<String, String> getHeader() {
		return Header;
	}

	public void setHeader(HashMap<String, String> header) {
		Header = header;
	}

	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public HashMap<String, String> getBody() {
		return Body;
	}

	public void setBody(HashMap<String, String> body) {
		Body = body;
	}

	public ArrayList<String> getFooter() {
		return Footer;
	}

	public void setFooter(ArrayList<String> footer) {
		Footer = footer;
	}

}
