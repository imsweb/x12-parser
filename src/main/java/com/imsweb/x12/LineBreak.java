package com.imsweb.x12;

/**
 * Choice of line break when serializing x12 documents.
 */
public enum LineBreak {

  /**
   * Unix line endings.
   */
  LF("\n"),
  /**
   * Windows line endings.
   */
  CRLF("\r\n"),
  /**
   * No line breaks at all.
   */
  NONE("");

  private String lineBreakString;

  LineBreak(String lineBreakString) {
    this.lineBreakString = lineBreakString;
  }

  public String getLineBreakString() {
    return lineBreakString;
  }
}
