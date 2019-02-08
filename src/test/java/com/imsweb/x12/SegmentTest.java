package com.imsweb.x12;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SegmentTest {

    @Test
    public void testSegmentEmpty() {
        Segment s = new Segment(new Separators('~', '*', ':'));
        assertNotNull(s);
        assertEquals(new Separators(), s.getSeparators());
        assertEquals(new Separators('~', '*', ':'), s.getSeparators());

        s = new Segment();
        assertEquals(new Separators(), s.getSeparators());
        assertEquals(new Separators('~', '*', ':'), s.getSeparators());
    }

    @Test
    public void testAddElementString() {
        Segment s = new Segment();
        assertTrue(s.addElement(new Element("ISA00", "ISA")));
    }

    @Test
    public void testAddElements() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem2 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem3 = new Element("ISA02", "ISA02");
        assertTrue(s.addElements(elem2, elem3));
    }

    @Test
    public void testAddCompositeElementStringArray() {
        Segment s = new Segment();
        assertTrue(s.addCompositeElement("ID", "AB", "CD", "EF"));
    }

    @Test
    public void testAddElementIntString() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem2 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem3 = new Element("ISA02", "ISA02", new Separators('~', '*', ':'));
        s.addElements(elem2, elem3);
        assertTrue(s.addCompositeElement("ISA03", "ISA03_1", "ISA03_2", "ISA03_3"));
    }

    @Test
    public void testAddCompositeElementIntStringArray() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem1 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem2 = new Element("ISA02", "ISA02");
        Element elem4 = new Element("ISA04", "ISA04", new Separators('~', '*', ':'));
        s.addElements(elem1, elem2, elem4);
        s.addCompositeElement(3, "ISA03", "ISA03_1", "ISA03_2", "ISA03_3");
        assertEquals("ISA03_1:ISA03_2:ISA03_3", s.getElementValue("ISA03"));
    }

    @Test
    public void testGetSeparators() {
        Segment s = new Segment();
        assertEquals("[~,*,:]", s.getSeparators().toString());
    }

    @Test
    public void testGetElement() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem1 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem2 = new Element("ISA02", "ISA02");
        Element elem3 = new Element("ISA03", "ISA03", new Separators('~', '*', ':'));
        s.addElements(elem1, elem2, elem3);
        assertEquals("ISA02", s.getElementValue("ISA02"));
    }

    @Test
    public void testIterator() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem1 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem2 = new Element("ISA02", "ISA02", new Separators('~', '*', ':'));
        Element elem3 = new Element("ISA03", "ISA03", new Separators('~', '*', ':'));
        s.addElements(elem1, elem2, elem3);
        assertNotNull(s.iterator());
    }

    @Test
    public void testRemoveElement() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem1 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem2 = new Element("ISA02", "ISA02", new Separators('~', '*', ':'));
        Element elem3 = new Element("ISA03", "ISA03", new Separators('~', '*', ':'));
        s.addElements(elem1, elem2, elem3);
        s.removeElement(2);
        assertEquals("ISA*ISA01*ISA03", s.toString());
    }

    @Test
    public void testRemoveElementTwo() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem1 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem2 = new Element("ISA02", "ISA02", new Separators('~', '*', ':'));
        Element elem3 = new Element("ISA03", "ISA03", new Separators('~', '*', ':'));
        s.addElements(elem1, elem2, elem3);
        s.removeElement(3);
        assertEquals("ISA*ISA01*ISA02", s.toString());
    }

    @Test
    public void testSetSeparators() {
        Segment s = new Segment();
        s.setSeparators(new Separators('s', 'e', 'c'));
        assertEquals("[s,e,c]", s.getSeparators().toString());
    }

    @Test
    public void testSetElement() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem1 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem2 = new Element("ISA02", "ISA02", new Separators('~', '*', ':'));
        Element elem3 = new Element("ISA03", "ISA04", new Separators('~', '*', ':'));
        Element elem4 = new Element("ISA04", "ISA04", new Separators('~', '*', ':'));
        s.addElements(elem1, elem2, elem3, elem4);
        s.setElement(3, "ISA03");
        assertEquals("ISA03", s.getElement("ISA03").getValue());
    }

    @Test
    public void testSetCompositeElement() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem1 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem2 = new Element("ISA02", "ISA02", new Separators('~', '*', ':'));
        Element elem3 = new Element("ISA03", "ISA04", new Separators('~', '*', ':'));
        Element elem4 = new Element("ISA04", "ISA04", new Separators('~', '*', ':'));
        s.addElements(elem1, elem2, elem3, elem4);
        s.setCompositeElement(3, "ISA03_1", "ISA03_2", "ISA03_3");
        assertEquals("ISA03_1:ISA03_2:ISA03_3", s.getElement("ISA03").getValue());
    }

    @Test
    public void testSize() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem1 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem2 = new Element("ISA02", "ISA02", new Separators('~', '*', ':'));
        Element elem3 = new Element("ISA03", "ISA03", new Separators('~', '*', ':'));
        Element elem4 = new Element("ISA04", "ISA04", new Separators('~', '*', ':'));
        s.addElements(elem1, elem2, elem3, elem4);
        assertEquals(4, s.size());
    }

    @Test
    public void testToString() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem1 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem2 = new Element("ISA02", "ISA02", new Separators('~', '*', ':'));
        Element elem3 = new Element("ISA03", "ISA03", new Separators('~', '*', ':'));
        Element elem4 = new Element("ISA04", "ISA04", new Separators('~', '*', ':'));
        s.addElements(elem1, elem2, elem3, elem4);
        s.setCompositeElement(3, "ISA03_1", "ISA03_2", "ISA03_3");
        assertEquals("ISA*ISA01*ISA02*ISA03_1:ISA03_2:ISA03_3*ISA04", s.toString());

    }

    @Test
    public void testToStringEmptyElements() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem1 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem2 = new Element("ISA02", "ISA02", new Separators('~', '*', ':'));
        Element elem3 = new Element("ISA03", "ISA03", new Separators('~', '*', ':'));
        Element elem4 = new Element("ISA04", "ISA04", new Separators('~', '*', ':'));
        Element elem5 = new Element("ISA04", "", new Separators('~', '*', ':'));
        Element elem6 = new Element("ISA04", "", new Separators('~', '*', ':'));
        Element elem7 = new Element("ISA04", "", new Separators('~', '*', ':'));
        s.addElements(elem1, elem2, elem3, elem4, elem5, elem6, elem7);
        assertEquals("ISA*ISA01*ISA02*ISA03*ISA04***", s.toString());
    }

    @Test
    public void testToStringNullElements() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem1 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem2 = new Element("ISA02", "ISA02", new Separators('~', '*', ':'));
        Element elem3 = new Element("ISA03", "ISA03", new Separators('~', '*', ':'));
        Element elem4 = new Element("ISA04", "ISA04", new Separators('~', '*', ':'));
        Element elem5 = new Element("ISA04", null, new Separators('~', '*', ':'));
        Element elem6 = new Element("ISA04", null, new Separators('~', '*', ':'));
        Element elem7 = new Element("ISA04", null, new Separators('~', '*', ':'));
        s.addElements(elem1, elem2, elem3, elem4, elem5, elem6, elem7);
        assertEquals("ISA*ISA01*ISA02*ISA03*ISA04***", s.toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testAppendElementToInvalidSegments() {
        Segment s = new Segment();
        s.appendElement("Append");
    }

    @Test(expected = IllegalStateException.class)
    public void testAppendNullSegment() {
        Segment s = new Segment();
        s.appendElement(null);
    }

    @Test
    public void testAppendSegment() {
        Segment s = new Segment("ISA");
        Element elem1 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem2 = new Element("ISA02", "ISA02", new Separators('~', '*', ':'));
        s.addElements(elem1, elem2);
        s.appendElement("ISA03");

        assertEquals("ISA03", s.getElement("ISA03").getId());
        assertEquals("ISA03", s.getElement("ISA03").getValue());
    }

}
