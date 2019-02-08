package com.imsweb.x12;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ElementTest {

    @Test
    public void testConstruction() {
        Element element = new Element("ID", "VALUE");

        assertEquals("ID", element.getId());
        assertEquals("VALUE", element.getValue());
    }

}