package net.jangaroo.extxml.generation;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.core.Environment;
import net.jangaroo.utils.log.Log;
import net.jangaroo.extxml.model.ComponentSuite;

import java.io.IOException;
import java.io.Writer;

/**
 * An XsdGenerator takes a {@link ComponentSuite} and (re)generates its XML Schema (.xsd) file.
 */
public final class XsdGenerator {

  private final static String outputCharset = "UTF-8";
  private static Configuration cfg = new Configuration();
  static {
    /* Create and adjust freemarker configuration */
    cfg.setClassForTemplateLoading(XsdGenerator.class, "/net/jangaroo/extxml/templates");
    cfg.setObjectWrapper(new DefaultObjectWrapper());
    cfg.setOutputEncoding("UTF-8");
  }

  private ComponentSuite componentSuite;

  /**
   * 
   * @param componentSuite
   */
  public XsdGenerator(ComponentSuite componentSuite) {    
    this.componentSuite = componentSuite;
  }

  public void generateXsd(Writer out) throws IOException {
    //do nothing if suite is empty
    if (!componentSuite.getComponentClasses().isEmpty()) {
      /* Get or create a template */
      Template template = null;
      try {
        template = cfg.getTemplate("component-suite-xsd.ftl");
      } catch (IOException e) {
        Log.e("Could not read xsd template", e);
      }

      /* Merge data-model with template */
      if (template != null) {
        Log.i(String.format("Writing XML Schema '%s' ", componentSuite.getNamespace()));
        try {
          Environment env = template.createProcessingEnvironment(componentSuite, out);
          env.setOutputEncoding(outputCharset);
          env.process();
        } catch (TemplateException e) {
          Log.e("Error while generating xsd", e);
        }
      }
    }
  }
}
