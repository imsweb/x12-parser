package com.imsweb.x12;

import org.junit.Assert;
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

    @Test
    public void testSplitSegment() {
        Separators separators = new Separators();
        Assert.assertNull(separators.splitSegment(null));

        String segments = "Test1~Test2~Test3~";
        String[] split = separators.splitSegment(segments);
        Assert.assertEquals(3, split.length);
        Assert.assertEquals("Test1", split[0]);
        Assert.assertEquals("Test2", split[1]);
        Assert.assertEquals("Test3", split[2]);

        segments = "Test1|Test2|Test3|";
        separators.setSegment('|');
        split = separators.splitSegment(segments);
        Assert.assertEquals(3, split.length);
        Assert.assertEquals("Test1", split[0]);
        Assert.assertEquals("Test2", split[1]);
        Assert.assertEquals("Test3", split[2]);

        separators.setSegment(null);
        Assert.assertNull(separators.splitSegment(segments));
    }

    @Test
    public void testSplitElement() {
        Separators separators = new Separators();
        Assert.assertNull(separators.splitElement(null));

        String segments = "Test1*Test2*Test3*";
        String[] split = separators.splitElement(segments);
        Assert.assertEquals(3, split.length);
        Assert.assertEquals("Test1", split[0]);
        Assert.assertEquals("Test2", split[1]);
        Assert.assertEquals("Test3", split[2]);

        segments = "Test1|Test2|Test3|";
        separators.setElement('|');
        split = separators.splitElement(segments);
        Assert.assertEquals(3, split.length);
        Assert.assertEquals("Test1", split[0]);
        Assert.assertEquals("Test2", split[1]);
        Assert.assertEquals("Test3", split[2]);

        separators.setElement(null);
        Assert.assertNull(separators.splitElement(segments));
    }

    @Test
    public void testSplitComposite() {
        Separators separators = new Separators();
        Assert.assertNull(separators.splitComposite(null));

        String segments = "Test1:Test2:Test3:";
        String[] split = separators.splitComposite(segments);
        Assert.assertEquals(3, split.length);
        Assert.assertEquals("Test1", split[0]);
        Assert.assertEquals("Test2", split[1]);
        Assert.assertEquals("Test3", split[2]);

        segments = "Test1|Test2|Test3|";
        separators.setCompositeElement('|');
        split = separators.splitComposite(segments);
        Assert.assertEquals(3, split.length);
        Assert.assertEquals("Test1", split[0]);
        Assert.assertEquals("Test2", split[1]);
        Assert.assertEquals("Test3", split[2]);

        separators.setCompositeElement(null);
        Assert.assertNull(separators.splitComposite(segments));
    }
}
