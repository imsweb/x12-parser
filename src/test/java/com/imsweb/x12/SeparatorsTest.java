package com.imsweb.x12;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SeparatorsTest {

    @Test
    void testSeparators() {
        Separators separators = new Separators();
        assertNotNull(separators);
    }

    @Test
    void testContextCharacterCharacterCharacter() {
        Separators separators = new Separators('a', 'b', 'c');
        assertNotNull(separators);
    }

    @Test
    void testGetCompositeElementSeparator() {
        Separators separators = new Separators('a', 'b', 'c');
        assertEquals(Character.valueOf('c'), separators.getCompositeElement());
    }

    @Test
    void testGetElementSeparator() {
        Separators separators = new Separators('a', 'b', 'c');
        assertEquals(Character.valueOf('b'), separators.getElement());
    }

    @Test
    void testGetSegmentSeparator() {
        Separators separators = new Separators('a', 'b', 'c');
        assertEquals(Character.valueOf('a'), separators.getSegment());
    }

    @Test
    void testSetCompositeElementSeparator() {
        Separators separators = new Separators();
        separators.setCompositeElement('c');
        assertEquals(Character.valueOf('c'), separators.getCompositeElement());

    }

    @Test
    void testSetElementSeparator() {
        Separators separators = new Separators();
        separators.setElement('b');
        assertEquals(Character.valueOf('b'), separators.getElement());
    }

    @Test
    void testSetSegmentSeparator() {
        Separators separators = new Separators();
        separators.setSegment('b');
        assertEquals(Character.valueOf('b'), separators.getSegment());
    }

    @Test
    void testToString() {
        Separators separators = new Separators('a', 'b', 'c');
        assertEquals("[a,b,c]", separators.toString());
    }

    @Test
    void testSplitSegment() {
        Separators separators = new Separators();
        assertNull(separators.splitSegment(null));

        String segments = "Test1~Test2~Test3~";
        String[] split = separators.splitSegment(segments);
        assertEquals(3, split.length);
        assertEquals("Test1", split[0]);
        assertEquals("Test2", split[1]);
        assertEquals("Test3", split[2]);

        segments = "Test1|Test2|Test3|";
        separators.setSegment('|');
        split = separators.splitSegment(segments);
        assertEquals(3, split.length);
        assertEquals("Test1", split[0]);
        assertEquals("Test2", split[1]);
        assertEquals("Test3", split[2]);

        separators.setSegment(null);
        assertNull(separators.splitSegment(segments));
    }

    @Test
    void testSplitElement() {
        Separators separators = new Separators();
        assertNull(separators.splitElement(null));

        String segments = "Test1*Test2*Test3*";
        String[] split = separators.splitElement(segments);
        assertEquals(3, split.length);
        assertEquals("Test1", split[0]);
        assertEquals("Test2", split[1]);
        assertEquals("Test3", split[2]);

        segments = "Test1|Test2|Test3|";
        separators.setElement('|');
        split = separators.splitElement(segments);
        assertEquals(3, split.length);
        assertEquals("Test1", split[0]);
        assertEquals("Test2", split[1]);
        assertEquals("Test3", split[2]);

        separators.setElement(null);
        assertNull(separators.splitElement(segments));
    }

    @Test
    void testSplitComposite() {
        Separators separators = new Separators();
        assertNull(separators.splitComposite(null));

        String segments = "Test1:Test2:Test3:";
        String[] split = separators.splitComposite(segments);
        assertEquals(3, split.length);
        assertEquals("Test1", split[0]);
        assertEquals("Test2", split[1]);
        assertEquals("Test3", split[2]);

        segments = "Test1|Test2|Test3|";
        separators.setCompositeElement('|');
        split = separators.splitComposite(segments);
        assertEquals(3, split.length);
        assertEquals("Test1", split[0]);
        assertEquals("Test2", split[1]);
        assertEquals("Test3", split[2]);

        separators.setCompositeElement(null);
        assertNull(separators.splitComposite(segments));
    }
}
