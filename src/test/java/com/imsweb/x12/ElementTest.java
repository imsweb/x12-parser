package com.imsweb.x12;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElementTest {

    @Test
    void testConstruction() {
        Element element = new Element("ID", "VALUE");

        assertEquals("ID", element.getId());
        assertEquals("VALUE", element.getValue());
    }

    @Test
    void testToHtmlEscapesValue() {
        Element element = new Element("NM101", "\" onfocus=\"alert(1)\" autofocus x=\"<script>alert('x')</script>&");

        String html = element.toHtml(Optional.empty(), new ArrayList<>());

        assertFalse(html.contains("<script>"));
        assertFalse(html.contains("\" onfocus="));
        assertTrue(html.contains("value=\"&quot; onfocus=&quot;alert(1)&quot; autofocus x=&quot;&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;&amp;\""));
    }

}