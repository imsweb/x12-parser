package com.imsweb.x12;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class HtmlUtilsTest {

    @Test
    void testNullAndEmpty() {
        assertEquals("", HtmlUtils.escape(null));
        assertEquals("", HtmlUtils.escape(""));
    }

    @Test
    void testNoSpecialCharactersReturnsSameInstance() {
        String value = "Plain value 123 - ok";
        assertSame(value, HtmlUtils.escape(value));
    }

    @Test
    void testEachSpecialCharacter() {
        assertEquals("&amp;", HtmlUtils.escape("&"));
        assertEquals("&lt;", HtmlUtils.escape("<"));
        assertEquals("&gt;", HtmlUtils.escape(">"));
        assertEquals("&quot;", HtmlUtils.escape("\""));
        assertEquals("&#39;", HtmlUtils.escape("'"));
    }

    @Test
    void testMixedContent() {
        assertEquals("a&amp;b &lt;c&gt; &quot;d&quot; &#39;e&#39; f", HtmlUtils.escape("a&b <c> \"d\" 'e' f"));
    }

    @Test
    void testSpecialCharacterAtStartAndEnd() {
        assertEquals("&lt;abc&gt;", HtmlUtils.escape("<abc>"));
    }

    @Test
    void testAlreadyEscapedInputIsEscapedAgain() {
        // by design the input is always treated as raw text, so existing entities are not preserved
        assertEquals("&amp;lt;", HtmlUtils.escape("&lt;"));
    }

    @Test
    void testHtmlId() {
        assertEquals("ISA-0", HtmlUtils.htmlId(null, "ISA", 0));
        assertEquals("ISA-3", HtmlUtils.htmlId("", "ISA", 3));
        assertEquals("ISA-0__GS-1", HtmlUtils.htmlId("ISA-0", "GS", 1));
    }

    @Test
    void testAttributeBreakout() {
        assertEquals("&quot; onmouseover=&quot;alert(1)", HtmlUtils.escape("\" onmouseover=\"alert(1)"));
    }
}
