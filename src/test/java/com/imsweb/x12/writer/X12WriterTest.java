/*
 * Copyright (C) 2020 Information Management Services, Inc.
 */
package com.imsweb.x12.writer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;

import com.imsweb.x12.LineBreak;
import com.imsweb.x12.reader.X12Reader;
import com.imsweb.x12.reader.X12Reader.FileType;

public class X12WriterTest {
    /**
     * Here we will test that you can go from x12, make changes, then serialize the
     * x12 once again.
     */
    @Test
    public void testSerializeBasic() throws IOException {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");

        X12Reader fromFileUtf8 = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()),
                StandardCharsets.UTF_8);

        String expected = IOUtils
                .toString(this.getClass().getResourceAsStream("/837_5010/x12_valid.txt"), StandardCharsets.UTF_8)
                .trim();
        LineBreak lineBreak;
        if (expected.contains(LineBreak.CRLF.getLineBreakString())) {
            lineBreak = LineBreak.CRLF;
        }
        else {
            lineBreak = LineBreak.LF;
        }
        X12Writer writer = new X12Writer(fromFileUtf8);
        Assert.assertEquals(expected, writer.toX12String(lineBreak).trim());
    }

    /**
     * Tests the toHtml method that
     */
    @Test
    public void testToHtmlBasic() throws IOException {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");

        X12Reader fromFileUtf8 = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()),
                StandardCharsets.UTF_8);

        String x12Template = IOUtils.toString(getClass().getResourceAsStream("/html/x12-template.html"), StandardCharsets.UTF_8);

        X12Writer writer = new X12Writer(fromFileUtf8);
        String x12HtmlSegment = writer.toHtml();

        String fullX12Html = String.format(x12Template, x12HtmlSegment);

        Document doc = Jsoup.parse(fullX12Html);
        Elements loops = doc.select(".x12-loop");
        Assert.assertEquals(20, loops.size());

        Elements segments = doc.select(".x12-segment");
        Assert.assertEquals(38, segments.size());

        Elements elements = doc.select(".x12-element");
        Assert.assertEquals(216, elements.size());
    }

    /**
     * Test a more complex x12 doc and see if we can serialize it.
     */
    @Test
    public void testSerializeComplex() throws IOException {
        URL url = this.getClass().getResource("/837_5010/x12_complex.txt");

        X12Reader fromFileUtf8 = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()),
                StandardCharsets.UTF_8);

        String expected = IOUtils
                .toString(this.getClass().getResourceAsStream("/837_5010/x12_complex.txt"), StandardCharsets.UTF_8)
                .trim();

        LineBreak lineBreak;
        if (expected.contains(LineBreak.CRLF.getLineBreakString())) {
            lineBreak = LineBreak.CRLF;
        }
        else {
            lineBreak = LineBreak.LF;
        }
        X12Writer writer = new X12Writer(fromFileUtf8);
        Assert.assertEquals(expected, writer.toX12String(lineBreak).trim());
    }


}
