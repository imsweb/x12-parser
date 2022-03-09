package com.imsweb.x12;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoopTest {

    @Test
    void testLoop() {
        Loop loop = new Loop(new Separators('~', '*', ':'), "ISA");
        assertNotNull(loop);
        assertEquals(new Separators('~', '*', ':'), loop.getSeparators());
        assertEquals(new Separators(), loop.getSeparators());

        loop = new Loop("ISA");
        assertEquals(new Separators('~', '*', ':'), loop.getSeparators());
        assertEquals(new Separators(), loop.getSeparators());

        loop = new Loop(new Separators('A', 'B', 'C'), "ISA");
        assertEquals(new Separators('A', 'B', 'C'), loop.getSeparators());
    }

    @Test
    void testAddChildString() {
        Loop loop = new Loop("ISA");
        Loop child = loop.addLoop("GS");
        assertNotNull(child);
    }

    @Test
    void testAddChildIntLoop() {
        Loop loop = new Loop("ISA");
        Loop gs = new Loop("GS");
        Loop st = new Loop("ST");
        loop.addLoop(0, gs);
        loop.addLoop(1, st);
        assertEquals("ST", loop.getLoop(1).getId());
    }

    @Test
    void testAddSegment() {
        Loop loop = new Loop("ST");
        Segment s = loop.addSegment();
        assertNotNull(s);
    }

    @Test
    void testAddSegmentString() {
        Loop loop = new Loop("ST");
        loop.addSegment("ST*835*000000001");
        assertEquals("ST", loop.getSegment(0).getId());
    }

    @Test
    void testAddSegmentSegment() {
        Loop loop = new Loop("ST");
        Segment segment = new Segment(new Separators('~', '*', ':'));
        segment.addElements("ST*835*000000001");
        loop.addSegment(segment);
        assertEquals("ST", loop.getSegment(0).getId());
    }

    @Test
    void testAddSegmentInt() {
        Loop loop = new Loop("ST");
        loop.addSegment("BPR*DATA*NOT*VALID*RANDOM*TEXT");
        loop.addSegment("TRN*1*0000000000*1999999999");
        loop.addSegment("DTM*111*20090915");
        Segment segment = new Segment(new Separators('~', '*', ':'));
        segment.addElements("ST*835*000000001");
        loop.addSegment(0, segment);
        assertEquals("ST", loop.getSegment(0).getId());
    }

    @Test
    void testAddSegmentIntString() {
        Loop loop = new Loop("ST");
        loop.addSegment("BPR*DATA*NOT*VALID*RANDOM*TEXT");
        loop.addSegment("TRN*1*0000000000*1999999999");
        loop.addSegment("DTM*111*20090915");
        loop.addSegment(0, "ST*835*000000001");
        assertEquals("ST", loop.getSegment(0).getId());
    }

    @Test
    void testAddSegmentIntSegment() {
        Loop loop = new Loop("ST");
        loop.addSegment("ST*835*000000001");
        loop.addSegment("BPR*DATA*NOT*VALID*RANDOM*TEXT");
        loop.addSegment("DTM*111*20090915");
        Segment segment = new Segment(new Separators('~', '*', ':'));
        segment.addElements("ST*835*000000001");
        loop.addSegment(2, "TRN*1*0000000000*1999999999");
        assertEquals("TRN", loop.getSegment(2).getId());
    }

    @Test
    void testAddChildIntString() {
        Loop loop = new Loop("ISA");
        loop.addLoop("GS");
        loop.addLoop(1, "ST");
        assertEquals("ST", loop.getLoop(1).getId());
    }

    @Test
    void testHasLoop() {
        Loop loop = new Loop("ISA");
        loop.addLoop("GS");
        loop.addLoop("ST");
        assertTrue(loop.hasLoop("ST"));
    }

    @Test
    void testFindLoop() {
        Loop loop = new Loop("ISA");
        loop.addLoop("GS");
        loop.addLoop("ST");
        loop.addLoop("1000A");
        loop.addLoop("1000B");
        loop.addLoop("2000");
        loop.addLoop("2100");
        loop.addLoop("2110");
        loop.addLoop("GE");
        loop.addLoop("IEA");
        List<Loop> loops = loop.findLoop("2000");
        assertEquals(1, loops.size());
    }

    @Test
    void testFindSegment() {
        Loop loop = new Loop("ISA");
        loop.addLoop("GS");
        loop.addLoop("ST");
        loop.addLoop("1000A");
        loop.addLoop("1000B");
        Loop child1 = loop.addLoop("2000");
        child1.addSegment("LX*1");
        Loop child2 = loop.addLoop("2000");
        child2.addSegment("LX*2");
        loop.addLoop("2100");
        loop.addLoop("2110");
        loop.addLoop("GE");
        loop.addLoop("IEA");
        List<Segment> segments = loop.findSegment("LX");
        assertEquals(0, segments.size());

        loop = loop.getLoop("1000A");
        segments = loop.findSegment("LX");
        assertEquals(0, segments.size());

        loop = child1;
        segments = loop.findSegment("LX");
        assertEquals(1, segments.size());

        loop = child2;
        segments = loop.findSegment("LX");
        assertEquals(1, segments.size());
    }

    @Test
    void testGetSeparators() {
        Loop loop = new Loop("ISA");
        assertEquals("[~,*,:]", loop.getSeparators().toString());
    }

    @Test
    void testGetLoop() {
        Loop loop = new Loop("X12");
        loop.addLoop("ISA");
        loop.addLoop("GS");
        loop.addLoop("ST");
        loop.addLoop("1000A");
        loop.addLoop("1000B");
        loop.addLoop("2000");
        loop.addLoop("2100");
        loop.addLoop("2110");
        loop.addLoop("GE");
        loop.addLoop("IEA");
        assertEquals("1000A", loop.getLoop(3).getId());
    }

    @Test
    void testGetSegment() {
        Loop loop = new Loop("ST");
        loop.addSegment("ST*835*000000001");
        loop.addSegment("BPR*DATA*NOT*VALID*RANDOM*TEXT");
        loop.addSegment("DTM*111*20090915");
        assertEquals("DTM", loop.getSegment(2).getId());
    }

    @Test
    void testGetName() {
        Loop loop = new Loop("ST");
        assertEquals("ST", loop.getId());
    }

    @Test
    void testIterator() {
        Loop loop = new Loop("ST");
        assertNotNull(loop.iterator());
    }

    @Test
    void testRemoveLoop() {
        Loop loop = new Loop("X12");
        loop.addLoop("ISA");
        loop.addLoop("GS");
        loop.addLoop("ST");
        loop.addLoop("1000A");
        loop.addLoop("1000B");
        loop.addLoop("2000");
        loop.addLoop("2100");
        loop.addLoop("2110");
        loop.addLoop("SE");
        loop.addLoop("GE");
        loop.addLoop("IEA");

        Loop l1 = loop.removeLoop(3);
        assertEquals("1000A", l1.getId());

        Loop l2 = loop.removeLoop(0);
        assertEquals("ISA", l2.getId());
    }

    @Test
    void testRemoveSegment() {
        Loop loop = new Loop("ST");
        loop.addSegment("BPR*DATA*NOT*VALID*RANDOM*TEXT");
        loop.addSegment("TRN*1*0000000000*1999999999");
        loop.addSegment("DTM*111*20090915");
        loop.addSegment(0, "ST*835*000000001");

        Segment s = loop.removeSegment(2);
        assertEquals("TRN*1*0000000000*1999999999", s.toString());
        assertEquals(3, loop.size());
    }

    @Test
    void testChildList() {
        Loop loop = new Loop("X12");
        loop.addLoop("ISA");
        loop.addLoop("GS");
        loop.addLoop("ST");
        loop.addLoop("1000A");
        loop.addLoop("1000B");
        loop.addLoop("2000");
        loop.addLoop("2100");
        loop.addLoop("2110");
        loop.addLoop("SE");
        loop.addLoop("GE");
        loop.addLoop("IEA");
        List<Loop> loops = loop.getLoops();
        assertEquals(11, loops.size());
    }

    @Test
    void testSize() {
        Loop loop = new Loop("ST");
        loop.addSegment("ST*835*000000001");
        loop.addSegment("BPR*DATA*NOT*VALID*RANDOM*TEXT");
        loop.addSegment("DTM*111*20090915");
        assertEquals(3, loop.size());
    }

    @Test
    void testSetSeparators() {
        Loop loop = new Loop(new Separators('a', 'b', 'c'), "ST");
        Separators separators = new Separators('~', '*', ':');
        loop.setSeparators(separators);
        assertEquals("[~,*,:]", loop.getSeparators().toString());
    }

    @Test
    void testSetChildIntString() {
        Loop loop = new Loop("X12");
        loop.addLoop("ISA");
        loop.addLoop("GS");
        loop.addLoop("XX");
        loop.addLoop("1000A");
        loop.addLoop("1000B");
        loop.addLoop("2000");
        loop.addLoop("2100");
        loop.addLoop("2110");
        loop.addLoop("GE");
        loop.addLoop("IEA");
        loop.setLoop(2, "ST"); // test
        assertEquals("ST", loop.getLoop(2).getId());
    }

    @Test
    void testSetChildIntLoop() {
        Loop loop = new Loop("X12");
        loop.addLoop("ISA");
        loop.addLoop("GS");
        loop.addLoop("XX");
        loop.addLoop("1000A");
        loop.addLoop("1000B");
        loop.addLoop("2000");
        loop.addLoop("2100");
        loop.addLoop("2110");
        loop.addLoop("GE");
        loop.addLoop("IEA");
        loop.setLoop(2, new Loop("ST"));
        assertEquals("ST", loop.getLoop(2).getId());
    }

    @Test
    void testSetSegmentInt() {
        Loop loop = new Loop("ST");
        loop.addSegment("NOT*THE*RIGHT*SEGMENT");
        loop.addSegment("BPR*DATA*NOT*VALID*RANDOM*TEXT");
        loop.addSegment("TRN*1*0000000000*1999999999");
        loop.addSegment("DTM*111*20090915");
        Segment segment = new Segment(new Separators('~', '*', ':'));
        segment.addElements("ST*835*000000001");
        loop.setSegment(0, segment);
        assertEquals("ST", loop.getSegment(0).getId());
    }

    @Test
    void testSetSegmentIntSegment() {
        Loop loop = new Loop("ST");
        loop.addSegment("ST*835*000000001");
        loop.addSegment("BPR*DATA*NOT*VALID*RANDOM*TEXT");
        loop.addSegment("DTM*111*20090915");
        loop.addSegment("NOT*THE*RIGHT*SEGMENT");
        loop.setSegment(2, "TRN*1*0000000000*1999999999");
        assertEquals("TRN", loop.getSegment(2).getId());
    }

    @Test
    void testSetName() {
        Loop loop = new Loop("AB");
        loop.setId("ST");
        assertEquals("ST", loop.getId());
    }

    @Test
    void testToString() {
        Loop loop = new Loop("ST");
        loop.addSegment("ST*835*000000001");
        assertEquals("ST*835*000000001~", loop.toString());
    }

    @Test
    void testToXML() {
        Loop loop = new Loop("ST");
        loop.addSegment("ST*835*000000001");
        assertEquals("<loop id=\"ST\">\n"
                + "  <segments>\n"
                + "    <segment id=\"ST\">\n"
                + "      <elements>\n"
                + "        <element id=\"ST01\">835</element>\n"
                + "        <element id=\"ST02\">000000001</element>\n"
                + "      </elements>\n"
                + "    </segment>\n"
                + "  </segments>\n"
                + "  <loops/>\n"
                + "</loop>", loop.toXML());
    }

}
