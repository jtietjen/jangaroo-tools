/*
 * Copyright 2008 CoreMedia AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 */

package net.jangaroo.jooc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Andreas Gawecki
 */
public class CompilationUnit extends NodeImplBase {

  public PackageDeclaration getPackageDeclaration() {
    return packageDeclaration;
  }

  public ClassDeclaration getClassDeclaration() {
    return classDeclaration;
  }

  PackageDeclaration packageDeclaration;
  JooSymbol lBrace;
  Directives directives;
  ClassDeclaration classDeclaration;
  JooSymbol rBrace;


  protected File sourceFile;

  protected File destionationDir = null;
  protected File outputFile = null;
  protected JsWriter out;

  public CompilationUnit(PackageDeclaration packageDeclaration, JooSymbol lBrace, Directives directives, ClassDeclaration classDeclaration, JooSymbol rBrace) {
    this.packageDeclaration = packageDeclaration;
    this.lBrace = lBrace;
    this.directives = directives;
    this.classDeclaration = classDeclaration;
    this.rBrace = rBrace;
  }

  public void setDestionationDir(File destionationDir) {
    this.destionationDir = destionationDir;
  }

  public void setSourceFile(File sourceFile) {
    this.sourceFile = sourceFile;
  }

  public File getSourceFile() {
    return sourceFile;
  }

  protected File getOutputFile() {
    if (outputFile == null) {
      outputFile = new File(getOutputFileName());
    }
    return outputFile;
  }

  protected void createOutputDirs(File outputFile) {
    File parentDir = outputFile.getParentFile();
    if (!parentDir.exists() && !parentDir.mkdirs()) {
      Jooc.error("cannot create directories '" + parentDir.getAbsolutePath() + "'");
    }
  }

  public void writeOutput(boolean verbose, boolean debugKeepSource, boolean debugKeepLines, boolean enableAssertions) throws Jooc.CompilerError {
    File outFile = getOutputFile();
    String fileName = outFile.getName();
    String classPart = fileName.substring(0, fileName.lastIndexOf('.'));
    String className = classDeclaration.getName();
    if (!classPart.equals(className))
      Jooc.error(classDeclaration,
       "class name must be equal to file name: expected " + classPart + ", found " + className);
    createOutputDirs(outFile);
    try {
      if (verbose)
        System.out.println("writing file: '" + outFile.getAbsolutePath() + "'");
      out = new JsWriter(new FileWriter(outFile));
    } catch (IOException e) {
      Jooc.error("cannot open output file for writing: '" + outFile.getAbsolutePath() + "'", e);
    }
    out.setEnableAssertions(enableAssertions);
    out.setKeepLines(debugKeepLines);
    out.setKeepSource(debugKeepSource);
    try {
      generateCode(out);
      out.close();
    } catch (IOException e) {
      Jooc.error("error writing file: '" + outFile.getAbsolutePath() + "'", e);
      outFile.delete();
    }
  }

  public void generateCode(JsWriter out) throws IOException {
     out.write(Jooc.CLASS_FULLY_QUALIFIED_NAME + ".prepare(");
     packageDeclaration.generateCode(out);
     out.writeSymbolWhitespace(lBrace);
     if (directives!=null) {
       directives.generateCode(out);
     }
     classDeclaration.generateCode(out);
     out.writeSymbolWhitespace(rBrace);
     out.write(");");
  }

  public void analyze(AnalyzeContext context) {
    packageDeclaration.analyze(context);
    classDeclaration.analyze(context);
  }

  protected String[] getPackageName() {
    return packageDeclaration.getQualifiedName();
  }

  protected String getOutputFileName() {
    String result = "";
    if (destionationDir == null) {
      result = sourceFile.getParentFile().getAbsolutePath();
    } else {
      result = destionationDir.getAbsolutePath();
      String[] packageName = getPackageName();
      for (int i = 0; i < packageName.length; i++) {
        result += File.separator;
        result += new String(packageName[i]);
      }
    }
    result += File.separator;
    result += sourceFile.getName();
    int dotpos = result.lastIndexOf('.');
    if (dotpos >= 0)
      result = result.substring(0, dotpos);
    result += Jooc.JS_SUFFIX;
    return result;
  }

  public JooSymbol getSymbol() {
     return packageDeclaration.getSymbol();
  }

}