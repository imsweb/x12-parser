package com.imsweb.x12;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElementTest {

    @Test
    void testConstruction() {
        Element element = new Element("ID", "VALUE");

        assertEquals("ID", element.getId());
        assertEquals("VALUE", element.getValue());
    }

}