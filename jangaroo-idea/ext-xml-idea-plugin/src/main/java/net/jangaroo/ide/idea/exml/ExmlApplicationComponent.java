package net.jangaroo.ide.idea.exml;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.javaee.ExternalResourceManager;
import org.jetbrains.annotations.NotNull;

/**
 * The EXML plugin's global configuration.
 * Adds XML Schemata for EXML 0.1.
 */
public class ExmlApplicationComponent implements ApplicationComponent {

  static final String EXML_NAMESPACE_URI = "http://net.jangaroo.com/extxml/0.1";

  @NotNull
  public String getComponentName() {
    return "exmlGlobals";
  }

  public void initComponent() {
    FileTypeManager ftm = FileTypeManager.getInstance();
    ftm.registerFileType(ftm.getFileTypeByExtension("xml"), "exml");
    ExternalResourceManager erm = ExternalResourceManager.getInstance();
    erm.addStdResource(EXML_NAMESPACE_URI, "/net/jangaroo/extxml/schemas/extxml.xsd", getClass());
//import com.intellij.psi.filters.AndFilter;
//import com.intellij.psi.filters.ClassFilter;
//import com.intellij.psi.filters.position.TargetNamespaceFilter;
//import com.intellij.psi.meta.MetaDataRegistrar;
//import com.intellij.psi.xml.XmlDocument;
//    MetaDataRegistrar.getInstance().registerMetaData(
//      new AndFilter(
//        new ClassFilter(XmlDocument.class),
//        new TargetNamespaceFilter(EXML_NAMESPACE_URI)
//      ),
//      XmlNSDescriptor.class
//    );
  }

  public void disposeComponent() {
    ExternalResourceManager erm = ExternalResourceManager.getInstance();
    erm.removeResource(EXML_NAMESPACE_URI, "/net/jangaroo/extxml/schemas/extxml.xsd");
  }
}
