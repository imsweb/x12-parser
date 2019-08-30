package com.imsweb.x12.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.imsweb.x12.Loop;
import com.imsweb.x12.mapping.TransactionDefinition;
import com.imsweb.x12.reader.X12Reader.FileType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class X12ReaderTest {

    //    @Test
    //    public void testDifferentSeparators() throws IOException {
    //        URL url = this.getClass().getResource("/837_5010/x12_valid_different_separators.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //        validate837Valid(reader.getLoops().get(0));
    //    }
    //
    //    @Test
    //    public void testConstructors() throws IOException {
    //        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
    //
    //        X12Reader fromFile = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //        X12Reader fromFileUtf8 = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()), StandardCharsets.UTF_8);
    //        X12Reader fromInputStream = new X12Reader(FileType.ANSI837_5010_X222, new FileInputStream(url.getFile()));
    //        X12Reader fromInputStreamUtf8 = new X12Reader(FileType.ANSI837_5010_X222, new FileInputStream(url.getFile()), StandardCharsets.UTF_8);
    //        X12Reader fromReaderUtf8 = new X12Reader(FileType.ANSI837_5010_X222, new BufferedReader(new InputStreamReader(new FileInputStream(url.getFile()), StandardCharsets.UTF_8)));
    //
    //        assertEquals(fromFile.getLoops().get(0).toString(), fromInputStream.getLoops().get(0).toString());
    //        assertEquals(fromFileUtf8.getLoops().get(0).toString(), fromInputStreamUtf8.getLoops().get(0).toString());
    //        assertEquals(fromFileUtf8.getLoops().get(0).toString(), fromReaderUtf8.getLoops().get(0).toString());
    //    }
    //
    //    @Test
    //    public void testMultipleGSLoops() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_multiple_gs.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //        validateMultipleGSLoops(reader.getLoops().get(0));
    //
    //    }
    //
    //    @Test
    //    public void testMultipleISALoops() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_multiple_isa.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //        validateMultipleISALoops(reader.getLoops());
    //    }
    //
    //    @Test
    //    public void testMultipleSTLoops() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_multiple_st.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //
    //        validateMultipleSTLoops(reader.getLoops().get(0));
    //    }
    //
    //    @Test
    //    public void testMarkingFiles() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
    //        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(url.getFile()), StandardCharsets.UTF_8))) {
    //            String line = null;
    //
    //            reader.mark(1);
    //            for (int i = 0; i < 10; i++)
    //                line = reader.readLine();
    //
    //            assertEquals("N3*3901 CALVERTON BLVD~", line);
    //
    //            reader.reset();
    //            line = reader.readLine();
    //            assertEquals("ISA*00*          *01*SECRET    *ZZ*SUBMITTERS.ID  *ZZ*RECEIVERS.ID   *030101*1253*U*00501*000000905*1*T*:~", line);
    //        }
    //    }
    //
    //    @Test
    //    public void testNewGetMethods() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //        Loop loop = reader.getLoops().get(0);
    //
    //        Loop test = reader.getLoops().get(0).getLoop("2000B");
    //
    //        //valid loop, no indices
    //        assertEquals("123456", loop.getElement("2010BA", "NM1", "NM109"));
    //        //valid loop, indicies
    //        assertEquals("3", loop.getElement("2000B", 1, "HL", 0, "HL02"));
    //        assertEquals("TE", loop.getElement("2010AA", 0, "PER", 1, "PER03"));
    //        //valid segement, indices
    //        assertEquals("1", test.getElement("HL", 0, "HL02"));
    //        //valid segment, no indices
    //        assertEquals("1", test.getElement("HL", "HL02"));
    //        //segment does not exist
    //        assertNull(test.getElement("HL", 1, "HL02"));
    //        //loop does not exist
    //        assertNull(loop.getElement("2000B", 2, "HL", 0, "HL02"));
    //        //loop exists
    //        assertNull(loop.getElement("2000B", 1, "MEA", 0, "MEA02"));
    //        //element does not exist
    //        assertNull(loop.getElement("2000B", 1, "HL", 0, "HL17"));
    //    }
    //
    //    @Test
    //    public void testWithFileConstructor() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //
    //        validate837Valid(reader.getLoops().get(0));
    //    }
    //
    //    @Test
    //    public void testWithReadableConstructor() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //
    //        validate837Valid(reader.getLoops().get(0));
    //    }
    //
    //    @Test
    //    public void testWithInputStreamConstructor() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new FileInputStream(new File(url.getFile())));
    //
    //        validate837Valid(reader.getLoops().get(0));
    //    }
    //
    //    @Test
    //    public void testBadValidCode() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_bad_valid_code.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //
    //        List<String> errors = reader.getErrors();
    //
    //        assertEquals(7, errors.size());
    //        assertTrue(errors.contains("Unable to find a matching segment format in loop 2000A"));
    //        // assertTrue(errors.contains("2010AA is required but missing"));
    //    }
    //
    //    @Test
    //    public void testSegmentErrors() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_segment_errors.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //
    //        List<String> errors = reader.getErrors();
    //
    //        assertEquals(3, errors.size());
    //        assertTrue(errors.contains("N3 in loop 2010AA is required but not found"));
    //        assertTrue(errors.contains("HI in loop 2300 is required but not found"));
    //        assertTrue(errors.contains("REF in loop 2010AA appears too many times"));
    //    }
    //
    //    //tests a file that has segments with missing data that is required.
    //    @Test
    //    public void testBadSegementIdentifier() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_bad_segment_identifier.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //
    //        List<String> errors = reader.getErrors();
    //
    //        assertEquals(6, errors.size());
    //        assertTrue(errors.contains("Unable to find a matching segment format in loop 2010AA"));
    //        assertTrue(errors.contains("Unable to find a matching segment format in loop 2000B"));
    //        assertTrue(errors.contains("Unable to find a matching segment format in loop 1000A"));
    //        assertTrue(errors.contains("N4 in loop 2010AA is required but not found"));
    //        assertTrue(errors.contains("PER in loop 1000A is required but not found"));
    //        assertTrue(errors.contains("2010BA is required but not found in 2000B iteration #1"));
    //    }
    //
    //    //tests a file that has segments with missing data that is required.
    //    @Test
    //    public void testMissingRequiredElements() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_missing_elements.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //
    //        List<String> errors = reader.getErrors();
    //        assertEquals(2, errors.size());
    //        assertTrue(errors.contains("NM1 in loop 1000B is missing a required element at 2"));
    //        assertTrue(errors.contains("CLM in loop 2300 is missing a required composite element at 5"));
    //    }
    //
    //    //tests a loop that appears too many times with the parent loop appearing once
    //    @Test
    //    public void testExceedsRepeatsOneParents() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_loop_errors1_exceeds_max_1_parent.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //
    //        List<String> errors = reader.getErrors();
    //
    //        assertEquals(1, errors.size());
    //        assertTrue(errors.contains("2010AA appears too many times"));
    //    }
    //
    //    //tests a loop that appears too many times with a parent loop that appears more than once
    //    @Test
    //    public void testExceedRepeatsMultipleParents() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_loop_errors2_exceeds_max_repeats_mulitple_parents.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //
    //        List<String> errors = reader.getErrors();
    //
    //        assertEquals(1, errors.size());
    //        assertTrue(errors.contains("2010AA appears too many times"));
    //    }
    //
    //    //tests a missing required loop
    //    @Test
    //    public void testMissingLoop() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_loop_errors3_missing_loops.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //
    //        List<String> errors = reader.getErrors();
    //        assertEquals(1, errors.size());
    //
    //        assertTrue(errors.contains("2010AA is required but not found in 2000A iteration #1"));
    //    }
    //
    //    //tests a loop that appears the correct number of times but in a parent that appears more than once.
    //    @Test
    //    public void testValidLoopStructure() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_no_errors.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //
    //        List<String> errors = reader.getErrors();
    //
    //        assertEquals(0, errors.size());
    //    }
    //
    //    //tests a loop that appears the correct number of times but in a parent that appears more than once.
    //    @Test
    //    public void testMissingLoopInOneRepeat() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_missing_required_loop_in_one_repeat.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //
    //        List<String> errors = reader.getErrors();
    //
    //        assertEquals(1, errors.size());
    //
    //        assertTrue(errors.contains("2010BA is required but not found in 2000B iteration #1"));
    //    }
    //
    //    @Test
    //    public void testDefinitions() {
    //        for (FileType type : FileType.values()) {
    //            TransactionDefinition definition = type.getDefinition();
    //
    //            assertNotNull(definition);
    //            assertNotNull(definition.getLoop());
    //
    //            // call a second time to make sure cache is working
    //            definition = type.getDefinition();
    //
    //            assertNotNull(definition);
    //            assertNotNull(definition.getLoop());
    //        }
    //    }
    //
    //    @Test
    //    public void testToXml() throws IOException {
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(this.getClass().getResource("/837_5010/x12_no_errors.txt").getFile()));
    //
    //        String xml = reader.getLoops().get(0).toXML();
    //        assertTrue(xml.length() > 0);
    //        assertTrue(xml.startsWith("<loop id=\"ISA_LOOP\">"));
    //    }
    //
    //    @Test
    //    public void testToJson() throws IOException {
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(this.getClass().getResource("/837_5010/x12_no_errors.txt").getFile()));
    //
    //        String json = reader.getLoops().get(0).toJson();
    //        assertTrue(json.length() > 0);
    //        assertTrue(json.startsWith("{\n  \"id\": \"ISA_LOOP\",\n  \"segments\""));
    //
    //        // TODO test JSON equality
    //        //        reader = new X12Reader(FileType.ANSI837_5010_X222, new File(this.getClass().getResource("/837_5010/x12_multiple_gs.txt").getFile()));
    //        //
    //        //        json = reader.getLoop().toJson();
    //        //
    //        //        System.out.println(json);
    //    }
    //
    //    @Test
    //    public void testMissingRequiredLoopInMultipleRepeats() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_missing_required_loops_mult_repeats.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //
    //        List<String> errors = reader.getErrors();
    //        assertEquals(4, errors.size());
    //
    //        assertTrue(errors.contains("Unable to find a matching segment format in loop 2300"));
    //        assertTrue(errors.contains("2400 is required but not found in 2300 iteration #2"));
    //        assertTrue(errors.contains("2400 is required but not found in 2300 iteration #5"));
    //    }
    //
    //    /**
    //     * Valid output for a testing file
    //     */
    //    private void validate837Valid(Loop loop) {
    //        assertEquals(1, loop.getLoops().size());
    //        assertEquals(1, loop.getLoop("GS_LOOP").getLoops().size());
    //        assertEquals(2, loop.getLoop("ST_LOOP").getLoops().size());
    //        assertEquals(2, loop.getLoop("HEADER").getLoops().size());
    //        assertEquals(0, loop.getLoop("1000A").getLoops().size());
    //        assertEquals(0, loop.getLoop("1000B").getLoops().size());
    //        assertEquals(1, loop.getLoop("DETAIL").getLoops().size());
    //        assertEquals(4, loop.getLoop("2000A").getLoops().size());
    //        assertEquals(0, loop.getLoop("2010AA").getLoops().size());
    //        assertEquals(0, loop.getLoop("2010AB").getLoops().size());
    //        assertEquals(2, loop.findLoop("2000B").size());
    //        assertEquals(3, loop.getLoop("2000B", 0).getLoops().size());
    //        assertEquals(0, loop.getLoop("2010BA", 0).getLoops().size());
    //        assertEquals(0, loop.getLoop("2010BB", 0).getLoops().size());
    //        assertEquals(1, loop.getLoop("2300", 0).getLoops().size());
    //        assertEquals(0, loop.getLoop("2400", 0).getLoops().size());
    //        assertEquals(3, loop.getLoop("2000B", 1).getLoops().size());
    //        assertEquals(0, loop.getLoop("2010BA", 1).getLoops().size());
    //        assertEquals(0, loop.getLoop("2010BB", 1).getLoops().size());
    //        assertEquals(1, loop.getLoop("2300", 1).getLoops().size());
    //        assertEquals(0, loop.getLoop("2400", 1).getLoops().size());
    //
    //        //Testing null results
    //        assertNull(loop.getLoop("2400", 2));
    //        assertNull(loop.getLoop("2310"));
    //        assertNull(loop.getLoop("2400").getSegment("MEA"));
    //        assertNull(loop.getLoop("2000B").getSegment("HL", 2));
    //        try {
    //            assertNull(loop.getLoop("2400", 2).getSegment("MEA"));
    //            fail("Expecting a null pointer exception to be thrown (test #1)");
    //        }
    //        catch (NullPointerException e) {
    //            //Expected
    //        }
    //        try {
    //            assertNull(loop.getLoop("2400").getSegment("MEA").getElementValue("MEA02"));
    //            fail("Expecting a null pointer exception to be thrown (test #2)");
    //        }
    //        catch (NullPointerException e) {
    //            //Expected
    //        }
    //
    //        // testing loopID match for each subloop
    //        assertEquals("GS_LOOP", loop.getLoops().get(0).getId());
    //        assertEquals("ST_LOOP", loop.getLoop("GS_LOOP").getLoops().get(0).getId());
    //        assertEquals("HEADER", loop.getLoop("ST_LOOP").getLoops().get(0).getId());
    //        assertEquals("DETAIL", loop.getLoop("ST_LOOP").getLoops().get(1).getId());
    //        assertEquals("1000A", loop.getLoop("HEADER").getLoops().get(0).getId());
    //        assertEquals("1000B", loop.getLoop("HEADER").getLoops().get(1).getId());
    //        assertEquals("2000A", loop.getLoop("DETAIL").getLoops().get(0).getId());
    //        assertEquals("2010AA", loop.getLoop("2000A").getLoops().get(0).getId());
    //        assertEquals("2010AB", loop.getLoop("2000A").getLoops().get(1).getId());
    //        assertEquals("2000B", loop.getLoop("2000A").getLoops().get(2).getId());
    //        assertEquals("2000B", loop.getLoop("2000A").getLoops().get(3).getId());
    //        assertEquals("2010BA", loop.getLoop("2000B", 0).getLoops().get(0).getId());
    //        assertEquals("2010BB", loop.getLoop("2000B", 0).getLoops().get(1).getId());
    //        assertEquals("2300", loop.getLoop("2000B", 0).getLoops().get(2).getId());
    //        assertEquals("2400", loop.getLoop("2300", 0).getLoops().get(0).getId());
    //        assertEquals("2010BA", loop.getLoop("2000B", 1).getLoops().get(0).getId());
    //        assertEquals("2010BB", loop.getLoop("2000B", 1).getLoops().get(1).getId());
    //        assertEquals("2300", loop.getLoop("2000B", 1).getLoops().get(2).getId());
    //        assertEquals("2400", loop.getLoop("2300", 1).getLoops().get(0).getId());
    //
    //        // testing grabbing data from each line
    //        assertEquals("030101", loop.getSegment("ISA").getElementValue("ISA09"));//First loop
    //        assertEquals("19991231", loop.getLoop("GS_LOOP").getSegment("GS").getElementValue("GS04"));
    //        assertEquals("987654", loop.getLoop("ST_LOOP").getSegment("ST").getElementValue("ST02"));
    //        assertEquals("0932", loop.getLoop("HEADER").getSegment("BHT").getElementValue("BHT05"));
    //        assertEquals("NM1", loop.getLoop("1000A").getSegment("NM1").getId());
    //        assertEquals("3016809770", loop.getLoop("1000A").getSegment("PER").getElementValue("PER04"));
    //        assertEquals("2", loop.getLoop("1000B").getSegment("NM1").getElementValue("NM102"));
    //        assertEquals("", loop.getLoop("2000A").getSegment("HL").getElementValue("HL02"));
    //        assertEquals("85", loop.getLoop("2010AA").getSegment("NM1").getElementValue("NM101"));
    //        assertEquals("N3", loop.getLoop("2010AA").getSegment("N3").getId());
    //        assertEquals("20705", loop.getLoop("2010AA").getSegment("N4").getElementValue("N403"));
    //        assertEquals("EI", loop.getLoop("2010AA").getSegment("REF").getElementValue("REF01"));
    //        assertEquals("JANE JONES", loop.getLoop("2010AA").getSegment("PER").getElementValue("PER02"));
    //        assertEquals("201", loop.getLoop("2010AA").getSegment("PER", 1).getElementValue("PER06"));
    //        assertEquals("87", loop.getLoop("2010AB").getSegment("NM1").getElementValue("NM101"));
    //        assertEquals("227 LASTNER LANE", loop.getLoop("2010AB").getSegment("N3").getElementValue("N301"));
    //        assertEquals("GREENBELT", loop.getLoop("2010AB").getSegment("N4").getElementValue("N401"));
    //        assertEquals("22", loop.getLoop("2000B").getSegment("HL").getElementValue("HL03"));
    //        assertEquals("", loop.getLoop("2000B").getSegment("SBR").getElementValue("SBR02"));
    //        assertEquals("123456", loop.getLoop("2010BA").getSegment("NM1").getElementValue("NM109"));
    //        assertEquals("PI", loop.getLoop("2010BB").getSegment("NM1").getElementValue("NM108"));
    //        assertEquals("A37YH556", loop.getLoop("2300").getSegment("CLM").getElementValue("CLM01"));
    //        assertEquals("BK", loop.getLoop("2300").getSegment("HI").getElementValue("HI01"));
    //        assertEquals("1", loop.getLoop("2400").getSegment("LX").getElementValue("LX01"));
    //        Character compositeSeparator = loop.getLoop("2400").getSegment("SV1").getSeparators().getCompositeElement();
    //        assertEquals("HC" + compositeSeparator + "99211" + compositeSeparator + "25", loop.getLoop("2400").getSegment("SV1").getElementValue("SV101"));
    //        assertEquals("RD8", loop.getLoop("2400").getSegment("DTP").getElementValue("DTP02"));
    //
    //        // Tests of differences between repeating loops
    //        assertEquals("1", loop.getLoop("2000B", 0).getSegment("HL").getElementValue("HL02"));
    //        assertEquals("3", loop.getLoop("2000B", 1).getSegment("HL").getElementValue("HL02"));
    //
    //        assertEquals("SUBSCRIBER GROUP", loop.getLoop("2000B", 0).getSegment("SBR").getElementValue("SBR03"));
    //        assertEquals("SUBSCRIBER GROUP TWO", loop.getLoop("2000B", 1).getSegment("SBR").getElementValue("SBR03"));
    //
    //        assertEquals("JOHN", loop.getLoop("2010BA", 0).getSegment("NM1").getElementValue("NM104"));
    //        assertEquals("DAVID", loop.getLoop("2010BA", 1).getSegment("NM1").getElementValue("NM104"));
    //
    //        assertEquals("HEALTH INSURANCE COMPANY", loop.getLoop("2010BB", 0).getSegment("NM1").getElementValue("NM103"));
    //        assertEquals("HEALTH INSURANCE COMPANY TWO", loop.getLoop("2010BB", 1).getSegment("NM1").getElementValue("NM103"));
    //
    //        assertEquals("A37YH556", loop.getLoop("2300", 0).getSegment("CLM").getElementValue("CLM01"));
    //        assertEquals("A37YH667", loop.getLoop("2300", 1).getSegment("CLM").getElementValue("CLM01"));
    //        assertEquals(1, loop.getLoop("2300", 1).getSegment("CLM").getElement("CLM01").getNumOfSubElements());
    //
    //        assertEquals("8901", loop.getLoop("2300", 0).getSegment("HI").getElementValue("HI02"));
    //        assertEquals("1987", loop.getLoop("2300", 1).getSegment("HI").getElementValue("HI02"));
    //        assertEquals(1, loop.getLoop("2300", 1).getSegment("HI").getElement("HI02").getNumOfSubElements());
    //
    //        assertEquals("1", loop.getLoop("2400", 0).getSegment("LX").getElementValue("LX01"));
    //        assertEquals("2", loop.getLoop("2400", 1).getSegment("LX").getElementValue("LX01"));
    //
    //        compositeSeparator = loop.getLoop("2400", 1).getSegment("SV1").getSeparators().getCompositeElement();
    //        assertEquals("HC" + compositeSeparator + "99211" + compositeSeparator + "25", loop.getLoop("2400", 0).getSegment("SV1").getElementValue("SV101"));
    //        assertEquals("HC" + compositeSeparator + "478331" + compositeSeparator + "25", loop.getLoop("2400", 1).getSegment("SV1").getElementValue("SV101"));
    //        assertEquals(3, loop.getLoop("2400", 1).getSegment("SV1").getElement("SV101").getNumOfSubElements());
    //        assertEquals("478331", loop.getLoop("2400", 1).getSegment("SV1").getElement("SV101", 1));
    //        assertNull(loop.getLoop("2400", 1).getSegment("SV1").getElement("SV101", 3));
    //        assertEquals(3, loop.getLoop("2400", 1).getSegment("SV1").getElement("SV107").getNumOfSubElements());
    //        assertEquals(1, loop.getLoop("2400", 0).getSegment("SV1").getElement("SV107").getNumOfSubElements());
    //
    //        assertEquals("20050314-20050325", loop.getLoop("2400", 0).getSegment("DTP").getElementValue("DTP03"));
    //        assertEquals("20050322-20050325", loop.getLoop("2400", 1).getSegment("DTP").getElementValue("DTP03"));
    //        assertEquals(1, loop.getLoop("2400", 1).getSegment("DTP").getElement("DTP03").getNumOfSubElements());
    //    }
    //
    //    private void validateMultipleGSLoops(Loop loop) {
    //        assertEquals(3, loop.getLoops().size());
    //        assertEquals(1, loop.getLoop("GS_LOOP", 0).getLoops().size());
    //        assertEquals(2, loop.getLoop("ST_LOOP").getLoops().size());
    //        assertEquals(2, loop.getLoop("HEADER").getLoops().size());
    //        assertEquals(0, loop.getLoop("1000A").getLoops().size());
    //        assertEquals(0, loop.getLoop("1000B").getLoops().size());
    //        assertEquals(1, loop.getLoop("DETAIL").getLoops().size());
    //        assertEquals(4, loop.getLoop("2000A").getLoops().size());
    //        assertEquals(0, loop.getLoop("2010AA").getLoops().size());
    //        assertEquals(0, loop.getLoop("2010AB").getLoops().size());
    //        assertEquals(6, loop.findLoop("2000B").size());
    //        assertEquals(3, loop.getLoop("2000B", 0).getLoops().size());
    //        assertEquals(0, loop.getLoop("2010BA", 0).getLoops().size());
    //        assertEquals(0, loop.getLoop("2010BB", 0).getLoops().size());
    //        assertEquals(1, loop.getLoop("2300", 0).getLoops().size());
    //        assertEquals(0, loop.getLoop("2400", 0).getLoops().size());
    //        assertEquals(3, loop.getLoop("2000B", 1).getLoops().size());
    //        assertEquals(0, loop.getLoop("2010BA", 1).getLoops().size());
    //        assertEquals(0, loop.getLoop("2010BB", 1).getLoops().size());
    //        assertEquals(1, loop.getLoop("2300", 1).getLoops().size());
    //        assertEquals(0, loop.getLoop("2400", 1).getLoops().size());
    //
    //        assertEquals(1, loop.getLoop("GS_LOOP", 1).getLoops().size());
    //        assertEquals(2, loop.getLoop(1).getLoop("ST_LOOP").getLoops().size());
    //        assertEquals(2, loop.getLoop(1).getLoop("HEADER").getLoops().size());
    //        assertEquals(0, loop.getLoop(1).getLoop("1000A").getLoops().size());
    //        assertEquals(0, loop.getLoop(1).getLoop("1000B").getLoops().size());
    //        assertEquals(1, loop.getLoop(1).getLoop("DETAIL").getLoops().size());
    //        assertEquals(4, loop.getLoop(1).getLoop("2000A").getLoops().size());
    //        assertEquals(0, loop.getLoop(1).getLoop("2010AA").getLoops().size());
    //        assertEquals(0, loop.getLoop(1).getLoop("2010AB").getLoops().size());
    //        assertEquals(3, loop.getLoop(1).getLoop("2000B", 0).getLoops().size());
    //        assertEquals(0, loop.getLoop(1).getLoop("2010BA", 0).getLoops().size());
    //        assertEquals(0, loop.getLoop(1).getLoop("2010BB", 0).getLoops().size());
    //        assertEquals(1, loop.getLoop(1).getLoop("2300", 0).getLoops().size());
    //        assertEquals(0, loop.getLoop(1).getLoop("2400", 0).getLoops().size());
    //        assertEquals(3, loop.getLoop(1).getLoop("2000B", 1).getLoops().size());
    //        assertEquals(0, loop.getLoop(1).getLoop("2010BA", 1).getLoops().size());
    //        assertEquals(0, loop.getLoop(1).getLoop("2010BB", 1).getLoops().size());
    //        assertEquals(1, loop.getLoop(1).getLoop("2300", 1).getLoops().size());
    //        assertEquals(0, loop.getLoop(1).getLoop("2400", 1).getLoops().size());
    //
    //        assertEquals("20050314-20050325", loop.getLoop(1).getLoop("2400", 0).getSegment("DTP").getElementValue("DTP03"));
    //        assertEquals("20010523-20010601", loop.getLoop(1).getLoop("2400", 1).getSegment("DTP").getElementValue("DTP03"));
    //
    //        assertEquals("GREENBELT", loop.getLoop(0).getLoop("2010AB").getSegment("N4").getElementValue("N401"));
    //        assertEquals("COLLEGE PARK", loop.getLoop(1).getLoop("2010AB").getSegment("N4").getElementValue("N401"));
    //
    //        assertEquals("DAVID ANGELASZEK", loop.getLoop(0).getLoop("1000A").getSegment("PER").getElementValue("PER02"));
    //        assertEquals("DAVID JEFFREY ANGELASZEK", loop.getLoop(2).getLoop("1000A").getSegment("PER").getElementValue("PER02"));
    //    }
    //
    //    private void validateMultipleISALoops(List<Loop> loops) {
    //        assertEquals(2, loops.size());
    //        for (Loop loop : loops) {
    //            assertEquals("ISA_LOOP", loop.getId());
    //            assertEquals(1, loop.getLoops().size());
    //            assertEquals(1, loop.getLoop("GS_LOOP", 0).getLoops().size());
    //            assertEquals(2, loop.getLoop("ST_LOOP").getLoops().size());
    //            assertEquals(2, loop.getLoop("HEADER").getLoops().size());
    //            assertEquals(0, loop.getLoop("1000A").getLoops().size());
    //            assertEquals(0, loop.getLoop("1000B").getLoops().size());
    //            assertEquals(1, loop.getLoop("DETAIL").getLoops().size());
    //            assertEquals(4, loop.getLoop("2000A").getLoops().size());
    //            assertEquals(0, loop.getLoop("2010AA").getLoops().size());
    //            assertEquals(0, loop.getLoop("2010AB").getLoops().size());
    //            assertEquals(2, loop.findLoop("2000B").size());
    //            assertEquals(3, loop.getLoop("2000B", 0).getLoops().size());
    //            assertEquals(0, loop.getLoop("2010BA", 0).getLoops().size());
    //            assertEquals(0, loop.getLoop("2010BB", 0).getLoops().size());
    //            assertEquals(2, loop.findLoop("2300").size());
    //            assertEquals(1, loop.getLoop("2300", 0).getLoops().size());
    //            assertEquals(1, loop.getLoop("2300", 1).getLoops().size());
    //            assertEquals(0, loop.getLoop("2400", 0).getLoops().size());
    //            assertEquals(3, loop.getLoop("2000B", 1).getLoops().size());
    //            assertEquals(0, loop.getLoop("2010BA", 1).getLoops().size());
    //            assertEquals(0, loop.getLoop("2010BB", 1).getLoops().size());
    //            assertEquals(0, loop.getLoop("2300", 0).getLoop("2400", 0).getLoops().size());
    //            assertEquals("20050314-20050325", loop.getLoop("2300").getLoop("2400", 0).getSegment("DTP").getElementValue("DTP03"));
    //            assertEquals("20050322-20050325", loop.getLoop("2300", 1).getLoop("2400", 0).getSegment("DTP").getElementValue("DTP03"));
    //            assertEquals("GREENBELT", loop.getLoop(0).getLoop("2010AB").getSegment("N4").getElementValue("N401"));
    //            assertEquals("DAVID ANGELASZEK", loop.getLoop(0).getLoop("1000A").getSegment("PER").getElementValue("PER02"));
    //            assertNotNull(loop.getLoop("2300", 1).getLoop("2400", 0).getSegment("IEA")); // test to ensure the final segment is included!
    //        }
    //    }
    //
    //    private void validateMultipleSTLoops(Loop loop) {
    //        assertEquals(1, loop.getLoops().size());
    //        assertEquals(2, loop.getLoop("GS_LOOP").getLoops().size());
    //        assertEquals(2, loop.getLoop("ST_LOOP").getLoops().size());
    //        assertEquals(2, loop.getLoop("HEADER").getLoops().size());
    //        assertEquals(0, loop.getLoop("1000A").getLoops().size());
    //        assertEquals(0, loop.getLoop("1000B").getLoops().size());
    //        assertEquals(1, loop.getLoop("DETAIL").getLoops().size());
    //        assertEquals(4, loop.getLoop("2000A").getLoops().size());
    //        assertEquals(0, loop.getLoop("2010AA").getLoops().size());
    //        assertEquals(0, loop.getLoop("2010AB").getLoops().size());
    //        assertEquals(4, loop.findLoop("2000B").size());
    //        assertEquals(3, loop.getLoop("2000B", 0).getLoops().size());
    //        assertEquals(0, loop.getLoop("2010BA", 0).getLoops().size());
    //        assertEquals(0, loop.getLoop("2010BB", 0).getLoops().size());
    //        assertEquals(1, loop.getLoop("2300", 0).getLoops().size());
    //        assertEquals(0, loop.getLoop("2400", 0).getLoops().size());
    //        assertEquals(3, loop.getLoop("2000B", 1).getLoops().size());
    //        assertEquals(0, loop.getLoop("2010BA", 1).getLoops().size());
    //        assertEquals(0, loop.getLoop("2010BB", 1).getLoops().size());
    //        assertEquals(1, loop.getLoop("2300", 1).getLoops().size());
    //        assertEquals(0, loop.getLoop("2400", 1).getLoops().size());
    //
    //        assertEquals(2, loop.getLoop("ST_LOOP", 1).getLoops().size());
    //        assertEquals(2, loop.getLoop("ST_LOOP", 1).getLoop("HEADER").getLoops().size());
    //        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("1000A").getLoops().size());
    //        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("1000B").getLoops().size());
    //        assertEquals(1, loop.getLoop("ST_LOOP", 1).getLoop("DETAIL").getLoops().size());
    //        assertEquals(4, loop.getLoop("ST_LOOP", 1).getLoop("2000A").getLoops().size());
    //        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010AA").getLoops().size());
    //        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010AB").getLoops().size());
    //        assertEquals(3, loop.getLoop("ST_LOOP", 1).getLoop("2000B", 0).getLoops().size());
    //        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010BA", 0).getLoops().size());
    //        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010BB", 0).getLoops().size());
    //        assertEquals(1, loop.getLoop("ST_LOOP", 1).getLoop("2300", 0).getLoops().size());
    //        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2400", 0).getLoops().size());
    //        assertEquals(3, loop.getLoop("ST_LOOP", 1).getLoop("2000B", 1).getLoops().size());
    //        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010BA", 1).getLoops().size());
    //        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010BB", 1).getLoops().size());
    //        assertEquals(1, loop.getLoop("ST_LOOP", 1).getLoop("2300", 1).getLoops().size());
    //        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2400", 1).getLoops().size());
    //
    //        assertEquals("227 LASTNER LANE", loop.getLoop("ST_LOOP", 0).getLoop("2010AB").getSegment("N3").getElementValue("N301"));
    //        assertEquals("21 CRAMER PATH", loop.getLoop("ST_LOOP", 1).getLoop("2010AB").getSegment("N3").getElementValue("N301"));
    //
    //        assertEquals("JANE JANES", loop.getLoop("ST_LOOP", 0).getLoop("2010AA").getSegment("PER", 1).getElementValue("PER02"));
    //        assertEquals("JANICE JONES", loop.getLoop("ST_LOOP", 1).getLoop("2010AA").getSegment("PER", 1).getElementValue("PER02"));
    //
    //        assertEquals("HEALTH INSURANCE COMPANY TWO", loop.getLoop("ST_LOOP", 0).getLoop("2010BB", 1).getSegment("NM1").getElementValue("NM103"));
    //        assertEquals("AN INSURANCE COMPANY", loop.getLoop("ST_LOOP", 1).getLoop("2010BB", 1).getSegment("NM1").getElementValue("NM103"));
    //    }
    //
    //    @Test
    //    public void testSegmentsNotInOrder() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_segments_out_of_order.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //
    //        List<String> errors = reader.getErrors();
    //
    //        assertEquals(2, errors.size());
    //
    //        assertTrue(errors.contains("Segment N4 in loop 2010AA is not in the correct position."));
    //        assertTrue(errors.contains("Segment N3 in loop 2010AB is not in the correct position."));
    //    }
    //
    //    @Test
    //    public void testBadFirstLine() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_bad_first_line.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //        assertEquals(1, reader.getErrors().size());
    //        assertTrue(reader.getErrors().contains("Error getting separators"));
    //    }
    //
    //    @Test
    //    public void testMissingFirstLine() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_no_isa_line.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //        assertEquals(1, reader.getErrors().size());
    //        assertTrue(reader.getErrors().contains("Error getting separators"));
    //    }
    //
    //    @Test
    //    public void testConsistentVersions() throws Exception {
    //        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X223, new File(url.getFile()));
    //        assertEquals(1, reader.getErrors().size());
    //        assertTrue(reader.getErrors().get(0).contains("not consistent with version specified"));
    //
    //        reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    //        assertTrue(reader.getErrors().isEmpty());
    //    }

    //    @Test
    //    public void test() throws Exception {
    //        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File("C:\\Users\\angelasd\\Desktop\\testing-files\\x12_valid_one_huge_claim.txt"));
    //        //X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File("C:\\Users\\angelasd\\Desktop\\testing-files\\x12_valid_many_average_claims.txt"));
    //
    //        System.out.println(reader.getLoops().get(0).getLoop("DETAIL").getLoops().size());
    //    }

    @Test
    public void test()  throws  Exception{
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
    }
}
