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

package net.jangaroo.jooc.ast;

import net.jangaroo.jooc.AnalyzeContext;
import net.jangaroo.jooc.JooSymbol;
import net.jangaroo.jooc.JsWriter;
import net.jangaroo.jooc.Scope;

import java.io.IOException;

/**
 * @author Andreas Gawecki
 */
public class ParenthesizedExpr<E extends Expr> extends Expr {

  private JooSymbol lParen;
  private E expr;
  private JooSymbol rParen;

  public ParenthesizedExpr(JooSymbol lParen, E expr, JooSymbol rParen) {
    this.setlParen(lParen);
    this.setExpr(expr);
    this.setrParen(rParen);
  }

  @Override
  public void visit(AstVisitor visitor) throws IOException {
    visitor.visitParenthesizedExpr(this);
  }

  @Override
  public void scope(final Scope scope) {
    if (getExpr() !=null) {
      getExpr().scope(scope);
    }
  }

  public void analyze(AstNode parentNode, AnalyzeContext context) {
    super.analyze(parentNode, context);
    if (getExpr() !=null)
      getExpr().analyze(this, context);
  }

  public void generateJsCode(JsWriter out) throws IOException {
    throw new UnsupportedOperationException();
  }

  public JooSymbol getSymbol() {
    return getLParen();
  }

  public JooSymbol getLParen() {
    return lParen;
  }

  public void setlParen(JooSymbol lParen) {
    this.lParen = lParen;
  }

  public E getExpr() {
    return expr;
  }

  public void setExpr(E expr) {
    this.expr = expr;
  }

  public JooSymbol getRParen() {
    return rParen;
  }

  public void setrParen(JooSymbol rParen) {
    this.rParen = rParen;
  }
}