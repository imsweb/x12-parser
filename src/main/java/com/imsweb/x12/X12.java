package com.imsweb.x12;

/**
 * The X12 class is the object representation of an ANSI X12
 * transaction. The building block of an X12 transaction is an element. Some
 * elements may be made of sub elements. Elements combine to form segments.
 * Segments are grouped as loops. And a set of loops form an X12 transaction.
 */
public class X12 extends Loop {

    /**
     * Default constructor uses the default separators
     */
    public X12() {
        super("X12");
    }

    /**
     * The constructor takes a separators object.
     * @param separators a Separators object
     */
    public X12(Separators separators) {
        super(separators, "X12");
    }
}
