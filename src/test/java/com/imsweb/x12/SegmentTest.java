package com.imsweb.x12;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SegmentTest {

    @Test
    void testSegmentEmpty() {
        Segment s = new Segment(new Separators('~', '*', ':'));
        assertNotNull(s);
        assertEquals(new Separators(), s.getSeparators());
        assertEquals(new Separators('~', '*', ':'), s.getSeparators());

        s = new Segment();
        assertEquals(new Separators(), s.getSeparators());
        assertEquals(new Separators('~', '*', ':'), s.getSeparators());
    }

    @Test
    void testAddElementString() {
        Segment s = new Segment();
        assertTrue(s.addElement(new Element("ISA00", "ISA")));
    }

    @Test
    void testAddElements() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem2 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem3 = new Element("ISA02", "ISA02");
        assertTrue(s.addElements(elem2, elem3));

        s = new Segment();
        assertTrue(s.addElement("ISA01", "ISA01"));
        assertTrue(s.addElement("ISA02", "ISA02"));
        assertEquals(2, s.getElements().size());
    }

    @Test
    void testAddCompositeElementStringArray() {
        Segment s = new Segment();
        assertTrue(s.addCompositeElement("ID", "AB", "CD", "EF"));
    }

    @Test
    void testAddElementIntString() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem2 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem3 = new Element("ISA02", "ISA02", new Separators('~', '*', ':'));
        s.addElements(elem2, elem3);
        assertTrue(s.addCompositeElement("ISA03", "ISA03_1", "ISA03_2", "ISA03_3"));
    }

    @Test
    void testAddCompositeElementIntStringArray() {
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
    void testGetSeparators() {
        Segment s = new Segment();
        assertEquals("[~,*,:]", s.getSeparators().toString());
    }

    @Test
    void testGetElement() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem1 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem2 = new Element("ISA02", "ISA02");
        Element elem3 = new Element("ISA03", "ISA03", new Separators('~', '*', ':'));
        s.addElements(elem1, elem2, elem3);
        assertEquals("ISA02", s.getElementValue("ISA02"));
    }

    @Test
    void testIterator() {
        Segment s = new Segment();
        s.setId("ISA");
        Element elem1 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem2 = new Element("ISA02", "ISA02", new Separators('~', '*', ':'));
        Element elem3 = new Element("ISA03", "ISA03", new Separators('~', '*', ':'));
        s.addElements(elem1, elem2, elem3);
        assertNotNull(s.iterator());
    }

    @Test
    void testRemoveElement() {
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
    void testRemoveElementTwo() {
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
    void testSetSeparators() {
        Segment s = new Segment();
        s.setSeparators(new Separators('s', 'e', 'c'));
        assertEquals("[s,e,c]", s.getSeparators().toString());
    }

    @Test
    void testSetElement() {
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
    void testSetCompositeElement() {
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
    void testSize() {
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
    void testToString() {
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
    void testToStringEmptyElements() {
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
    void testToStringNullElements() {
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

    @Test
    void testAppendElementToInvalidSegments() {
        Segment s = new Segment();
        assertThrows(IllegalStateException.class, () -> s.appendElement("Append"));
    }

    @Test
    void testAppendNullSegment() {
        Segment s = new Segment();
        assertThrows(IllegalStateException.class, () -> s.appendElement(null));
    }

    @Test
    void testAppendSegment() {
        Segment s = new Segment("ISA");
        Element elem1 = new Element("ISA01", "ISA01", new Separators('~', '*', ':'));
        Element elem2 = new Element("ISA02", "ISA02", new Separators('~', '*', ':'));
        s.addElements(elem1, elem2);
        s.appendElement("ISA03");

        assertEquals("ISA03", s.getElement("ISA03").getId());
        assertEquals("ISA03", s.getElement("ISA03").getValue());
    }

    @Test
    void testReadmeExample() {
        Loop isaLoop = new Loop("ISA_LOOP");

        Segment segment = new Segment("ISA");
        segment.addElement("01", "00");
        segment.addElement("02", "          ");
        segment.addElement("03", "01");
        segment.addElement("04", "SECRET    ");
        segment.addElement("05", "ZZ");
        segment.addElement("06", "SUBMITTERS.ID  ");
        segment.addElement("07", "ZZ");
        segment.addElement("08", "RECEIVERS.ID   ");
        segment.addElement("09", "030101");
        segment.addElement("10", "1253");
        segment.addElement("11", "U");
        segment.addElement("12", "00501");
        segment.addElement("13", "000000905");
        segment.addElement("14", "1");
        segment.addElement("15", "T");
        segment.addElement("16", ":");
        isaLoop.addSegment(segment);

        segment = new Segment("IEA");
        segment.addElement("01", "1");
        segment.addElement("02", "000000905");
        isaLoop.addSegment(segment);

        assertEquals(2, isaLoop.getSegments().size());
    }

}
