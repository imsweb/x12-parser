package com.imsweb.x12;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The class represents an X12 separator definition. A separators object consists of a segment separator, element separator and a composite element separator.
 */
public class Separators {

    public static final String HTML_ID_SEPARATOR = "__";

    private Character _segment;
    private Character _element;
    private Character _composite;
    private Pattern _segmentPattern;
    private Pattern _elementPattern;
    private Pattern _compositePattern;
    private LineBreak _lineBreak;

    /**
     * Default constructor.
     */
    public Separators() {
        setSegment('~');
        setElement('*');
        setCompositeElement(':');
        setLineBreak(LineBreak.NONE);
    }

    /**
     * Constructor which takes the segment separator, element separator and composite element separator as input.
     * @param segment segment separator
     * @param element element separator
     * @param composite composite element separator
     */
    public Separators(Character segment, Character element, Character composite) {
        setSegment(segment);
        setElement(element);
        setCompositeElement(composite);
        setLineBreak(LineBreak.NONE);
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

    public String[] splitElement(String line) {
        return (line != null && _elementPattern != null) ? _elementPattern.split(line) : null;
    }

    public String[] splitSegment(String line) {
        return (line != null && _segmentPattern != null) ? _segmentPattern.split(line) : null;
    }

    public String[] splitComposite(String line) {
        return (line != null && _compositePattern != null) ? _compositePattern.split(line) : null;
    }

    public LineBreak getLineBreak() {
        return _lineBreak;
    }

    public void setLineBreak(LineBreak lineBreak) {
        this._lineBreak = lineBreak;
    }

    /**
     * This method produces an ID string from a list of IDs that is used when creating
     * HTML contents from the x12 file.
     * @param idList List of IDs of all parents and current id.
     * @return An ID string with all the parent ids separated by HTML_ID_SEPARATOR.
     */
    public static String getIdString(List<String> idList) {
        StringBuilder sb = new StringBuilder();
        for (String id : idList) {
            if (sb.length() != 0) {
                sb.append(HTML_ID_SEPARATOR);
            }
            sb.append(id);
        }
        return sb.toString();
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
