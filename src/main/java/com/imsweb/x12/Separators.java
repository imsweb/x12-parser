package com.imsweb.x12;

import java.util.Objects;

/**
 * The class represents an X12 separator definition. A separators object consists of a segment separator, element separator and a composite element separator.
 */
public class Separators {

    private Character _segment;
    private Character _element;
    private Character _composite;

    /**
     * Default constructor.
     */
    public Separators() {
        _segment = '~';
        _element = '*';
        _composite = ':';
    }

    /**
     * Constructor which takes the segment separator, element separator and composite element separator as input.
     * @param segment segment separator
     * @param element element separator
     * @param composite composite element separator
     */
    public Separators(Character segment, Character element, Character composite) {
        _segment = segment;
        _element = element;
        _composite = composite;
    }

    /**
     * Returns the composite element separator.
     * @return composite element separator
     */
    public Character getCompositeElement() {
        return _composite;
    }

    /**
     * Returns the element separator.
     * @return an element separator
     */
    public Character getElement() {
        return _element;
    }

    /**
     * Returns the segment separator.
     * @return a segment separator
     */
    public Character getSegment() {
        return _segment;
    }

    /**
     * Sets the composite element separator.
     * @param c the composite element separator.
     */
    public void setCompositeElement(Character c) {
        _composite = c;
    }

    /**
     * Sets the element separator.
     * @param e the element separator.
     */
    public void setElement(Character e) {
        _element = e;
    }

    /**
     * Sets the segment separator.
     * @param s the segment separator
     */
    public void setSegment(Character s) {
        _segment = s;
    }

    /**
     * Returns a <code>String</code> consisting of segment, element and
     * composite element separator.
     */
    public String toString() {
        return "[" + _segment + "," + _element + "," + _composite + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Separators that = (Separators)o;
        return Objects.equals(_segment, that._segment) &&
                Objects.equals(_element, that._element) &&
                Objects.equals(_composite, that._composite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_segment, _element, _composite);
    }

}
