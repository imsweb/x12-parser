/*
 * Copyright (C) 2020 Information Management Services, Inc.
 */
package com.imsweb.x12.writer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import com.jayway.jsonpath.JsonPath;

import com.imsweb.x12.Element;
import com.imsweb.x12.LineBreak;
import com.imsweb.x12.Loop;
import com.imsweb.x12.Segment;
import com.imsweb.x12.Separators;
import com.imsweb.x12.reader.X12Reader;
import com.imsweb.x12.reader.X12Reader.FileType;

import static org.junit.jupiter.api.Assertions.assertEquals;

class X12WriterTest {

    /**
     * Here we will test that you can go from x12, make changes, then serialize the
     * x12 once again.
     */
    @Test
    void testSerializeBasic() throws IOException {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");

        X12Reader fromFileUtf8 = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()), StandardCharsets.UTF_8);

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
        assertEquals(expected, writer.toX12String(lineBreak).trim());
    }

    /**
     * Tests the toHtml method that
     */
    @Test
    void testToHtmlBasic() throws IOException {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");

        X12Reader fromFileUtf8 = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()),
                StandardCharsets.UTF_8);

        String x12Template = IOUtils.toString(getClass().getResourceAsStream("/html/x12-template.html"), StandardCharsets.UTF_8);

        X12Writer writer = new X12Writer(fromFileUtf8);
        String x12HtmlSegment = writer.toHtml();

        String fullX12Html = String.format(x12Template, x12HtmlSegment);

        Document doc = Jsoup.parse(fullX12Html);
        Elements loops = doc.select(".x12-loop");
        assertEquals(20, loops.size());

        Elements segments = doc.select(".x12-segment");
        assertEquals(38, segments.size());

        Elements elements = doc.select(".x12-element");
        assertEquals(216, elements.size());
    }

    /**
     * Test a more complex x12 doc and see if we can serialize it.
     */
    @Test
    void testSerializeComplex() throws IOException {
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
        assertEquals(expected, writer.toX12String(lineBreak).trim());
    }

    @Test
    void testPrintFromLoop() throws Exception {
        String expected = IOUtils
                .toString(this.getClass().getResourceAsStream("/837_5010/x12_writer_test.txt"), StandardCharsets.UTF_8)
                .trim();

        LineBreak lineBreak;
        if (expected.contains(LineBreak.CRLF.getLineBreakString())) {
            lineBreak = LineBreak.CRLF;
        }
        else {
            lineBreak = LineBreak.LF;
        }

        Separators separators = new Separators();
        separators.setLineBreak(lineBreak);
        Loop isaLoop = new Loop(separators, "ISA_LOOP");
        Segment segment = new Segment("ISA");
        addElement(segment, "01", "00");
        addElement(segment, "02", "          ");
        addElement(segment, "03", "01");
        addElement(segment, "04", "SECRET    ");
        addElement(segment, "05", "ZZ");
        addElement(segment, "06", "SUBMITTERS.ID  ");
        addElement(segment, "07", "ZZ");
        addElement(segment, "08", "RECEIVERS.ID   ");
        addElement(segment, "09", "030101");
        addElement(segment, "10", "1253");
        addElement(segment, "11", "U");
        addElement(segment, "12", "00501");
        addElement(segment, "13", "000000905");
        addElement(segment, "14", "1");
        addElement(segment, "15", "T");
        addElement(segment, "16", ":");
        isaLoop.addSegment(segment);
        segment = new Segment("IEA");
        addElement(segment, "01", "1");
        addElement(segment, "02", "000000905");
        isaLoop.addSegment(segment);

        Loop gsLoop = new Loop(separators, "GS_LOOP");
        segment = new Segment("GS");
        addElement(segment, "01", "HC");
        addElement(segment, "02", "SENDER CODE");
        addElement(segment, "03", "RECEIVER CODE");
        addElement(segment, "04", "19991231");
        addElement(segment, "05", "0802");
        addElement(segment, "06", "1");
        addElement(segment, "07", "X");
        addElement(segment, "08", "005010X222A1");
        gsLoop.addSegment(segment);
        segment = new Segment("GE");
        addElement(segment, "01", "1");
        addElement(segment, "02", "1");
        gsLoop.addSegment(segment);
        isaLoop.getLoops().add(gsLoop);

        Loop stLoop = new Loop(separators, "ST_LOOP");
        segment = new Segment("ST");
        addElement(segment, "01", "837");
        addElement(segment, "02", "987654");
        addElement(segment, "03", "005010X222A1");
        stLoop.addSegment(segment);
        segment = new Segment("SE");
        addElement(segment, "01", "25");
        addElement(segment, "02", "987654");
        stLoop.addSegment(segment);
        gsLoop.getLoops().add(stLoop);

        Loop header = new Loop(separators, "HEADER");
        segment = new Segment("BHT");
        addElement(segment, "01", "0019");
        addElement(segment, "02", "00");
        addElement(segment, "03", "0123");
        addElement(segment, "04", "20200617");
        addElement(segment, "05", "0932");
        addElement(segment, "06", "CH");
        header.addSegment(segment);
        Loop a1000 = new Loop(separators, "1000A");
        segment = new Segment("NM1");
        addElement(segment, "01", "41");
        addElement(segment, "02", "2");
        addElement(segment, "03", "MEDICAL FACILITY");
        addElement(segment, "04", "");
        addElement(segment, "05", "");
        addElement(segment, "06", "");
        addElement(segment, "07", "");
        addElement(segment, "08", "46");
        addElement(segment, "09", "999999999");
        a1000.addSegment(segment);
        segment = new Segment("PER");
        addElement(segment, "01", "IC");
        addElement(segment, "02", "MEDICAL DOCTOR");
        addElement(segment, "03", "TE");
        addElement(segment, "04", "3016809770");
        addElement(segment, "05", "EX");
        addElement(segment, "06", "123");
        a1000.addSegment(segment);
        Loop b1000 = new Loop(separators, "1000B");
        segment = new Segment("NM1");
        addElement(segment, "01", "40");
        addElement(segment, "02", "2");
        addElement(segment, "03", "HEALTH RECEIVER");
        addElement(segment, "04", "");
        addElement(segment, "05", "");
        addElement(segment, "06", "");
        addElement(segment, "07", "");
        addElement(segment, "08", "46");
        addElement(segment, "09", "111222333");
        b1000.addSegment(segment);
        header.getLoops().add(a1000);
        header.getLoops().add(b1000);
        stLoop.getLoops().add(header);

        Loop detail = new Loop(separators, "DETAIL");
        Loop a2000 = new Loop(separators, "2000A");
        segment = new Segment("HL");
        addElement(segment, "01", "1");
        addElement(segment, "02", "");
        addElement(segment, "03", "20");
        addElement(segment, "04", "1");
        a2000.addSegment(segment);
        Loop aa2010 = new Loop(separators, "2010AA");
        segment = new Segment("NM1");
        addElement(segment, "01", "85");
        addElement(segment, "02", "2");
        addElement(segment, "03", "MEDICAL GROUP");
        addElement(segment, "04", "");
        addElement(segment, "05", "");
        addElement(segment, "06", "");
        addElement(segment, "07", "");
        addElement(segment, "08", "XX");
        addElement(segment, "09", "1234567890");
        aa2010.addSegment(segment);
        segment = new Segment("N3");
        addElement(segment, "01", "3901 CALVERTON BLVD");
        aa2010.addSegment(segment);
        segment = new Segment("N4");
        addElement(segment, "01", "CALVERTON");
        addElement(segment, "02", "MD");
        addElement(segment, "03", "20705");
        aa2010.addSegment(segment);
        segment = new Segment("REF");
        addElement(segment, "01", "EI");
        addElement(segment, "02", "123456789");
        aa2010.addSegment(segment);
        segment = new Segment("PER");
        addElement(segment, "01", "IC");
        addElement(segment, "02", "JANE JONES");
        addElement(segment, "03", "TE");
        addElement(segment, "04", "3022893453");
        aa2010.addSegment(segment);
        segment = new Segment("PER");
        addElement(segment, "01", "IC");
        addElement(segment, "02", "JANE JONES");
        addElement(segment, "03", "TE");
        addElement(segment, "04", "3012833053");
        addElement(segment, "05", "EX");
        addElement(segment, "06", "201");
        aa2010.addSegment(segment);
        Loop ab2010 = new Loop(separators, "2010AB");
        segment = new Segment("NM1");
        addElement(segment, "01", "87");
        addElement(segment, "02", "2");
        ab2010.addSegment(segment);
        segment = new Segment("N3");
        addElement(segment, "01", "227 LASTNER LANE");
        ab2010.addSegment(segment);
        segment = new Segment("N4");
        addElement(segment, "01", "GREENBELT");
        addElement(segment, "02", "MD");
        addElement(segment, "03", "20770");
        ab2010.addSegment(segment);
        Loop b2000 = new Loop(separators, "2000B");
        segment = new Segment("HL");
        addElement(segment, "01", "2");
        addElement(segment, "02", "1");
        addElement(segment, "03", "22");
        addElement(segment, "04", "1");
        b2000.addSegment(segment);
        segment = new Segment("SBR");
        addElement(segment, "01", "P");
        addElement(segment, "02", "");
        addElement(segment, "03", "SUBSCRIBER GROUP");
        addElement(segment, "04", "");
        addElement(segment, "05", "");
        addElement(segment, "06", "");
        addElement(segment, "07", "");
        addElement(segment, "08", "");
        addElement(segment, "09", "CI");
        b2000.addSegment(segment);
        Loop ba2010 = new Loop(separators, "2010BA");
        segment = new Segment("NM1");
        addElement(segment, "01", "IL");
        addElement(segment, "02", "1");
        addElement(segment, "03", "DOE");
        addElement(segment, "04", "JOHN");
        addElement(segment, "05", "T");
        addElement(segment, "06", "");
        addElement(segment, "07", "JR");
        addElement(segment, "08", "MI");
        addElement(segment, "09", "123456");
        ba2010.addSegment(segment);
        segment = new Segment("N3");
        addElement(segment, "01", "123 MAIN STREET");
        addElement(segment, "02", "APARTMENT 9");
        ba2010.addSegment(segment);
        segment = new Segment("N4");
        addElement(segment, "01", "RIVERDALE");
        addElement(segment, "02", "MD");
        addElement(segment, "03", "20737");
        ba2010.addSegment(segment);
        segment = new Segment("DMG");
        addElement(segment, "01", "D8");
        addElement(segment, "02", "19611124");
        addElement(segment, "03", "M");
        ba2010.addSegment(segment);
        Loop bb2010 = new Loop(separators, "2010BB");
        segment = new Segment("NM1");
        addElement(segment, "01", "PR");
        addElement(segment, "02", "2");
        addElement(segment, "03", "HEALTH INSURANCE COMPANY");
        addElement(segment, "04", "");
        addElement(segment, "05", "");
        addElement(segment, "06", "");
        addElement(segment, "07", "");
        addElement(segment, "08", "PI");
        addElement(segment, "09", "11122333");
        bb2010.addSegment(segment);
        Loop l2300 = new Loop(separators, "2300");
        segment = new Segment("CLM");
        addElement(segment, "01", "A37YH556");
        addElement(segment, "02", "500");
        addElement(segment, "03", "");
        addElement(segment, "04", "");
        addElement(segment, "05", "11:B:1");
        addElement(segment, "06", "Y");
        addElement(segment, "07", "A");
        addElement(segment, "08", "Y");
        addElement(segment, "09", "I");
        addElement(segment, "10", "P");
        l2300.addSegment(segment);
        segment = new Segment("DTP");
        addElement(segment, "01", "435");
        addElement(segment, "02", "D8");
        addElement(segment, "03", "20200515");
        l2300.addSegment(segment);
        segment = new Segment("DTP");
        addElement(segment, "01", "096");
        addElement(segment, "02", "D8");
        addElement(segment, "03", "20200523");
        l2300.addSegment(segment);
        segment = new Segment("HI");
        addElement(segment, "01", "BK:1739");
        addElement(segment, "02", "");
        addElement(segment, "03", "");
        l2300.addSegment(segment);
        Loop l2400 = new Loop(separators, "2400");
        segment = new Segment("LX");
        addElement(segment, "01", "1");
        l2400.addSegment(segment);
        segment = new Segment("SV1");
        addElement(segment, "01", "HC:99211:25");
        addElement(segment, "02", "12.25");
        addElement(segment, "03", "UN");
        addElement(segment, "04", "5");
        addElement(segment, "05", "11");
        addElement(segment, "06", "");
        addElement(segment, "07", "1:2:3");
        addElement(segment, "08", "");
        addElement(segment, "09", "Y");
        l2400.addSegment(segment);
        segment = new Segment("DTP");
        addElement(segment, "01", "472");
        addElement(segment, "02", "RD8");
        addElement(segment, "03", "20200524-20200528");
        l2400.addSegment(segment);
        segment = new Segment("DTP");
        addElement(segment, "01", "304");
        addElement(segment, "02", "D8");
        addElement(segment, "03", "20200530");
        l2400.addSegment(segment);
        l2300.getLoops().add(l2400);
        b2000.getLoops().add(l2300);
        b2000.getLoops().add(bb2010);
        b2000.getLoops().add(ba2010);
        a2000.getLoops().add(b2000);
        a2000.getLoops().add(ab2010);
        a2000.getLoops().add(aa2010);
        detail.getLoops().add(a2000);
        stLoop.getLoops().add(detail);

        X12Writer writer = new X12Writer(FileType.ANSI837_5010_X222, Collections.singletonList(isaLoop), separators);
        String writerResult = writer.toX12String(lineBreak).trim();

        assertEquals(expected, writerResult);
    }

    private void addElement(Segment segment, String elementNum, String data) {
        segment.addElement(new Element(segment.getId() + elementNum, data));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testToMap() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");

        X12Reader fromFileUtf8 = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()),
                StandardCharsets.UTF_8);

        X12Writer writer = new X12Writer(fromFileUtf8);
        List<Map<String, Object>> listOfMap = writer.toListOfMap();

        List<String> types = JsonPath.parse(listOfMap).read("$..type");

        assertEquals(20, types.stream().filter("loop"::equals).count());
        assertEquals(38, types.stream().filter("segment"::equals).count());
        assertEquals(200, types.stream().filter("element"::equals).count());
        assertEquals(16, types.stream().filter("composite"::equals).count());

        List<Map<String, Object>> loopsAndSegmentsByName = JsonPath.parse(listOfMap).read("$..[?(@.name=='Interchange Control Header')]");
        assertEquals(2, loopsAndSegmentsByName.size());

        List<Map<String, Object>> elmByName = JsonPath.parse(listOfMap).read("$..[?(@.name=='Functional Identifier Code')]");
        assertEquals(1, elmByName.size());
        assertEquals("HC", elmByName.get(0).get("value"));
        List<Map<String, Object>> compositeList = JsonPath.parse(listOfMap).read("$..[?(@.type=='composite')]");
        assertEquals("Health Care Service Location Information", compositeList.get(0).get("name"));
        assertEquals("11:B:1", compositeList.get(0).get("value"));
        List<Map<String, Object>> subElements = (List<Map<String, Object>>)compositeList.get(0).get("subElements");
        assertEquals(3, subElements.size());
        Map<String, Object> subElement = subElements.get(0);
        assertEquals("11", subElement.get("value"));
        assertEquals("Place of Service Code", subElement.get("name"));
        assertEquals("compositeValue", subElement.get("type"));
        assertEquals("CLM05-01", subElement.get("xid"));
    }
}
