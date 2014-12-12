package assets.iso_schematron_xslt2;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

/**
 * Created by yerlibilgin on 12/12/14.
 */
public class SchematronClassResolver implements URIResolver {
  @Override
  public Source resolve(String href, String base) throws TransformerException {
    return new StreamSource(this.getClass().getResourceAsStream(href));
  }

  public InputStream rstrm(String s) {
    return this.getClass().getResourceAsStream(s);
  }
}
