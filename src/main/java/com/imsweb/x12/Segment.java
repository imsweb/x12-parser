package com.imsweb.x12;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * This class represents an X12 segment.
 */
@XStreamAlias("segment")
public class Segment implements Iterable<Element> {

    @XStreamOmitField
    private Separators _separators;
    @XStreamAlias("id")
    private String _id;
    @XStreamAlias("elements")
    private List<Element> _elements = new ArrayList<>();

    /**
     * Construct with the default separators
     */
    public Segment() {
        _separators = new Separators();
    }

    /**
     * The constructor takes a <code>Separators</code> object as input. The separators object represents the delimiters in a X12 transaction.
     * @param separators the separators  object
     */
    public Segment(Separators separators) {
        _separators = separators;
    }

    /**
     * The constructor takes a <code>String</code> object as input. The string object represents the id of the segment.
     * @param id the segement name
     */
    public Segment(String id) {
        _id = id;
        _separators = new Separators();
    }

    /**
     * The constructor takes a <code>String</code> object and a <code>Separators</code> object as input . The string object represents the id of the segment.
     * The separators object represents the delimiters in a X12 transaction.
     * @param id the segement name
     * @param separators the separators object
     */
    public Segment(String id, Separators separators) {
        _id = id;
        _separators = separators;
    }

    /**
     * Returns the id of the segment.
     * @return the segment id.
     */
    public String getId() {
        return _id;
    }

    /**
     * Takes a <code>String</code> object that will be set as the id for this segment
     * @param id is the segment name
     */
    public void setId(String id) {
        _id = id;
    }

    /**
     * Returns the separator definitions
     * @return Separators object
     */
    public Separators getSeparators() {
        return _separators;
    }

    /**
     * @return List of elements
     */
    public List<Element> getElements() {
        return _elements;
    }

    /**
     * Set the list of elements
     * @param elements list of Elements
     */
    public void setElements(List<Element> elements) {
        _elements = elements;
    }

    /**
     * Adds <code>Element</code> element to the segment. The element is added at the end of the elements in the current segment.
     * @param element the element to be added
     * @return boolean
     */
    public boolean addElement(Element element) {
        return _elements.add(element);
    }

    /**
     * Appends an element to the current element using the a <code>String</code> object as the Element value. Uses the first Element
     * in the element list to determine the element ID so element must have at least one element in it.
     * @param elementText string to append in a new element
     * @return value of addElements
     */
    public boolean appendElement(String elementText) {
        if (_id == null || elementText == null)
            throw new IllegalStateException("No segment identifier has been set");

        int elementIdNum = _elements.size() + 1;
        String elementId = (_elements.size() < 10) ? _id + "0" + elementIdNum : _id + elementIdNum;

        return addElements(new Element(elementId, elementText, _separators));
    }

    /**
     * Adds Element with elements to the segment. The elements are
     * added at the end of the elements in the current segment. e.g.
     * <pre>
     * {@code
     * addElements("ISA*ISA01*ISA02");
     * }
     * </pre>
     * @param elementText raw text representing the elements of a segment
     * @return boolean indicating success
     */
    public boolean addElements(String elementText) {
        String[] elementsStr = elementText.split(Pattern.quote(_separators.getElement().toString()));
        Element[] elements = new Element[elementsStr.length - 1];

        _id = elementsStr[0];
        for (int i = 1; i < elementsStr.length; i++) {
            String elementId = (i < 10) ? _id + "0" + i : _id + i;
            elements[i - 1] = new Element(elementId, elementsStr[i], _separators);
        }

        return addElements(elements);
    }

    /**
     * Adds <code>Element</code> elements to the segment. The elements are added at the end of the elements in the current segment. e.g.
     * <code> addElements("ISA", "ISA01", "ISA02");</code>
     * @param elements raw text representing the elements of a segment
     * @return boolean indicating success
     */
    public boolean addElements(Element... elements) {
        for (Element s : elements) {
            if (!_elements.add(s) || _id == null)
                return false;
        }

        return true;
    }

