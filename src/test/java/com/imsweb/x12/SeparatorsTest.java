package com.imsweb.x12;

import org.junit.Assert;
import org.junit.Test;

public class SeparatorsTest {

    @Test
    public void testSeparators() {
        Separators separators = new Separators();
        Assert.assertNotNull(separators);
    }

    @Test
    public void testContextCharacterCharacterCharacter() {
        Separators separators = new Separators('a', 'b', 'c');
        Assert.assertNotNull(separators);
    }

    @Test
    public void testGetCompositeElementSeparator() {
        Separators separators = new Separators('a', 'b', 'c');
        Assert.assertEquals(Character.valueOf('c'), separators.getCompositeElement());
    }

    @Test
    public void testGetElementSeparator() {
        Separators separators = new Separators('a', 'b', 'c');
        Assert.assertEquals(Character.valueOf('b'), separators.getElement());
    }

    @Test
    public void testGetSegmentSeparator() {
        Separators separators = new Separators('a', 'b', 'c');
        Assert.assertEquals(Character.valueOf('a'), separators.getSegment());
    }

    @Test
    public void testSetCompositeElementSeparator() {
        Separators separators = new Separators();
        separators.setCompositeElement('c');
        Assert.assertEquals(Character.valueOf('c'), separators.getCompositeElement());

    }

    @Test
    public void testSetElementSeparator() {
        Separators separators = new Separators();
        separators.setElement('b');
        Assert.assertEquals(Character.valueOf('b'), separators.getElement());
    }

    @Test
    public void testSetSegmentSeparator() {
        Separators separators = new Separators();
        separators.setSegment('b');
        Assert.assertEquals(Character.valueOf('b'), separators.getSegment());
    }

    @Test
    public void testToString() {
        Separators separators = new Separators('a', 'b', 'c');
        Assert.assertEquals("[a,b,c]", separators.toString());
    }

}
