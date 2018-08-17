package com.imsweb.x12.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.x12.Loop;
import com.imsweb.x12.mapping.TransactionDefinition;
import com.imsweb.x12.reader.X12Reader.FileType;

public class X12ReaderTest {

    @Test
    public void testDifferentSeparators() throws IOException {
        URL url = this.getClass().getResource("/837_5010/x12_valid_different_separators.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
        validate837Valid(reader.getLoop());
    }

    @Test
    public void testConstructors() throws IOException {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");

        X12Reader fromFile = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
        X12Reader fromFileUtf8 = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()), StandardCharsets.UTF_8);
        X12Reader fromInputStream = new X12Reader(FileType.ANSI837_5010_X222, new FileInputStream(url.getFile()));
        X12Reader fromInputStreamUtf8 = new X12Reader(FileType.ANSI837_5010_X222, new FileInputStream(url.getFile()), StandardCharsets.UTF_8);

        Assert.assertEquals(fromFile.getLoop().toString(), fromInputStream.getLoop().toString());
        Assert.assertEquals(fromFileUtf8.getLoop().toString(), fromInputStreamUtf8.getLoop().toString());
    }

    @Test
    public void testMultipleGSLoops() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_multiple_gs.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        validateMultipleGSLoops(reader.getLoop());
    }

    @Test
    public void testMultipleSTLoops() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_multiple_st.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        validateMultipleSTLoops(reader.getLoop());
    }

    @Test
    public void testMarkingFiles() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(url.getFile()), StandardCharsets.UTF_8))) {
            String line = null;

            reader.mark(1);
            for (int i = 0; i < 10; i++)
                line = reader.readLine();

            Assert.assertEquals("N3*3901 CALVERTON BLVD~", line);

            reader.reset();
            line = reader.readLine();
            Assert.assertEquals("ISA*00*          *01*SECRET    *ZZ*SUBMITTERS.ID  *ZZ*RECEIVERS.ID   *030101*1253*U*00501*000000905*1*T*:~", line);
        }
    }

    @Test
    public void testNewGetMethods() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
        Loop loop = reader.getLoop();

        Loop test = reader.getLoop().getLoop("2000B");

        //valid loop, no indices
        Assert.assertEquals("123456", loop.getElement("2010BA", "NM1", "NM109"));
        //valid loop, indicies
        Assert.assertEquals("3", loop.getElement("2000B", 1, "HL", 0, "HL02"));
        Assert.assertEquals("TE", loop.getElement("2010AA", 0, "PER", 1, "PER03"));
        //valid segement, indices
        Assert.assertEquals("1", test.getElement("HL", 0, "HL02"));
        //valid segment, no indices
        Assert.assertEquals("1", test.getElement("HL", "HL02"));
        //segment does not exist
        Assert.assertNull(test.getElement("HL", 1, "HL02"));
        //loop does not exist
        Assert.assertNull(loop.getElement("2000B", 2, "HL", 0, "HL02"));
        //loop exists
        Assert.assertNull(loop.getElement("2000B", 1, "MEA", 0, "MEA02"));
        //element does not exist
        Assert.assertNull(loop.getElement("2000B", 1, "HL", 0, "HL17"));
    }

    @Test
    public void testWithFileConstructor() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        validate837Valid(reader.getLoop());
    }

    @Test
    public void testWithReadableConstructor() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        validate837Valid(reader.getLoop());
    }

    @Test
    public void testWithInputStreamConstructor() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new FileInputStream(new File(url.getFile())));

        validate837Valid(reader.getLoop());
    }

    @Test
    public void testBadValidCode() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_bad_valid_code.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();

        Assert.assertTrue(errors.size() == 7);
        Assert.assertTrue(errors.contains("Unable to find a matching segment format in loop 2000A"));
        // Assert.assertTrue(errors.contains("2010AA is required but missing"));
    }

    @Test
    public void testSegmentErrors() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_segment_errors.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();

        Assert.assertTrue(errors.size() == 3);
        Assert.assertTrue(errors.contains("N3 in loop 2010AA is required but not found"));
        Assert.assertTrue(errors.contains("HI in loop 2300 is required but not found"));
        Assert.assertTrue(errors.contains("REF in loop 2010AA appears too many times"));
    }

    //tests a file that has segments with missing data that is required.
    @Test
    public void testBadSegementIdentifier() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_bad_segment_identifier.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();

        Assert.assertTrue(errors.size() == 6);
        Assert.assertTrue(errors.contains("Unable to find a matching segment format in loop 2010AA"));
        Assert.assertTrue(errors.contains("Unable to find a matching segment format in loop 2000B"));
        Assert.assertTrue(errors.contains("Unable to find a matching segment format in loop 1000A"));
        Assert.assertTrue(errors.contains("N4 in loop 2010AA is required but not found"));
        Assert.assertTrue(errors.contains("PER in loop 1000A is required but not found"));
        Assert.assertTrue(errors.contains("2010BA is required but not found in 2000B iteration #1"));
    }

    //tests a file that has segments with missing data that is required.
    @Test
    public void testMissingRequiredElements() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_missing_elements.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();
        Assert.assertTrue(errors.size() == 2);
        Assert.assertTrue(errors.contains("NM1 in loop 1000B is missing a required element at 2"));
        Assert.assertTrue(errors.contains("CLM in loop 2300 is missing a required composite element at 5"));
    }

    //tests a loop that appears too many times with the parent loop appearing once
    @Test
    public void testExceedsRepeatsOneParents() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_loop_errors1_exceeds_max_1_parent.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();

        Assert.assertTrue(errors.size() == 1);
        Assert.assertTrue(errors.contains("2010AA appears too many times"));
    }

    //tests a loop that appears too many times with a parent loop that appears more than once
    @Test
    public void testExceedRepeatsMultipleParents() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_loop_errors2_exceeds_max_repeats_mulitple_parents.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();

        Assert.assertTrue(errors.size() == 1);
        Assert.assertTrue(errors.contains("2010AA appears too many times"));
    }

    //tests a missing required loop
    @Test
    public void testMissingLoop() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_loop_errors3_missing_loops.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();
        Assert.assertTrue(errors.size() == 1);

        Assert.assertTrue(errors.contains("2010AA is required but not found in 2000A iteration #1"));
    }

    //tests a loop that appears the correct number of times but in a parent that appears more than once.
    @Test
    public void testValidLoopStructure() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_no_errors.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();

        Assert.assertTrue(errors.size() == 0);
    }

    //tests a loop that appears the correct number of times but in a parent that appears more than once.
    @Test
    public void testMissingLoopInOneRepeat() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_missing_required_loop_in_one_repeat.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();

        Assert.assertTrue(errors.size() == 1);

        Assert.assertTrue(errors.contains("2010BA is required but not found in 2000B iteration #1"));
    }

    @Test
    public void testDefinitions() {
        for (FileType type : FileType.values()) {
            TransactionDefinition definition = type.getDefinition();

            Assert.assertNotNull(definition);
            Assert.assertNotNull(definition.getLoop());

            // call a second time to make sure cache is working
            definition = type.getDefinition();

            Assert.assertNotNull(definition);
            Assert.assertNotNull(definition.getLoop());
        }
    }

    @Test
    public void testToXml() throws IOException {
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(this.getClass().getResource("/837_5010/x12_no_errors.txt").getFile()));

        String xml = reader.getLoop().toXML();
        Assert.assertTrue(xml.length() > 0);
        Assert.assertTrue(xml.startsWith("<loop id=\"ISA_LOOP\">"));
    }

    @Test
    public void testToJson() throws IOException {
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(this.getClass().getResource("/837_5010/x12_no_errors.txt").getFile()));

        String json = reader.getLoop().toJson();
        Assert.assertTrue(json.length() > 0);
        Assert.assertTrue(json.startsWith("{\n  \"id\": \"ISA_LOOP\",\n  \"segments\""));

        // TODO test JSON equality
        //        reader = new X12Reader(FileType.ANSI837_5010_X222, new File(this.getClass().getResource("/837_5010/x12_multiple_gs.txt").getFile()));
        //
        //        json = reader.getLoop().toJson();
        //
        //        System.out.println(json);
    }

    @Test
    public void testMissingRequiredLoopInMultipleRepeats() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_missing_required_loops_mult_repeats.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();
        Assert.assertTrue(errors.size() == 4);

        Assert.assertTrue(errors.contains("Unable to find a matching segment format in loop 2300"));
        Assert.assertTrue(errors.contains("2400 is required but not found in 2300 iteration #2"));
        Assert.assertTrue(errors.contains("2400 is required but not found in 2300 iteration #5"));
    }

    /**
     * Valid output for a testing file
     * @param loop
     */
    private void validate837Valid(Loop loop) {
        Assert.assertEquals(1, loop.getLoops().size());
        Assert.assertEquals(1, loop.getLoop("GS_LOOP").getLoops().size());
        Assert.assertEquals(2, loop.getLoop("ST_LOOP").getLoops().size());
        Assert.assertEquals(2, loop.getLoop("HEADER").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("1000A").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("1000B").getLoops().size());
        Assert.assertEquals(1, loop.getLoop("DETAIL").getLoops().size());
        Assert.assertEquals(4, loop.getLoop("2000A").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010AA").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010AB").getLoops().size());
        Assert.assertEquals(2, loop.findLoop("2000B").size());
        Assert.assertEquals(3, loop.getLoop("2000B", 0).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010BA", 0).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010BB", 0).getLoops().size());
        Assert.assertEquals(1, loop.getLoop("2300", 0).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2400", 0).getLoops().size());
        Assert.assertEquals(3, loop.getLoop("2000B", 1).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010BA", 1).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010BB", 1).getLoops().size());
        Assert.assertEquals(1, loop.getLoop("2300", 1).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2400", 1).getLoops().size());

        //Testing null results
        Assert.assertNull(loop.getLoop("2400", 2));
        Assert.assertNull(loop.getLoop("2310"));
        Assert.assertNull(loop.getLoop("2400").getSegment("MEA"));
        Assert.assertNull(loop.getLoop("2000B").getSegment("HL", 2));
        try {
            Assert.assertNull(loop.getLoop("2400", 2).getSegment("MEA"));
            Assert.fail("Expecting a null pointer exception to be thrown (test #1)");
        }
        catch (NullPointerException e) {
            //Expected
        }
        try {
            Assert.assertNull(loop.getLoop("2400").getSegment("MEA").getElementValue("MEA02"));
            Assert.fail("Expecting a null pointer exception to be thrown (test #2)");
        }
        catch (NullPointerException e) {
            //Expected
        }

        // testing loopID match for each subloop
        Assert.assertEquals("GS_LOOP", loop.getLoops().get(0).getId());
        Assert.assertEquals("ST_LOOP", loop.getLoop("GS_LOOP").getLoops().get(0).getId());
        Assert.assertEquals("HEADER", loop.getLoop("ST_LOOP").getLoops().get(0).getId());
        Assert.assertEquals("DETAIL", loop.getLoop("ST_LOOP").getLoops().get(1).getId());
        Assert.assertEquals("1000A", loop.getLoop("HEADER").getLoops().get(0).getId());
        Assert.assertEquals("1000B", loop.getLoop("HEADER").getLoops().get(1).getId());
        Assert.assertEquals("2000A", loop.getLoop("DETAIL").getLoops().get(0).getId());
        Assert.assertEquals("2010AA", loop.getLoop("2000A").getLoops().get(0).getId());
        Assert.assertEquals("2010AB", loop.getLoop("2000A").getLoops().get(1).getId());
        Assert.assertEquals("2000B", loop.getLoop("2000A").getLoops().get(2).getId());
        Assert.assertEquals("2000B", loop.getLoop("2000A").getLoops().get(3).getId());
        Assert.assertEquals("2010BA", loop.getLoop("2000B", 0).getLoops().get(0).getId());
        Assert.assertEquals("2010BB", loop.getLoop("2000B", 0).getLoops().get(1).getId());
        Assert.assertEquals("2300", loop.getLoop("2000B", 0).getLoops().get(2).getId());
        Assert.assertEquals("2400", loop.getLoop("2300", 0).getLoops().get(0).getId());
        Assert.assertEquals("2010BA", loop.getLoop("2000B", 1).getLoops().get(0).getId());
        Assert.assertEquals("2010BB", loop.getLoop("2000B", 1).getLoops().get(1).getId());
        Assert.assertEquals("2300", loop.getLoop("2000B", 1).getLoops().get(2).getId());
        Assert.assertEquals("2400", loop.getLoop("2300", 1).getLoops().get(0).getId());

        // testing grabbing data from each line
        Assert.assertEquals("030101", loop.getSegment("ISA").getElementValue("ISA09"));//First loop
        Assert.assertEquals("19991231", loop.getLoop("GS_LOOP").getSegment("GS").getElementValue("GS04"));
        Assert.assertEquals("987654", loop.getLoop("ST_LOOP").getSegment("ST").getElementValue("ST02"));
        Assert.assertEquals("0932", loop.getLoop("HEADER").getSegment("BHT").getElementValue("BHT05"));
        Assert.assertEquals("NM1", loop.getLoop("1000A").getSegment("NM1").getId());
        Assert.assertEquals("3016809770", loop.getLoop("1000A").getSegment("PER").getElementValue("PER04"));
        Assert.assertEquals("2", loop.getLoop("1000B").getSegment("NM1").getElementValue("NM102"));
        Assert.assertEquals("", loop.getLoop("2000A").getSegment("HL").getElementValue("HL02"));
        Assert.assertEquals("85", loop.getLoop("2010AA").getSegment("NM1").getElementValue("NM101"));
        Assert.assertEquals("N3", loop.getLoop("2010AA").getSegment("N3").getId());
        Assert.assertEquals("20705", loop.getLoop("2010AA").getSegment("N4").getElementValue("N403"));
        Assert.assertEquals("EI", loop.getLoop("2010AA").getSegment("REF").getElementValue("REF01"));
        Assert.assertEquals("JANE JONES", loop.getLoop("2010AA").getSegment("PER").getElementValue("PER02"));
        Assert.assertEquals("201", loop.getLoop("2010AA").getSegment("PER", 1).getElementValue("PER06"));
        Assert.assertEquals("87", loop.getLoop("2010AB").getSegment("NM1").getElementValue("NM101"));
        Assert.assertEquals("227 LASTNER LANE", loop.getLoop("2010AB").getSegment("N3").getElementValue("N301"));
        Assert.assertEquals("GREENBELT", loop.getLoop("2010AB").getSegment("N4").getElementValue("N401"));
        Assert.assertEquals("22", loop.getLoop("2000B").getSegment("HL").getElementValue("HL03"));
        Assert.assertEquals("", loop.getLoop("2000B").getSegment("SBR").getElementValue("SBR02"));
        Assert.assertEquals("123456", loop.getLoop("2010BA").getSegment("NM1").getElementValue("NM109"));
        Assert.assertEquals("PI", loop.getLoop("2010BB").getSegment("NM1").getElementValue("NM108"));
        Assert.assertEquals("A37YH556", loop.getLoop("2300").getSegment("CLM").getElementValue("CLM01"));
        Assert.assertEquals("BK", loop.getLoop("2300").getSegment("HI").getElementValue("HI01"));
        Assert.assertEquals("1", loop.getLoop("2400").getSegment("LX").getElementValue("LX01"));
        Character compositeSeparator = loop.getLoop("2400").getSegment("SV1").getSeparators().getCompositeElement();
        Assert.assertEquals("HC" + compositeSeparator + "99211" + compositeSeparator + "25", loop.getLoop("2400").getSegment("SV1").getElementValue("SV101"));
        Assert.assertEquals("RD8", loop.getLoop("2400").getSegment("DTP").getElementValue("DTP02"));

        // Tests of differences between repeating loops
        Assert.assertEquals("1", loop.getLoop("2000B", 0).getSegment("HL").getElementValue("HL02"));
        Assert.assertEquals("3", loop.getLoop("2000B", 1).getSegment("HL").getElementValue("HL02"));

        Assert.assertEquals("SUBSCRIBER GROUP", loop.getLoop("2000B", 0).getSegment("SBR").getElementValue("SBR03"));
        Assert.assertEquals("SUBSCRIBER GROUP TWO", loop.getLoop("2000B", 1).getSegment("SBR").getElementValue("SBR03"));

        Assert.assertEquals("JOHN", loop.getLoop("2010BA", 0).getSegment("NM1").getElementValue("NM104"));
        Assert.assertEquals("DAVID", loop.getLoop("2010BA", 1).getSegment("NM1").getElementValue("NM104"));

        Assert.assertEquals("HEALTH INSURANCE COMPANY", loop.getLoop("2010BB", 0).getSegment("NM1").getElementValue("NM103"));
        Assert.assertEquals("HEALTH INSURANCE COMPANY TWO", loop.getLoop("2010BB", 1).getSegment("NM1").getElementValue("NM103"));

        Assert.assertEquals("A37YH556", loop.getLoop("2300", 0).getSegment("CLM").getElementValue("CLM01"));
        Assert.assertEquals("A37YH667", loop.getLoop("2300", 1).getSegment("CLM").getElementValue("CLM01"));
        Assert.assertEquals(1, loop.getLoop("2300", 1).getSegment("CLM").getElement("CLM01").getNumOfSubElements());

        Assert.assertEquals("8901", loop.getLoop("2300", 0).getSegment("HI").getElementValue("HI02"));
        Assert.assertEquals("1987", loop.getLoop("2300", 1).getSegment("HI").getElementValue("HI02"));
        Assert.assertEquals(1, loop.getLoop("2300", 1).getSegment("HI").getElement("HI02").getNumOfSubElements());

        Assert.assertEquals("1", loop.getLoop("2400", 0).getSegment("LX").getElementValue("LX01"));
        Assert.assertEquals("2", loop.getLoop("2400", 1).getSegment("LX").getElementValue("LX01"));

        compositeSeparator = loop.getLoop("2400", 1).getSegment("SV1").getSeparators().getCompositeElement();
        Assert.assertEquals("HC" + compositeSeparator + "99211" + compositeSeparator + "25", loop.getLoop("2400", 0).getSegment("SV1").getElementValue("SV101"));
        Assert.assertEquals("HC" + compositeSeparator + "478331" + compositeSeparator + "25", loop.getLoop("2400", 1).getSegment("SV1").getElementValue("SV101"));
        Assert.assertEquals(3, loop.getLoop("2400", 1).getSegment("SV1").getElement("SV101").getNumOfSubElements());
        Assert.assertEquals("478331", loop.getLoop("2400", 1).getSegment("SV1").getElement("SV101", 1));
        Assert.assertNull(loop.getLoop("2400", 1).getSegment("SV1").getElement("SV101", 3));
        Assert.assertEquals(3, loop.getLoop("2400", 1).getSegment("SV1").getElement("SV107").getNumOfSubElements());
        Assert.assertEquals(1, loop.getLoop("2400", 0).getSegment("SV1").getElement("SV107").getNumOfSubElements());

        Assert.assertEquals("20050314-20050325", loop.getLoop("2400", 0).getSegment("DTP").getElementValue("DTP03"));
        Assert.assertEquals("20050322-20050325", loop.getLoop("2400", 1).getSegment("DTP").getElementValue("DTP03"));
        Assert.assertEquals(1, loop.getLoop("2400", 1).getSegment("DTP").getElement("DTP03").getNumOfSubElements());
    }

    private void validateMultipleGSLoops(Loop loop) {
        Assert.assertEquals(3, loop.getLoops().size());
        Assert.assertEquals(1, loop.getLoop("GS_LOOP", 0).getLoops().size());
        Assert.assertEquals(2, loop.getLoop("ST_LOOP").getLoops().size());
        Assert.assertEquals(2, loop.getLoop("HEADER").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("1000A").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("1000B").getLoops().size());
        Assert.assertEquals(1, loop.getLoop("DETAIL").getLoops().size());
        Assert.assertEquals(4, loop.getLoop("2000A").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010AA").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010AB").getLoops().size());
        Assert.assertEquals(6, loop.findLoop("2000B").size());
        Assert.assertEquals(3, loop.getLoop("2000B", 0).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010BA", 0).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010BB", 0).getLoops().size());
        Assert.assertEquals(1, loop.getLoop("2300", 0).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2400", 0).getLoops().size());
        Assert.assertEquals(3, loop.getLoop("2000B", 1).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010BA", 1).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010BB", 1).getLoops().size());
        Assert.assertEquals(1, loop.getLoop("2300", 1).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2400", 1).getLoops().size());

        Assert.assertEquals(1, loop.getLoop("GS_LOOP", 1).getLoops().size());
        Assert.assertEquals(2, loop.getLoop(1).getLoop("ST_LOOP").getLoops().size());
        Assert.assertEquals(2, loop.getLoop(1).getLoop("HEADER").getLoops().size());
        Assert.assertEquals(0, loop.getLoop(1).getLoop("1000A").getLoops().size());
        Assert.assertEquals(0, loop.getLoop(1).getLoop("1000B").getLoops().size());
        Assert.assertEquals(1, loop.getLoop(1).getLoop("DETAIL").getLoops().size());
        Assert.assertEquals(4, loop.getLoop(1).getLoop("2000A").getLoops().size());
        Assert.assertEquals(0, loop.getLoop(1).getLoop("2010AA").getLoops().size());
        Assert.assertEquals(0, loop.getLoop(1).getLoop("2010AB").getLoops().size());
        Assert.assertEquals(3, loop.getLoop(1).getLoop("2000B", 0).getLoops().size());
        Assert.assertEquals(0, loop.getLoop(1).getLoop("2010BA", 0).getLoops().size());
        Assert.assertEquals(0, loop.getLoop(1).getLoop("2010BB", 0).getLoops().size());
        Assert.assertEquals(1, loop.getLoop(1).getLoop("2300", 0).getLoops().size());
        Assert.assertEquals(0, loop.getLoop(1).getLoop("2400", 0).getLoops().size());
        Assert.assertEquals(3, loop.getLoop(1).getLoop("2000B", 1).getLoops().size());
        Assert.assertEquals(0, loop.getLoop(1).getLoop("2010BA", 1).getLoops().size());
        Assert.assertEquals(0, loop.getLoop(1).getLoop("2010BB", 1).getLoops().size());
        Assert.assertEquals(1, loop.getLoop(1).getLoop("2300", 1).getLoops().size());
        Assert.assertEquals(0, loop.getLoop(1).getLoop("2400", 1).getLoops().size());

        Assert.assertEquals("20050314-20050325", loop.getLoop(1).getLoop("2400", 0).getSegment("DTP").getElementValue("DTP03"));
        Assert.assertEquals("20010523-20010601", loop.getLoop(1).getLoop("2400", 1).getSegment("DTP").getElementValue("DTP03"));

        Assert.assertEquals("GREENBELT", loop.getLoop(0).getLoop("2010AB").getSegment("N4").getElementValue("N401"));
        Assert.assertEquals("COLLEGE PARK", loop.getLoop(1).getLoop("2010AB").getSegment("N4").getElementValue("N401"));

        Assert.assertEquals("DAVID ANGELASZEK", loop.getLoop(0).getLoop("1000A").getSegment("PER").getElementValue("PER02"));
        Assert.assertEquals("DAVID JEFFREY ANGELASZEK", loop.getLoop(2).getLoop("1000A").getSegment("PER").getElementValue("PER02"));
    }

    private void validateMultipleSTLoops(Loop loop) {
        Assert.assertEquals(1, loop.getLoops().size());
        Assert.assertEquals(2, loop.getLoop("GS_LOOP").getLoops().size());
        Assert.assertEquals(2, loop.getLoop("ST_LOOP").getLoops().size());
        Assert.assertEquals(2, loop.getLoop("HEADER").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("1000A").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("1000B").getLoops().size());
        Assert.assertEquals(1, loop.getLoop("DETAIL").getLoops().size());
        Assert.assertEquals(4, loop.getLoop("2000A").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010AA").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010AB").getLoops().size());
        Assert.assertEquals(4, loop.findLoop("2000B").size());
        Assert.assertEquals(3, loop.getLoop("2000B", 0).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010BA", 0).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010BB", 0).getLoops().size());
        Assert.assertEquals(1, loop.getLoop("2300", 0).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2400", 0).getLoops().size());
        Assert.assertEquals(3, loop.getLoop("2000B", 1).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010BA", 1).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2010BB", 1).getLoops().size());
        Assert.assertEquals(1, loop.getLoop("2300", 1).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("2400", 1).getLoops().size());

        Assert.assertEquals(2, loop.getLoop("ST_LOOP", 1).getLoops().size());
        Assert.assertEquals(2, loop.getLoop("ST_LOOP", 1).getLoop("HEADER").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("1000A").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("1000B").getLoops().size());
        Assert.assertEquals(1, loop.getLoop("ST_LOOP", 1).getLoop("DETAIL").getLoops().size());
        Assert.assertEquals(4, loop.getLoop("ST_LOOP", 1).getLoop("2000A").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010AA").getLoops().size());
        Assert.assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010AB").getLoops().size());
        Assert.assertEquals(3, loop.getLoop("ST_LOOP", 1).getLoop("2000B", 0).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010BA", 0).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010BB", 0).getLoops().size());
        Assert.assertEquals(1, loop.getLoop("ST_LOOP", 1).getLoop("2300", 0).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2400", 0).getLoops().size());
        Assert.assertEquals(3, loop.getLoop("ST_LOOP", 1).getLoop("2000B", 1).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010BA", 1).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010BB", 1).getLoops().size());
        Assert.assertEquals(1, loop.getLoop("ST_LOOP", 1).getLoop("2300", 1).getLoops().size());
        Assert.assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2400", 1).getLoops().size());

        Assert.assertEquals("227 LASTNER LANE", loop.getLoop("ST_LOOP", 0).getLoop("2010AB").getSegment("N3").getElementValue("N301"));
        Assert.assertEquals("21 CRAMER PATH", loop.getLoop("ST_LOOP", 1).getLoop("2010AB").getSegment("N3").getElementValue("N301"));

        Assert.assertEquals("JANE JANES", loop.getLoop("ST_LOOP", 0).getLoop("2010AA").getSegment("PER", 1).getElementValue("PER02"));
        Assert.assertEquals("JANICE JONES", loop.getLoop("ST_LOOP", 1).getLoop("2010AA").getSegment("PER", 1).getElementValue("PER02"));

        Assert.assertEquals("HEALTH INSURANCE COMPANY TWO", loop.getLoop("ST_LOOP", 0).getLoop("2010BB", 1).getSegment("NM1").getElementValue("NM103"));
        Assert.assertEquals("AN INSURANCE COMPANY", loop.getLoop("ST_LOOP", 1).getLoop("2010BB", 1).getSegment("NM1").getElementValue("NM103"));
    }

    @Test
    public void testSegmentsNotInOrder() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_segments_out_of_order.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();

        System.out.println(errors);
        Assert.assertEquals(2, errors.size());

        Assert.assertTrue(errors.contains("Segment N4 in loop 2010AA is not in the correct position."));
        Assert.assertTrue(errors.contains("Segment N3 in loop 2010AB is not in the correct position."));
    }

    @Test
    public void testBadFirstLine() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_bad_first_line.txt");
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
        Assert.assertEquals(1, reader.getErrors().size());
        Assert.assertTrue(reader.getErrors().contains("Error getting separators"));
    }
}
