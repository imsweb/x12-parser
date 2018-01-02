package com.imsweb.x12;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * This class represents an X12 Element.
 */
@XStreamAlias("element")
public class Element {

    @XStreamOmitField
    private Separators _separators;
    @XStreamAlias("id")
    private String _id;
    @XStreamAlias("value")
    private String _value;
    @XStreamAlias("subValue")
    private List<String> _subValues = new ArrayList<>();

    /**
     * Constructor
     * @param id an element identifier
     * @param value an element value
     * @param separators separators to use
     */
    public Element(String id, String value, Separators separators) {
        if (id == null || id.isEmpty())
            throw new IllegalStateException("Elements must have a non-null identifier");

        _separators = separators;
        setId(id);
        setValue(value == null ? "" : value);
    }

    /**
     * Constructor
     * @param id an element identifier
     * @param value an element value
     */
    public Element(String id, String value) {
        if (id == null || id.isEmpty())
            throw new IllegalStateException("Elements must have a non-null identifier");

        _separators = new Separators();
        setId(id);
        setValue(value == null ? "" : value);
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        String values[] = value.split(Pattern.quote(_separators.getCompositeElement().toString()));
        if (values.length >= 1)
            _subValues.addAll(Arrays.asList(values));

        _value = value;
    }

    public int getNumOfSubElements() {
        return _subValues.size();
    }

    public String getSubElement(int index) {
        if (index < _subValues.size())
            return _subValues.get(index);

        return null;
    }

    @Override
    public String toString() {
        return _value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Element element = (Element)o;
        return Objects.equals(_id, element._id) &&
                Objects.equals(_value, element._value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, _value);
    }

}
