package com.imsweb.x12;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.imsweb.x12.mapping.CompositeDefinition;
import com.imsweb.x12.mapping.ElementDefinition;
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

    public Separators getSeparators() {
        return _separators;
    }

    public List<String> getSubValues() {
        return _subValues;
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
        String[] values = _separators.splitComposite(value);
        if (values != null && values.length >= 1)
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

    /**
     * Converts this element to HTML segment.
     * @param elementDefinition The element definition that defines this element.
     * @param parentIds The parent IDs thus far.
     * @return Html segment representing this element.
     */
    public String toHtml(Optional<ElementDefinition> elementDefinition, List<String> parentIds) {

        ArrayList<String> newParentIds = new ArrayList<>();
        newParentIds.addAll(parentIds);
        newParentIds.add(getId());

        StringBuilder dump = new StringBuilder()
            .append("<div class=\"x12-element\"><p><span class=\"x12-element-name\">");

        if (elementDefinition.isPresent()) {
            dump.append(elementDefinition.get().getName())
                .append(" (")
                .append(_id)
                .append("): ");
        }
        else {
            dump.append(_id)
                .append(": ");
        }
        return dump
            .append("</span> <input type=\"text\" name=\"")
            .append(Separators.getIdString(newParentIds))
            .append("\" value=\"")
            .append(_value)
            .append("\" /> </p></div>")
            .toString();
    }

    public Map<String, Object> toMap(Optional<ElementDefinition> elementDefinition, Optional<CompositeDefinition> compositeDefinition, List<String> parentIds) {
        Map<String, Object> elmMap = new HashMap<>();
        elmMap.put("parentIds", parentIds);
        elmMap.put("xid", _id);
        elmMap.put("type", elementDefinition.isPresent() ? "element" : "composite");
        elmMap.put("value", _value);
        if (elementDefinition.isPresent()) {
            elmMap.put("name", elementDefinition.get().getName());
        }
        else {
            compositeDefinition.ifPresent(definition -> {
                List<Map<String, Object>> subElements = new ArrayList<>();
                List<String> subParentIds = new ArrayList<>(parentIds);
                subParentIds.add(_id);
                elmMap.put("name", definition.getName());
                for (int i = 0; i < getNumOfSubElements(); ++i) {
                    String subElementVal = getSubElement(i);
                    ElementDefinition subElementDefinition = definition.getElements().get(i);
                    Map<String, Object> compositeMap = new HashMap<>();
                    compositeMap.put("name", subElementDefinition.getName());
                    compositeMap.put("parentIds", subParentIds);
                    compositeMap.put("xid", subElementDefinition.getXid());
                    compositeMap.put("type", "compositeValue");
                    compositeMap.put("value", subElementVal);
                    subElements.add(compositeMap);
                }
                elmMap.put("subElements", subElements);
            });
        }
        return elmMap;
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
