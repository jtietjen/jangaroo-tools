/*
 *   Copyright (c) 2003 CoreMedia AG, Hamburg. All rights reserved.
 */

package com.coremedia.jscc;


/**
 * @author Andreas Gawecki
 */
class BreakStatement extends LabelRefStatement {

  public BreakStatement(JscSymbol symBreak, Ide optIde, JscSymbol symSemicolon) {
    super(symBreak, optIde, symSemicolon);
  }

}