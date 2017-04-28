package com.imsweb.x12;

import org.junit.Assert;
import org.junit.Test;

public class ElementTest {

    @Test
    public void testConstruction() {
        Element element = new Element("ID", "VALUE");

        Assert.assertEquals("ID", element.getId());
        Assert.assertEquals("VALUE", element.getValue());
    }

}