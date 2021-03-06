/*
 * Copyright (c) 2009, CoreMedia AG, Hamburg. All rights reserved.
 */
package net.jangaroo.extxml.mojo;

import net.jangaroo.extxml.file.SrcFileScanner;
import net.jangaroo.extxml.generation.JooClassGenerator;
import net.jangaroo.extxml.generation.XsdGenerator;
import net.jangaroo.extxml.model.ComponentSuite;
import net.jangaroo.extxml.xml.XsdScanner;
import net.jangaroo.utils.log.Log;
import net.jangaroo.utils.log.LogHandler;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Mojo to compile Jangaroo sources during the compile phase.
 *
 * @goal extxml
 * @phase generate-sources
 * @requiresDependencyResolution
 */
public class ExtXmlMojo extends AbstractMojo {

  /**
   * The maven project.
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * Source directory to scan for files to compile.
   *
   * @parameter expression="${basedir}/src/main/joo"
   */
  private File sourceDirectory;

  /**
   * The namespace of the component suite
   *
   * @parameter expression="${project.artifactId}"
   */
  private String namespace;

  /**
   * The namespace of the component suite
   *
   * @parameter expression="${project.artifactId}"
   */
  private String namespacePrefix;

  /**
   * Output directory for all ActionScript3 files generated out of exml components
   *
   * @parameter expression="${project.build.directory}/generated-sources/joo"
   */
  private File generatedSourcesDirectory;

  /**
   * The XSD Schema that will be generated for this component suite
   *
   * @parameter expression="${project.artifactId}.xsd"
   */
  private String xsd;

  /**
   * The XSD Schema that will be generated for this component suite
   *
   * @parameter expression="${project.build.directory}/generated-resources"
   */
  private File generatedResourcesDirectory;

  /**
   * The XSD Schema that will be generated for this component suite
   *
   * @parameter
   */
  private File[] importedXsds;

  /**
   * @component
   */
  MavenProjectHelper projectHelper;

  public void execute() throws MojoExecutionException, MojoFailureException {

    if (!generatedSourcesDirectory.exists()) {
      getLog().info("generating sources into: " + generatedSourcesDirectory.getPath());
      getLog().debug("created " + generatedSourcesDirectory.mkdirs());
    }
    if (!generatedResourcesDirectory.exists()) {
      getLog().info("generating resources into: " + generatedResourcesDirectory.getPath());
      getLog().debug("created " + generatedResourcesDirectory.mkdirs());
    }


    MavenLogHandler errorHandler = new MavenLogHandler();
    Log.setLogHandler(errorHandler);
    ComponentSuite suite = new ComponentSuite(namespace, namespacePrefix, sourceDirectory, generatedSourcesDirectory);
    XsdScanner xsdScanner = new XsdScanner();

    if (importedXsds != null) {
      for (File importedXsd : importedXsds) {
        try {
          suite.addImportedComponentSuite(xsdScanner.scan(new FileInputStream(importedXsd)));
        } catch (IOException e) {
          throw new MojoExecutionException("Error while xsd scanning", e);
        }
      }
    }

    Set<Artifact> dependencies = project.getDependencyArtifacts();

    for (Artifact dependency : dependencies) {
      if (!dependency.isOptional() && "jangaroo".equals(dependency.getType()) && dependency.getFile() != null && dependency.getFile().getName().endsWith(".jar")) {
        ZipFile zipArtifact = null;
        try {
          zipArtifact = new ZipFile(dependency.getFile(), ZipFile.OPEN_READ);
          Enumeration<? extends ZipEntry> entries = zipArtifact.entries();
          while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".xsd")) {
              getLog().info(String.format("Loading %s from %s", zipEntry.getName(), dependency.getFile().getAbsolutePath()));
              InputStream stream = new BufferedInputStream(zipArtifact.getInputStream(zipEntry));
              suite.addImportedComponentSuite(xsdScanner.scan(stream));
            }
          }
        } catch (IOException e) {
          throw new MojoExecutionException("Error while xsd scanning", e);
        } finally {
          try {
            if (zipArtifact != null) {
              zipArtifact.close();
            }
          } catch (IOException e) {
            throw new MojoExecutionException("Error while xsd scanning", e);
          }
        }
      }
    }


    SrcFileScanner fileScanner = new SrcFileScanner(suite);
    try {
      fileScanner.scan();
    } catch (IOException e) {
      throw new MojoExecutionException("Error while file scanning", e);
    }

    //Generate JSON out of the xml compontents, complete the data in those ComponentClasses

    JooClassGenerator generator = new JooClassGenerator(suite);
    generator.generateClasses();

    if (errorHandler.lastException != null) {
      throw new MojoExecutionException(errorHandler.exceptionMsg, errorHandler.lastException);
    }

    StringBuffer errorsMsgs = new StringBuffer();
    for (String msg : errorHandler.errors) {
      errorsMsgs.append(msg);
      errorsMsgs.append("\n");
    }

    if (errorsMsgs.length() != 0) {
      throw new MojoFailureException(errorsMsgs.toString());
    }


    for (String msg : errorHandler.warnings) {
      getLog().warn(msg);
    }


    //generate the XSD for that
    if (!suite.getComponentClasses().isEmpty()) {
      Writer out = null;
      try {
        //generate the XSD for that
        File xsdFile = new File(generatedResourcesDirectory, xsd);
        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(xsdFile), "UTF-8"));
        new XsdGenerator(suite).generateXsd(out);
        out.close();
        projectHelper.attachArtifact(project, "xsd", xsdFile );        

      } catch (IOException e) {
        throw new MojoExecutionException("Error while generating XML schema", e);
      } finally {
        if (out != null) {
          try {
            out.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }

    project.addCompileSourceRoot(generatedSourcesDirectory.getPath());
  }

  class MavenLogHandler implements LogHandler {
    ArrayList<String> errors = new ArrayList<String>();
    ArrayList<String> warnings = new ArrayList<String>();
    Exception lastException;
    String exceptionMsg;
    File currentFile;

    public void setCurrentFile(File file) {
      this.currentFile = file;
    }

    public void error(String message, int lineNumber, int columnNumber) {
      errors.add(String.format("ERROR in %s, line %s, column %s: %s", currentFile, lineNumber, columnNumber, message));
    }

    public void error(String message, Exception exception) {
      this.exceptionMsg = message;
      if (currentFile != null) {
        this.exceptionMsg += String.format(" in file: %s", currentFile);
      }
      this.lastException = exception;
    }

    public void error(String message) {
      errors.add(message);
    }

    public void warning(String message) {
      warnings.add(message);
    }

    public void warning(String message, int lineNumber, int columnNumber) {
      warnings.add(String.format("WARNING in %s, line %s, column %s: %s", currentFile, lineNumber, columnNumber, message));
    }

    public void info(String message) {
      getLog().info(message);
    }

    public void debug(String message) {
      getLog().debug(message);
    }
  }
}
