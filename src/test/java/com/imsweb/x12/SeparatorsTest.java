package com.imsweb.x12;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SeparatorsTest {

    @Test
    public void testSeparators() {
        Separators separators = new Separators();
        assertNotNull(separators);
    }

    @Test
    public void testContextCharacterCharacterCharacter() {
        Separators separators = new Separators('a', 'b', 'c');
        assertNotNull(separators);
    }

    @Test
    public void testGetCompositeElementSeparator() {
        Separators separators = new Separators('a', 'b', 'c');
        assertEquals(Character.valueOf('c'), separators.getCompositeElement());
    }

    @Test
    public void testGetElementSeparator() {
        Separators separators = new Separators('a', 'b', 'c');
        assertEquals(Character.valueOf('b'), separators.getElement());
    }

    @Test
    public void testGetSegmentSeparator() {
        Separators separators = new Separators('a', 'b', 'c');
        assertEquals(Character.valueOf('a'), separators.getSegment());
    }

    @Test
    public void testSetCompositeElementSeparator() {
        Separators separators = new Separators();
        separators.setCompositeElement('c');
        assertEquals(Character.valueOf('c'), separators.getCompositeElement());

    }

    @Test
    public void testSetElementSeparator() {
        Separators separators = new Separators();
        separators.setElement('b');
        assertEquals(Character.valueOf('b'), separators.getElement());
    }

    @Test
    public void testSetSegmentSeparator() {
        Separators separators = new Separators();
        separators.setSegment('b');
        assertEquals(Character.valueOf('b'), separators.getSegment());
    }

    @Test
    public void testToString() {
        Separators separators = new Separators('a', 'b', 'c');
        assertEquals("[a,b,c]", separators.toString());
    }

}
