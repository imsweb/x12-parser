/*
 * Copyright (C) 2020 Information Management Services, Inc.
 */
package com.imsweb.x12.writer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;

import com.imsweb.x12.Element;
import com.imsweb.x12.LineBreak;
import com.imsweb.x12.Loop;
import com.imsweb.x12.Segment;
import com.imsweb.x12.Separators;
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

    @Test
    public void testPrintFromLoop() {
        Separators separators = new Separators();
        separators.setLineBreak(LineBreak.CRLF);
        Loop isaLoop = new Loop(separators, "ISA_LOOP");
        Segment segment = new Segment("ISA", separators);
        addElement(segment, "01", "00", separators);
        addElement(segment, "02", "          ", separators);
        addElement(segment, "03", "01", separators);
        addElement(segment, "04", "SECRET    ", separators);
        addElement(segment, "05", "ZZ", separators);
        addElement(segment, "06", "SUBMITTERS.ID  ", separators);
        addElement(segment, "07", "ZZ", separators);
        addElement(segment, "08", "RECEIVERS.ID   ", separators);
        addElement(segment, "09", "030101", separators);
        addElement(segment, "10", "1253", separators);
        addElement(segment, "11", "U", separators);
        addElement(segment, "12", "00501", separators);
        addElement(segment, "13", "000000905", separators);
        addElement(segment, "14", "1", separators);
        addElement(segment, "15", "T", separators);
        addElement(segment, "16", ":", separators);
        isaLoop.addSegment(segment);
        segment = new Segment("IEA", separators);
        addElement(segment, "01", "1", separators);
        addElement(segment, "02", "000000905", separators);
        isaLoop.addSegment(segment);
        
        Loop gsLoop = new Loop(separators, "GS_LOOP");
        segment = new Segment("GS", separators);
        addElement(segment, "01", "HC", separators);
        addElement(segment, "02", "SENDER CODE", separators);
        addElement(segment, "03", "RECEIVER CODE", separators);
        addElement(segment, "04", "19991231", separators);
        addElement(segment, "05", "0802", separators);
        addElement(segment, "06", "1", separators);
        addElement(segment, "07", "X", separators);
        addElement(segment, "08", "005010X222A1", separators);
        gsLoop.addSegment(segment);
        segment = new Segment("GE", separators);
        addElement(segment, "01", "1", separators);
        addElement(segment, "02", "1", separators);
        gsLoop.addSegment(segment);
        isaLoop.getLoops().add(gsLoop);
        
        
        X12Writer writer = new X12Writer(FileType.ANSI837_5010_X222, Collections.singletonList(isaLoop), separators);
        //System.out.println(writer.toX12String(LineBreak.CRLF));
    }
    
    
    private void addElement(Segment segment, String elementNum, String data, Separators separators) {
        segment.addElement(new Element(segment.getId() + elementNum, data, separators));   
    }
}
