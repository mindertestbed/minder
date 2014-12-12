package minder.iso_schematron_xslt2;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/**
 * Created by yerlibilgin on 12/12/14.
 */
public class SchematronClassResolver implements URIResolver {
  @Override
  public Source resolve(String href, String base) throws TransformerException {
    System.out.println(href);
    System.out.println(base);
    return new StreamSource(this.getClass().getResourceAsStream(href));
  }
}
