/*
 *   Copyright (c) 2003 CoreMedia AG, Hamburg. All rights reserved.
 */

package com.coremedia.jscc;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Andreas Gawecki
 */
public class ClassDeclaration extends IdeDeclaration {

  protected JscSymbol symClass;
  protected Extends optExtends;
  private Set privateMembers = new HashSet();

  public Extends getOptExtends() {
    return optExtends;
  }

  protected Implements optImplements;

  public ClassBody getBody() {
    return body;
  }

  public MethodDeclaration getConstructor() {
    return constructor;
  }

  protected ClassBody body;

  protected MethodDeclaration constructor = null;

  public MethodDeclaration getConstructorDeclaration() {
    return constructor;
  }

  public ClassDeclaration(JscSymbol[] modifiers, JscSymbol cls, Ide ide, Extends ext, Implements impl, ClassBody body) {
    super(modifiers,
            MODIFIER_ABSTRACT|MODIFIER_FINAL|MODIFIERS_SCOPE|MODIFIER_STATIC,
            ide);
    this.symClass = cls;
    this.optExtends = ext;
    this.optImplements = impl;
    this.body = body;
    body.classDeclaration = this;
  }

  // valid after analyze phase
  public PackageDeclaration getPackageDeclaration() {
    return (PackageDeclaration) getParentDeclaration();
  }

  public String getName() {
    return ide.getName();
  }

  public void setConstructor(MethodDeclaration methodDeclaration) {
     if (constructor != null)
       Jscc.error(methodDeclaration, "Only one constructor allowed per class");
//     if (methodDeclaration != body.declararations.get(0))
//       Jscc.error(methodDeclaration, "Constructor declaration must be the first declaration in a class");
     constructor = methodDeclaration;
  }

  public void generateCode(JsWriter out) throws IOException {
    out.writeSymbolWhitespace(symClass);
    if (!writeRuntimeModifiersUnclosed(out)) {
      out.write("\"");
    }
    out.writeSymbolToken(symClass);
    ide.generateCode(out);
    if (optExtends != null) optExtends.generateCode(out);
    //if (optImplements != null) optImplements.generateCode(out);
    out.write("\",");
    out.write("function($jooPublic,$jooPrivate){with(");
    getPackageDeclaration().ide.generateCode(out);
    out.write(")with($jooPublic)with($jooPrivate)return[");
    body.generateCode(out);
    out.write("]}");
  }

  public void analyze(AnalyzeContext context) {
    parentDeclaration = context.getScope().getPackageDeclaration();
    context.enterScope(this);
    if (optExtends != null) optExtends.analyze(context);
    if (optImplements != null) optImplements.analyze(context);
    body.analyze(context);
    context.leaveScope(this);
    computeModifiers();
  }

  public void registerPrivateMember(Ide ide) {
    privateMembers.add(ide.getName());
  }

  public boolean isPrivateMember(String memberName) {
    return privateMembers.contains(memberName);
  }

  public Type getSuperClassType() {
    return optExtends != null
      ? optExtends.superClass
      : new IdeType(new Ide(new JscSymbol(sym.IDE,  "", -1, -1, "", "Object")));
  }

  public String getSuperClassPath() {
    Type type = getSuperClassType();
    //TODO: scope class declarations, implement getSuperClassDeclaration()
    IdeType ideType = (IdeType) type;
    return toPath(ideType.getIde().getQualifiedName());
  }
}