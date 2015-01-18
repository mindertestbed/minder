package builtin.reportEngine;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Edona
 * 
 *         Class that serves the purpose of parsing a Report Schema and
 *         producing a Report Model. The Report Schema is in XML format. It
 *         defines the possible layout of the report. The Report Model is the
 *         representative of the Report Schema in Java format. Parses unknown
 *         xml structure.
 */

public class ReportParser {

	public static Document parseTemplate(byte[] template)
			throws ParserConfigurationException, SAXException, IOException {
		// Get Document Builder
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		// Build Document
		Document document = builder.parse(new ByteArrayInputStream(template));

		// Normalize the XML Structure; It's just too important !!
		document.getDocumentElement().normalize();

		return document;
	}

	public static NodeList getNodeList(Document document) {
		Element root = document.getDocumentElement();
		NodeList nList = document.getElementsByTagName(root.getNodeName());
		return nList;
	}

	private Node searchDocForField(NodeList nList, String field) {

		System.out.println("main");
		Node foundNode = null;
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node node = nList.item(temp);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				System.out.println("1");
				System.out.println(node.getNodeName());
				System.out.println("2");
				if (node.getNodeName().equals(field)) {
					System.out.println("Node Name = " + node.getNodeName()
							+ "; Value = " + node.getTextContent());

					System.out.println("3");
					System.out.println("3" + node.getNodeName());
					return node;

				}
				// Check all attributes
				if (node.hasAttributes()) {
					// get attributes names and values
					NamedNodeMap nodeMap = node.getAttributes();
					for (int i = 0; i < nodeMap.getLength(); i++) {
						Node tempNode = nodeMap.item(i);
						System.out.println("4");
						if (tempNode.getNodeName().equals(field)) {
							System.out.println("5");
							System.out.println("Attr name : "
									+ tempNode.getNodeName() + "; Value = "
									+ tempNode.getNodeValue());

							System.out.println("5:" + node.getNodeName());

						}
					}
				}
				if (node.hasChildNodes()) {
					System.out.println("6");
					// We got more childs; Let's visit them as well
					foundNode = searchDocForField(node.getChildNodes(), field);
					System.out.println("6:" + foundNode.getNodeName());
					if (foundNode != null)
						return foundNode;

				}
			}

		}

		return foundNode;
	}

	public static Node recursiveFindNode(Node node, String field) {

		if (node.getNodeName() == field) {
			return node;
		}

		if ("#text" == node.getNodeName())
			return null;
		Node fNode = null;

		if (node.getNodeType() == Node.ELEMENT_NODE) {
			if (node.hasChildNodes()) {
				// We got more childs; Let's visit them as well
				int childSize = node.getChildNodes().getLength();
				for (int m = 0; m < childSize; m++) {

					fNode = recursiveFindNode(node.getChildNodes().item(m),
							field);
					if (null != fNode)
						return fNode;
				}

			}

			if (node.hasAttributes()) {
				// get attributes names and values
				NamedNodeMap nodeMap = node.getAttributes();
				for (int i = 0; i < nodeMap.getLength(); i++) {
					Node tempNode = nodeMap.item(i);
					if (tempNode.getNodeName().equals(field)) {
						return node;

					}
				}
			}
		}
		return fNode;

	}

}
