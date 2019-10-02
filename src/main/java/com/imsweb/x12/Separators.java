package com.imsweb.x12;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The class represents an X12 separator definition. A separators object consists of a segment separator, element separator and a composite element separator.
 */
public class Separators {

    private Character _segment;
    private Character _element;
    private Character _composite;
    private Pattern _segmentPattern;
    private Pattern _elementPattern;
    private Pattern _compositePattern;

    /**
     * Default constructor.
     */
    public Separators() {
        _segment = '~';
        _element = '*';
        _composite = ':';
        setSeparatorPatterns(_segment, _element, _composite);
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
        setSeparatorPatterns(_segment, _element, _composite);
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
        _compositePattern = c == null ? null : Pattern.compile(Pattern.quote(c.toString()));
    }

    /**
     * Sets the element separator.
     * @param e the element separator.
     */
    public void setElement(Character e) {
        _element = e;
        _elementPattern = e == null ? null : Pattern.compile(Pattern.quote(e.toString()));
    }

    /**
     * Sets the segment separator.
     * @param s the segment separator
     */
    public void setSegment(Character s) {
        _segment = s;
        _segmentPattern = s == null ? null : Pattern.compile(Pattern.quote(s.toString()));
    }

    /**
     * Returns a <code>String</code> consisting of segment, element and
     * composite element separator.
     */
    public String toString() {
        return "[" + _segment + "," + _element + "," + _composite + "]";
    }

    private void setSeparatorPatterns(Character segment, Character element, Character composite) {
        if (segment != null)
            _segmentPattern = Pattern.compile(Pattern.quote(segment.toString()));

        if (element != null)
            _elementPattern = Pattern.compile(Pattern.quote(element.toString()));

        if (composite != null)
            _compositePattern = Pattern.compile(Pattern.quote(composite.toString()));
    }

    public String[] splitElement(String line) {
        return (line != null && _elementPattern != null) ? _elementPattern.split(line) : null;
    }

    public String[] splitSegment(String line) {
        return (line != null && _segmentPattern != null) ? _segmentPattern.split(line) : null;
    }

    public String[] splitComposite(String line) {
        return (line != null && _compositePattern != null) ? _compositePattern.split(line) : null;
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