    /**
     * Adds strings as a composite element to the end of the segment.
     * @param id element identifier
     * @param compositeElements sub-elements of a composite element
     * @return boolean indicating success
     */
    public boolean addCompositeElement(String id, String... compositeElements) {
        StringBuilder dump = new StringBuilder();
        for (String s : compositeElements) {
            dump.append(s);
            dump.append(_separators.getCompositeElement());
        }

        return _elements.add(new Element(id, dump.substring(0, dump.length() - 1), _separators));
    }

    /**
     * Inserts Element element to the segment at the specified position
     * @param index element index
     * @param element the element to be added
     */
    public void addElement(int index, Element element) {
        _elements.add(index, element);
    }

    /**
     * Inserts strings as a composite element to segment at specified position
     * @param index element index
     * @param id element identifier
     * @param compositeElements sub-elements of a composite element
     */
    public void addCompositeElement(int index, String id, String... compositeElements) {
        StringBuilder dump = new StringBuilder();
        for (String s : compositeElements) {
            dump.append(s);
            dump.append(_separators.getCompositeElement());
        }
        _elements.add(index, new Element(id, dump.substring(0, dump.length() - 1), _separators));
    }

    /**
     * Returns the Element element at the specified position.
     * @param id of the desired element
     * @return the element at the specified position.
     */
    public Element getElement(String id) {
        for (Element elem : _elements)
            if (elem.getId().equals(id))
                return elem;

        return null;
    }

    /**
     * Return the element Value with the passed identifier or null if none found
     * @param id element id
     * @return element value as a String
     */
    public String getElementValue(String id) {
        for (Element elem : _elements)
            if (elem.getId().equals(id))
                return elem.getValue();

        return null;
    }

    /**
     * Return the subelement value with the passed identifier or null if none found
     * @param id element id
     * @param index element index
     * @return element value as a String
     */
    public String getElement(String id, int index) {
        for (Element elem : _elements)
            if (elem.getId().equals(id))
                return elem.getSubElement(index);

        return null;
    }

    /**
     * Removes the element at the specified position in this list.
     * @param index element index
     * @return the removed Element
     */
    public Element removeElement(int index) {
        return _elements.remove(index - 1);
    }

    /**
     * Sets the separators of the segment
     * @param separators separator definitions
     */
    public void setSeparators(Separators separators) {
        _separators = separators;
    }

    /**
     * Replaces element at the specified position with the specified Element
     * @param index position of the element to be replaced
     * @param elementText new element with which to replace
     */
    public void setElement(int index, String elementText) {
        _elements.get(index - 1).setValue(elementText);
    }

    /**
     * Replaces composite element at the specified position in segment.
     * @param index element index
     * @param compositeElements sub-elements of a composite element
     */
    public void setCompositeElement(int index, String... compositeElements) {
        StringBuilder dump = new StringBuilder();
        for (String s : compositeElements) {
            dump.append(s);
            dump.append(_separators.getCompositeElement());
        }
        _elements.get(index - 1).setValue(dump.substring(0, dump.length() - 1));
    }

    /**
     * Returns number of elements in the segment.
     * @return number of elements in the segment
     */
    public int size() {
        return _elements.size();
    }

    /**
     * Returns and Iterator to the elements in the segment.
     * @return Iterator of element Strings
     */
    @Override
    public Iterator<Element> iterator() {
        return _elements.iterator();
    }

    /**
     * Returns the X12 representation of the segment.
     * @return X12 representation as a String
     */
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append(_id);
        output.append(_separators.getElement());
        for (Element e : _elements) {
            output.append(e.toString());
            output.append(_separators.getElement());
        }

        if (output.length() == 0)
            return "";

        return output.substring(0, output.length() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Segment elements = (Segment)o;
        return Objects.equals(_separators, elements._separators) &&
                Objects.equals(_id, elements._id) &&
                Objects.equals(_elements, elements._elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_separators, _id, _elements);
    }
}
