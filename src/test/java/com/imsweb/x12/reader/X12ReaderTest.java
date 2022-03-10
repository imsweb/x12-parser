package com.imsweb.x12.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.imsweb.x12.Element;
import com.imsweb.x12.Loop;
import com.imsweb.x12.mapping.TransactionDefinition;
import com.imsweb.x12.reader.X12Reader.FileType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"java:S5961", "java:S5976"})
class X12ReaderTest {

    @Test
    void testConstructors() throws IOException {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
        assertNotNull(url);

        X12Reader fromFile = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
        X12Reader fromFileUtf8 = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()), StandardCharsets.UTF_8);
        X12Reader fromInputStream = new X12Reader(FileType.ANSI837_5010_X222, new FileInputStream(url.getFile()));
        X12Reader fromInputStreamUtf8 = new X12Reader(FileType.ANSI837_5010_X222, new FileInputStream(url.getFile()), StandardCharsets.UTF_8);
        X12Reader fromReaderUtf8 = new X12Reader(FileType.ANSI837_5010_X222, new BufferedReader(new InputStreamReader(new FileInputStream(url.getFile()), StandardCharsets.UTF_8)));

        assertEquals(fromFile.getLoops().get(0).toString(), fromInputStream.getLoops().get(0).toString());
        assertEquals(fromFileUtf8.getLoops().get(0).toString(), fromInputStreamUtf8.getLoops().get(0).toString());
        assertEquals(fromFileUtf8.getLoops().get(0).toString(), fromReaderUtf8.getLoops().get(0).toString());
    }

    @Test
    void testMultipleGSLoops() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_multiple_gs.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
        validateMultipleGSLoops(reader.getLoops().get(0));
    }

    @Test
    void testMultipleISALoops() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_multiple_isa.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
        validateMultipleISALoops(reader.getLoops());
    }

    @Test
    void testMultipleSTLoops() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_multiple_st.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<Loop> loops = reader.getLoops();
        assertEquals(1, loops.size());
        validateMultipleSTLoops(loops.get(0));
    }

    @Test
    void testMarkingFiles() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
        assertNotNull(url);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(url.getFile()), StandardCharsets.UTF_8))) {
            String line = null;

            reader.mark(1);
            for (int i = 0; i < 10; i++)
                line = reader.readLine();

            assertEquals("N3*3901 CALVERTON BLVD~", line);

            reader.reset();
            line = reader.readLine();
            assertEquals("ISA*00*          *01*SECRET    *ZZ*SUBMITTERS.ID  *ZZ*RECEIVERS.ID   *030101*1253*U*00501*000000905*1*T*:~", line);
        }
    }

    @Test
    void testNewGetMethods() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
        Loop loop = reader.getLoops().get(0);

        Loop test = loop.getLoop("2000B");

        //valid loop, no indices
        assertEquals("123456", loop.getElement("2010BA", "NM1", "NM109"));
        //valid loop, indicies
        assertEquals("3", loop.getElement("2000B", 1, "HL", 0, "HL02"));
        assertEquals("TE", loop.getElement("2010AA", 0, "PER", 1, "PER03"));
        //valid segement, indices
        assertEquals("1", test.getElement("HL", 0, "HL02"));
        //valid segment, no indices
        assertEquals("1", test.getElement("HL", "HL02"));
        //segment does not exist
        assertNull(test.getElement("HL", 1, "HL02"));
        //loop does not exist
        assertNull(loop.getElement("2000B", 2, "HL", 0, "HL02"));
        //loop exists
        assertNull(loop.getElement("2000B", 1, "MEA", 0, "MEA02"));
        //element does not exist
        assertNull(loop.getElement("2000B", 1, "HL", 0, "HL17"));
    }

    @Test
    void testDifferentSeparators() throws IOException {
        URL url = this.getClass().getResource("/837_5010/x12_valid_different_separators.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
        validate837Valid(reader.getLoops().get(0));
    }

    @Test
    void testWithFileConstructor() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        validate837Valid(reader.getLoops().get(0));
    }

    @Test
    void testWithReadableConstructor() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        validate837Valid(reader.getLoops().get(0));
    }

    @Test
    void testWithInputStreamConstructor() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new FileInputStream(url.getFile()));

        validate837Valid(reader.getLoops().get(0));
    }

    @Test
    void testBadValidCode() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_bad_valid_code.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();

        assertEquals(3, errors.size());
        assertTrue(errors.contains("Unable to find a matching segment format in loop 2000A"));

        assertFalse(reader.getFatalErrors().isEmpty());
    }

    @Test
    void testSegmentErrors() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_segment_errors.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();

        assertEquals(3, errors.size());
        assertTrue(errors.contains("N3 in loop 2010AA is required but not found"));
        assertTrue(errors.contains("HI in loop 2300 is required but not found"));
        assertTrue(errors.contains("REF in loop 2010AA appears too many times"));
        assertTrue(reader.getFatalErrors().isEmpty());
    }

    //tests a file that has segments with missing data that is required.
    @Test
    void testBadSegementIdentifier() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_bad_segment_identifier.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();

        assertEquals(6, errors.size());
        assertTrue(errors.contains("Unable to find a matching segment format in loop 2010AA"));
        assertTrue(errors.contains("Unable to find a matching segment format in loop 2000B"));
        assertTrue(errors.contains("Unable to find a matching segment format in loop 1000A"));
        assertTrue(errors.contains("N4 in loop 2010AA is required but not found"));
        assertTrue(errors.contains("PER in loop 1000A is required but not found"));
        assertTrue(errors.contains("2010BA is required but not found in 2000B iteration #1"));
        assertTrue(reader.getFatalErrors().isEmpty());
    }

    //tests a file that has segments with missing data that is required.
    @Test
    void testMissingRequiredElements() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_missing_elements.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();
        assertEquals(2, errors.size());
        assertTrue(errors.contains("NM1 in loop 1000B is missing a required element at 2"));
        assertTrue(errors.contains("CLM in loop 2300 is missing a required composite element at 5"));
        assertTrue(reader.getFatalErrors().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/837_5010/x12_loop_errors1_exceeds_max_1_parent.txt",
            "/837_5010/x12_loop_errors2_exceeds_max_repeats_multiple_parents.txt",
    })
    void testRepeats(String file) throws Exception {
        URL url = this.getClass().getResource(file);
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();

        assertEquals(1, errors.size());
        assertTrue(errors.contains("2010AA appears too many times"));
        assertTrue(reader.getFatalErrors().isEmpty());
    }

    //tests a missing required loop
    @Test
    void testMissingLoop() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_loop_errors3_missing_loops.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();
        assertEquals(1, errors.size());

        assertTrue(errors.contains("2010AA is required but not found in 2000A iteration #1"));
        assertTrue(reader.getFatalErrors().isEmpty());
    }

    //tests a loop that appears the correct number of times but in a parent that appears more than once.
    @Test
    void testValidLoopStructure() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_no_errors.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();

        assertEquals(0, errors.size());
        assertTrue(reader.getFatalErrors().isEmpty());
    }

    //tests a loop that appears the correct number of times but in a parent that appears more than once.
    @Test
    void testMissingLoopInOneRepeat() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_missing_required_loop_in_one_repeat.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();
        assertEquals(1, errors.size());

        assertTrue(errors.contains("2010BA is required but not found in 2000B iteration #1"));
        assertTrue(reader.getFatalErrors().isEmpty());
    }

    @Test
    void testDefinitions() {
        for (FileType type : FileType.values()) {
            TransactionDefinition definition = type.getDefinition();

            assertNotNull(definition);
            assertNotNull(definition.getLoop());

            // call a second time to make sure cache is working
            definition = type.getDefinition();

            assertNotNull(definition);
            assertNotNull(definition.getLoop());
        }
    }

    @Test
    void testToXml() throws IOException {
        URL url = this.getClass().getResource("/837_5010/x12_no_errors.txt");
        assertNotNull(url);

        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        String xml = reader.getLoops().get(0).toXML();
        assertTrue(xml.length() > 0);
        assertTrue(xml.startsWith("<loop id=\"ISA_LOOP\">"));
    }

    @Test
    void testToJson() throws IOException {
        URL url = this.getClass().getResource("/837_5010/x12_no_errors.txt");
        assertNotNull(url);

        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        String json = reader.getLoops().get(0).toJson();
        assertTrue(json.length() > 0);
        assertTrue(json.startsWith("{\n  \"id\": \"ISA_LOOP\",\n  \"segments\""));
    }

    @Test
    void testMissingRequiredLoopInMultipleRepeats() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_missing_required_loops_mult_repeats.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();
        assertEquals(6, errors.size());

        assertTrue(errors.contains("Unable to find a matching segment format in loop 2300"));
        assertTrue(errors.contains("2400 is required but not found in 2300 iteration #2"));
        assertTrue(errors.contains("2400 is required but not found in 2300 iteration #5"));
        assertEquals(4, errors.stream().filter(e -> e.equals("Unable to find a matching segment format in loop 2300")).count());
        assertTrue(reader.getFatalErrors().isEmpty());
    }

    /**
     * Valid output for a testing file
     */
    @SuppressWarnings("java:S5778")
    private void validate837Valid(Loop loop) {
        assertEquals(1, loop.getLoops().size());
        assertEquals(1, loop.getLoop("GS_LOOP").getLoops().size());
        assertEquals(2, loop.getLoop("ST_LOOP").getLoops().size());
        assertEquals(2, loop.getLoop("HEADER").getLoops().size());
        assertEquals(0, loop.getLoop("1000A").getLoops().size());
        assertEquals(0, loop.getLoop("1000B").getLoops().size());
        assertEquals(1, loop.getLoop("DETAIL").getLoops().size());
        assertEquals(4, loop.getLoop("2000A").getLoops().size());
        assertEquals(0, loop.getLoop("2010AA").getLoops().size());
        assertEquals(0, loop.getLoop("2010AB").getLoops().size());
        assertEquals(2, loop.findLoop("2000B").size());
        assertEquals(3, loop.getLoop("2000B", 0).getLoops().size());
        assertEquals(0, loop.getLoop("2010BA", 0).getLoops().size());
        assertEquals(0, loop.getLoop("2010BB", 0).getLoops().size());
        assertEquals(1, loop.getLoop("2300", 0).getLoops().size());
        assertEquals(0, loop.getLoop("2400", 0).getLoops().size());
        assertEquals(3, loop.getLoop("2000B", 1).getLoops().size());
        assertEquals(0, loop.getLoop("2010BA", 1).getLoops().size());
        assertEquals(0, loop.getLoop("2010BB", 1).getLoops().size());
        assertEquals(1, loop.getLoop("2300", 1).getLoops().size());
        assertEquals(0, loop.getLoop("2400", 1).getLoops().size());

        //Testing null results
        assertNull(loop.getLoop("2400", 2));
        assertNull(loop.getLoop("2310"));
        assertNull(loop.getLoop("2400").getSegment("MEA"));
        assertNull(loop.getLoop("2000B").getSegment("HL", 2));

        assertThrows(NullPointerException.class, () -> loop.getLoop("2400", 2).getSegment("MEA"));
        assertThrows(NullPointerException.class, () -> loop.getLoop("2400").getSegment("MEA").getElementValue("MEA02"));

        // testing loopID match for each subloop
        assertEquals("GS_LOOP", loop.getLoops().get(0).getId());
        assertEquals("ST_LOOP", loop.getLoop("GS_LOOP").getLoops().get(0).getId());
        assertEquals("HEADER", loop.getLoop("ST_LOOP").getLoops().get(0).getId());
        assertEquals("DETAIL", loop.getLoop("ST_LOOP").getLoops().get(1).getId());
        assertEquals("1000A", loop.getLoop("HEADER").getLoops().get(0).getId());
        assertEquals("1000B", loop.getLoop("HEADER").getLoops().get(1).getId());
        assertEquals("2000A", loop.getLoop("DETAIL").getLoops().get(0).getId());
        assertEquals("2010AA", loop.getLoop("2000A").getLoops().get(0).getId());
        assertEquals("2010AB", loop.getLoop("2000A").getLoops().get(1).getId());
        assertEquals("2000B", loop.getLoop("2000A").getLoops().get(2).getId());
        assertEquals("2000B", loop.getLoop("2000A").getLoops().get(3).getId());
        assertEquals("2010BA", loop.getLoop("2000B", 0).getLoops().get(0).getId());
        assertEquals("2010BB", loop.getLoop("2000B", 0).getLoops().get(1).getId());
        assertEquals("2300", loop.getLoop("2000B", 0).getLoops().get(2).getId());
        assertEquals("2400", loop.getLoop("2300", 0).getLoops().get(0).getId());
        assertEquals("2010BA", loop.getLoop("2000B", 1).getLoops().get(0).getId());
        assertEquals("2010BB", loop.getLoop("2000B", 1).getLoops().get(1).getId());
        assertEquals("2300", loop.getLoop("2000B", 1).getLoops().get(2).getId());
        assertEquals("2400", loop.getLoop("2300", 1).getLoops().get(0).getId());

        // testing grabbing data from each line
        assertEquals("030101", loop.getSegment("ISA").getElementValue("ISA09"));//First loop
        assertEquals("19991231", loop.getLoop("GS_LOOP").getSegment("GS").getElementValue("GS04"));
        assertEquals("987654", loop.getLoop("ST_LOOP").getSegment("ST").getElementValue("ST02"));
        assertEquals("0932", loop.getLoop("HEADER").getSegment("BHT").getElementValue("BHT05"));
        assertEquals("NM1", loop.getLoop("1000A").getSegment("NM1").getId());
        assertEquals("3016809770", loop.getLoop("1000A").getSegment("PER").getElementValue("PER04"));
        assertEquals("2", loop.getLoop("1000B").getSegment("NM1").getElementValue("NM102"));
        assertEquals("", loop.getLoop("2000A").getSegment("HL").getElementValue("HL02"));
        assertEquals("85", loop.getLoop("2010AA").getSegment("NM1").getElementValue("NM101"));
        assertEquals("N3", loop.getLoop("2010AA").getSegment("N3").getId());
        assertEquals("20705", loop.getLoop("2010AA").getSegment("N4").getElementValue("N403"));
        assertEquals("EI", loop.getLoop("2010AA").getSegment("REF").getElementValue("REF01"));
        assertEquals("JANE JONES", loop.getLoop("2010AA").getSegment("PER").getElementValue("PER02"));
        assertEquals("201", loop.getLoop("2010AA").getSegment("PER", 1).getElementValue("PER06"));
        assertEquals("87", loop.getLoop("2010AB").getSegment("NM1").getElementValue("NM101"));
        assertEquals("227 LASTNER LANE", loop.getLoop("2010AB").getSegment("N3").getElementValue("N301"));
        assertEquals("GREENBELT", loop.getLoop("2010AB").getSegment("N4").getElementValue("N401"));
        assertEquals("22", loop.getLoop("2000B").getSegment("HL").getElementValue("HL03"));
        assertEquals("", loop.getLoop("2000B").getSegment("SBR").getElementValue("SBR02"));
        assertEquals("123456", loop.getLoop("2010BA").getSegment("NM1").getElementValue("NM109"));
        assertEquals("PI", loop.getLoop("2010BB").getSegment("NM1").getElementValue("NM108"));
        assertEquals("A37YH556", loop.getLoop("2300").getSegment("CLM").getElementValue("CLM01"));
        assertEquals("BK", loop.getLoop("2300").getSegment("HI").getElementValue("HI01"));
        assertEquals("1", loop.getLoop("2400").getSegment("LX").getElementValue("LX01"));
        Character compositeSeparator = loop.getLoop("2400").getSegment("SV1").getSeparators().getCompositeElement();
        assertEquals("HC" + compositeSeparator + "99211" + compositeSeparator + "25", loop.getLoop("2400").getSegment("SV1").getElementValue("SV101"));
        assertEquals("RD8", loop.getLoop("2400").getSegment("DTP").getElementValue("DTP02"));

        // Tests of differences between repeating loops
        assertEquals("1", loop.getLoop("2000B", 0).getSegment("HL").getElementValue("HL02"));
        assertEquals("3", loop.getLoop("2000B", 1).getSegment("HL").getElementValue("HL02"));

        assertEquals("SUBSCRIBER GROUP", loop.getLoop("2000B", 0).getSegment("SBR").getElementValue("SBR03"));
        assertEquals("SUBSCRIBER GROUP TWO", loop.getLoop("2000B", 1).getSegment("SBR").getElementValue("SBR03"));

        assertEquals("JOHN", loop.getLoop("2010BA", 0).getSegment("NM1").getElementValue("NM104"));
        assertEquals("DAVID", loop.getLoop("2010BA", 1).getSegment("NM1").getElementValue("NM104"));

        assertEquals("HEALTH INSURANCE COMPANY", loop.getLoop("2010BB", 0).getSegment("NM1").getElementValue("NM103"));
        assertEquals("HEALTH INSURANCE COMPANY TWO", loop.getLoop("2010BB", 1).getSegment("NM1").getElementValue("NM103"));

        assertEquals("A37YH556", loop.getLoop("2300", 0).getSegment("CLM").getElementValue("CLM01"));
        assertEquals("A37YH667", loop.getLoop("2300", 1).getSegment("CLM").getElementValue("CLM01"));
        assertEquals(1, loop.getLoop("2300", 1).getSegment("CLM").getElement("CLM01").getNumOfSubElements());

        assertEquals("8901", loop.getLoop("2300", 0).getSegment("HI").getElementValue("HI02"));
        assertEquals("1987", loop.getLoop("2300", 1).getSegment("HI").getElementValue("HI02"));
        assertEquals(1, loop.getLoop("2300", 1).getSegment("HI").getElement("HI02").getNumOfSubElements());

        assertEquals("1", loop.getLoop("2400", 0).getSegment("LX").getElementValue("LX01"));
        assertEquals("2", loop.getLoop("2400", 1).getSegment("LX").getElementValue("LX01"));

        compositeSeparator = loop.getLoop("2400", 1).getSegment("SV1").getSeparators().getCompositeElement();
        assertEquals("HC" + compositeSeparator + "99211" + compositeSeparator + "25", loop.getLoop("2400", 0).getSegment("SV1").getElementValue("SV101"));
        assertEquals("HC" + compositeSeparator + "478331" + compositeSeparator + "25", loop.getLoop("2400", 1).getSegment("SV1").getElementValue("SV101"));
        assertEquals(3, loop.getLoop("2400", 1).getSegment("SV1").getElement("SV101").getNumOfSubElements());
        assertEquals("478331", loop.getLoop("2400", 1).getSegment("SV1").getElement("SV101", 1));
        assertNull(loop.getLoop("2400", 1).getSegment("SV1").getElement("SV101", 3));
        assertEquals(3, loop.getLoop("2400", 1).getSegment("SV1").getElement("SV107").getNumOfSubElements());
        assertEquals(1, loop.getLoop("2400", 0).getSegment("SV1").getElement("SV107").getNumOfSubElements());

        assertEquals("20050314-20050325", loop.getLoop("2400", 0).getSegment("DTP").getElementValue("DTP03"));
        assertEquals("20050322-20050325", loop.getLoop("2400", 1).getSegment("DTP").getElementValue("DTP03"));
        assertEquals(1, loop.getLoop("2400", 1).getSegment("DTP").getElement("DTP03").getNumOfSubElements());

        assertEquals("ISA_LOOP", loop.getId());
        assertNotNull(loop.getSegment("ISA"));
        assertEquals("00", loop.getSegment("ISA").getElementValue("ISA01"));
        assertEquals("030101", loop.getSegment("ISA").getElementValue("ISA09"));
        assertNotNull(loop.getSegment("IEA"));
        assertEquals("1", loop.getSegment("IEA").getElementValue("IEA01"));
        assertEquals("000000905", loop.getSegment("IEA").getElementValue("IEA02"));

        assertNotNull(loop.getLoop("GS_LOOP").getSegment("GS"));
        assertEquals("HC", loop.getLoop("GS_LOOP").getSegment("GS").getElementValue("GS01"));
        assertEquals("19991231", loop.getLoop("GS_LOOP").getSegment("GS").getElementValue("GS04"));
        assertNotNull(loop.getLoop("GS_LOOP").getSegment("GE"));
        assertEquals("1", loop.getLoop("GS_LOOP").getSegment("GE").getElementValue("GE01"));
        assertEquals("1", loop.getLoop("GS_LOOP").getSegment("GE").getElementValue("GE02"));

        assertNotNull(loop.getLoop("ST_LOOP").getSegment("ST"));
        assertEquals("837", loop.getLoop("ST_LOOP").getSegment("ST").getElementValue("ST01"));
        assertEquals("987654", loop.getLoop("ST_LOOP").getSegment("ST").getElementValue("ST02"));
        assertNotNull(loop.getLoop("ST_LOOP").getSegment("SE"));
        assertEquals("25", loop.getLoop("ST_LOOP").getSegment("SE").getElementValue("SE01"));
        assertEquals("987654", loop.getLoop("ST_LOOP").getSegment("SE").getElementValue("SE02"));
    }

    private void validateMultipleGSLoops(Loop loop) {
        assertEquals(3, loop.getLoops().size());
        assertEquals(1, loop.getLoop("GS_LOOP", 0).getLoops().size());
        assertEquals(2, loop.getLoop("ST_LOOP").getLoops().size());
        assertEquals(2, loop.getLoop("HEADER").getLoops().size());
        assertEquals(0, loop.getLoop("1000A").getLoops().size());
        assertEquals(0, loop.getLoop("1000B").getLoops().size());
        assertEquals(1, loop.getLoop("DETAIL").getLoops().size());
        assertEquals(4, loop.getLoop("2000A").getLoops().size());
        assertEquals(0, loop.getLoop("2010AA").getLoops().size());
        assertEquals(0, loop.getLoop("2010AB").getLoops().size());
        assertEquals(6, loop.findLoop("2000B").size());
        assertEquals(3, loop.getLoop("2000B", 0).getLoops().size());
        assertEquals(0, loop.getLoop("2010BA", 0).getLoops().size());
        assertEquals(0, loop.getLoop("2010BB", 0).getLoops().size());
        assertEquals(1, loop.getLoop("2300", 0).getLoops().size());
        assertEquals(0, loop.getLoop("2400", 0).getLoops().size());
        assertEquals(3, loop.getLoop("2000B", 1).getLoops().size());
        assertEquals(0, loop.getLoop("2010BA", 1).getLoops().size());
        assertEquals(0, loop.getLoop("2010BB", 1).getLoops().size());
        assertEquals(1, loop.getLoop("2300", 1).getLoops().size());
        assertEquals(0, loop.getLoop("2400", 1).getLoops().size());

        assertEquals(1, loop.getLoop("GS_LOOP", 1).getLoops().size());
        assertEquals(2, loop.getLoop(1).getLoop("ST_LOOP").getLoops().size());
        assertEquals(2, loop.getLoop(1).getLoop("HEADER").getLoops().size());
        assertEquals(0, loop.getLoop(1).getLoop("1000A").getLoops().size());
        assertEquals(0, loop.getLoop(1).getLoop("1000B").getLoops().size());
        assertEquals(1, loop.getLoop(1).getLoop("DETAIL").getLoops().size());
        assertEquals(4, loop.getLoop(1).getLoop("2000A").getLoops().size());
        assertEquals(0, loop.getLoop(1).getLoop("2010AA").getLoops().size());
        assertEquals(0, loop.getLoop(1).getLoop("2010AB").getLoops().size());
        assertEquals(3, loop.getLoop(1).getLoop("2000B", 0).getLoops().size());
        assertEquals(0, loop.getLoop(1).getLoop("2010BA", 0).getLoops().size());
        assertEquals(0, loop.getLoop(1).getLoop("2010BB", 0).getLoops().size());
        assertEquals(1, loop.getLoop(1).getLoop("2300", 0).getLoops().size());
        assertEquals(0, loop.getLoop(1).getLoop("2400", 0).getLoops().size());
        assertEquals(3, loop.getLoop(1).getLoop("2000B", 1).getLoops().size());
        assertEquals(0, loop.getLoop(1).getLoop("2010BA", 1).getLoops().size());
        assertEquals(0, loop.getLoop(1).getLoop("2010BB", 1).getLoops().size());
        assertEquals(1, loop.getLoop(1).getLoop("2300", 1).getLoops().size());
        assertEquals(0, loop.getLoop(1).getLoop("2400", 1).getLoops().size());

        assertEquals("20050314-20050325", loop.getLoop(1).getLoop("2400", 0).getSegment("DTP").getElementValue("DTP03"));
        assertEquals("20010523-20010601", loop.getLoop(1).getLoop("2400", 1).getSegment("DTP").getElementValue("DTP03"));

        assertEquals("GREENBELT", loop.getLoop(0).getLoop("2010AB").getSegment("N4").getElementValue("N401"));
        assertEquals("COLLEGE PARK", loop.getLoop(1).getLoop("2010AB").getSegment("N4").getElementValue("N401"));

        assertEquals("DAVID ANGELASZEK", loop.getLoop(0).getLoop("1000A").getSegment("PER").getElementValue("PER02"));
        assertEquals("DAVID JEFFREY ANGELASZEK", loop.getLoop(2).getLoop("1000A").getSegment("PER").getElementValue("PER02"));
    }

    private void validateMultipleISALoops(List<Loop> loops) {
        assertEquals(2, loops.size());
        for (Loop loop : loops) {
            assertEquals("ISA_LOOP", loop.getId());
            assertEquals(1, loop.getLoops().size());
            assertEquals(1, loop.getLoop("GS_LOOP", 0).getLoops().size());
            assertEquals(2, loop.getLoop("ST_LOOP").getLoops().size());
            assertEquals(2, loop.getLoop("HEADER").getLoops().size());
            assertEquals(0, loop.getLoop("1000A").getLoops().size());
            assertEquals(0, loop.getLoop("1000B").getLoops().size());
            assertEquals(1, loop.getLoop("DETAIL").getLoops().size());
            assertEquals(4, loop.getLoop("2000A").getLoops().size());
            assertEquals(0, loop.getLoop("2010AA").getLoops().size());
            assertEquals(0, loop.getLoop("2010AB").getLoops().size());
            assertEquals(2, loop.findLoop("2000B").size());
            assertEquals(3, loop.getLoop("2000B", 0).getLoops().size());
            assertEquals(0, loop.getLoop("2010BA", 0).getLoops().size());
            assertEquals(0, loop.getLoop("2010BB", 0).getLoops().size());
            assertEquals(2, loop.findLoop("2300").size());
            assertEquals(1, loop.getLoop("2300", 0).getLoops().size());
            assertEquals(1, loop.getLoop("2300", 1).getLoops().size());
            assertEquals(0, loop.getLoop("2400", 0).getLoops().size());
            assertEquals(3, loop.getLoop("2000B", 1).getLoops().size());
            assertEquals(0, loop.getLoop("2010BA", 1).getLoops().size());
            assertEquals(0, loop.getLoop("2010BB", 1).getLoops().size());
            assertEquals(0, loop.getLoop("2300", 0).getLoop("2400", 0).getLoops().size());
            assertEquals("20050314-20050325", loop.getLoop("2300").getLoop("2400", 0).getSegment("DTP").getElementValue("DTP03"));
            assertEquals("20050322-20050325", loop.getLoop("2300", 1).getLoop("2400", 0).getSegment("DTP").getElementValue("DTP03"));
            assertEquals("GREENBELT", loop.getLoop(0).getLoop("2010AB").getSegment("N4").getElementValue("N401"));
            assertEquals("DAVID ANGELASZEK", loop.getLoop(0).getLoop("1000A").getSegment("PER").getElementValue("PER02"));
            assertNotNull(loop.getSegment("IEA")); // test to ensure the final segment is included!
        }
    }

    private void validateMultipleSTLoops(Loop loop) {
        assertEquals(1, loop.getLoops().size());
        assertEquals(2, loop.getLoop("GS_LOOP").getLoops().size());
        assertEquals(2, loop.getLoop("ST_LOOP").getLoops().size());
        assertEquals(2, loop.getLoop("HEADER").getLoops().size());
        assertEquals(0, loop.getLoop("1000A").getLoops().size());
        assertEquals(0, loop.getLoop("1000B").getLoops().size());
        assertEquals(4, loop.getLoop("2000A").getLoops().size());
        assertEquals(0, loop.getLoop("2010AA").getLoops().size());
        assertEquals(0, loop.getLoop("2010AB").getLoops().size());
        assertEquals(4, loop.findLoop("2000B").size());
        assertEquals(3, loop.getLoop("2000B", 0).getLoops().size());
        assertEquals(0, loop.getLoop("2010BA", 0).getLoops().size());
        assertEquals(0, loop.getLoop("2010BB", 0).getLoops().size());
        assertEquals(1, loop.getLoop("2300", 0).getLoops().size());
        assertEquals(0, loop.getLoop("2400", 0).getLoops().size());
        assertEquals(3, loop.getLoop("2000B", 1).getLoops().size());
        assertEquals(0, loop.getLoop("2010BA", 1).getLoops().size());
        assertEquals(0, loop.getLoop("2010BB", 1).getLoops().size());
        assertEquals(1, loop.getLoop("2300", 1).getLoops().size());
        assertEquals(0, loop.getLoop("2400", 1).getLoops().size());

        assertEquals(2, loop.getLoop("ST_LOOP", 1).getLoops().size());
        assertEquals(2, loop.getLoop("ST_LOOP", 1).getLoop("HEADER").getLoops().size());
        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("1000A").getLoops().size());
        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("1000B").getLoops().size());
        assertEquals(1, loop.getLoop("ST_LOOP", 1).getLoop("DETAIL").getLoops().size());
        assertEquals(4, loop.getLoop("ST_LOOP", 1).getLoop("2000A").getLoops().size());
        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010AA").getLoops().size());
        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010AB").getLoops().size());
        assertEquals(3, loop.getLoop("ST_LOOP", 1).getLoop("2000B", 0).getLoops().size());
        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010BA", 0).getLoops().size());
        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010BB", 0).getLoops().size());
        assertEquals(1, loop.getLoop("ST_LOOP", 1).getLoop("2300", 0).getLoops().size());
        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2400", 0).getLoops().size());
        assertEquals(3, loop.getLoop("ST_LOOP", 1).getLoop("2000B", 1).getLoops().size());
        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010BA", 1).getLoops().size());
        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2010BB", 1).getLoops().size());
        assertEquals(1, loop.getLoop("ST_LOOP", 1).getLoop("2300", 1).getLoops().size());
        assertEquals(0, loop.getLoop("ST_LOOP", 1).getLoop("2400", 1).getLoops().size());

        assertEquals("227 LASTNER LANE", loop.getLoop("ST_LOOP", 0).getLoop("2010AB").getSegment("N3").getElementValue("N301"));
        assertEquals("21 CRAMER PATH", loop.getLoop("ST_LOOP", 1).getLoop("2010AB").getSegment("N3").getElementValue("N301"));

        assertEquals("JANE JANES", loop.getLoop("ST_LOOP", 0).getLoop("2010AA").getSegment("PER", 1).getElementValue("PER02"));
        assertEquals("JANICE JONES", loop.getLoop("ST_LOOP", 1).getLoop("2010AA").getSegment("PER", 1).getElementValue("PER02"));

        assertEquals("HEALTH INSURANCE COMPANY TWO", loop.getLoop("ST_LOOP", 0).getLoop("2010BB", 1).getSegment("NM1").getElementValue("NM103"));
        assertEquals("AN INSURANCE COMPANY", loop.getLoop("ST_LOOP", 1).getLoop("2010BB", 1).getSegment("NM1").getElementValue("NM103"));
    }

    @Test
    void testSegmentsNotInOrder() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_segments_out_of_order.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<String> errors = reader.getErrors();

        assertEquals(2, errors.size());

        assertTrue(errors.contains("Segment N4 in loop 2010AA is not in the correct position."));
        assertTrue(errors.contains("Segment N3 in loop 2010AB is not in the correct position."));
        assertTrue(reader.getFatalErrors().isEmpty());
    }

    @Test
    void testBadFirstLine() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_bad_first_line.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
        assertEquals(1, reader.getErrors().size());
        assertTrue(reader.getErrors().contains("Error getting separators"));
        assertFalse(reader.getFatalErrors().isEmpty());
    }

    @Test
    void testMissingFirstLine() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_no_isa_line.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
        assertEquals(1, reader.getErrors().size());
        assertTrue(reader.getErrors().contains("Error getting separators"));
        assertFalse(reader.getFatalErrors().isEmpty());
    }

    @Test
    void testConsistentVersions() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_valid.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X223, new File(url.getFile()));
        assertEquals(1, reader.getErrors().size());
        assertTrue(reader.getErrors().get(0).contains("not consistent with version specified"));

        reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
        assertTrue(reader.getErrors().isEmpty());
        assertTrue(reader.getFatalErrors().isEmpty());
    }

    @Test
    void testManyClaims() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_many_claims.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
        assertTrue(reader.getFatalErrors().isEmpty());
        List<Loop> loops = reader.getLoops();

        assertEquals(1, loops.size());
        Loop loop = loops.get(0);

        assertEquals("ISA_LOOP", loop.getId());
        assertNotNull(loop.getSegment("ISA"));
        assertEquals("00", loop.getSegment("ISA").getElementValue("ISA01"));
        assertEquals("030101", loop.getSegment("ISA").getElementValue("ISA09"));
        assertNotNull(loop.getSegment("IEA"));
        assertEquals("1", loop.getSegment("IEA").getElementValue("IEA01"));
        assertEquals("000000905", loop.getSegment("IEA").getElementValue("IEA02"));

        assertNotNull(loop.getLoop("GS_LOOP").getSegment("GS"));
        assertEquals("HC", loop.getLoop("GS_LOOP").getSegment("GS").getElementValue("GS01"));
        assertEquals("19991231", loop.getLoop("GS_LOOP").getSegment("GS").getElementValue("GS04"));
        assertNotNull(loop.getLoop("GS_LOOP").getSegment("GE"));
        assertEquals("1", loop.getLoop("GS_LOOP").getSegment("GE").getElementValue("GE01"));
        assertEquals("1", loop.getLoop("GS_LOOP").getSegment("GE").getElementValue("GE02"));

        assertNotNull(loop.getLoop("ST_LOOP").getSegment("ST"));
        assertEquals("837", loop.getLoop("ST_LOOP").getSegment("ST").getElementValue("ST01"));
        assertEquals("987654", loop.getLoop("ST_LOOP").getSegment("ST").getElementValue("ST02"));
        assertNotNull(loop.getLoop("ST_LOOP").getSegment("SE"));
        assertEquals("25", loop.getLoop("ST_LOOP").getSegment("SE").getElementValue("SE01"));
        assertEquals("987654", loop.getLoop("ST_LOOP").getSegment("SE").getElementValue("SE02"));

        assertEquals(2, loop.getLoop("HEADER").getLoops().size());
        assertEquals(0, loop.getLoop("1000A").getLoops().size());
        assertEquals(0, loop.getLoop("1000B").getLoops().size());
        assertEquals(410, loop.getLoop("ST_LOOP").findLoop("2000A").size());

        for (Loop loop2000A : loop.getLoop("ST_LOOP").findLoop("2000A")) {
            assertEquals(4, loop2000A.getLoops().size());
            assertEquals(0, loop2000A.getLoop("2010AA").getLoops().size());
            assertEquals(0, loop2000A.getLoop("2010AB").getLoops().size());
            assertEquals(2, loop2000A.findLoop("2000B").size());
            assertEquals(3, loop2000A.getLoop("2000B", 0).getLoops().size());
            assertEquals(0, loop2000A.getLoop("2010BA", 0).getLoops().size());
            assertEquals(0, loop2000A.getLoop("2010BB", 0).getLoops().size());
            assertEquals(1, loop2000A.getLoop("2300", 0).getLoops().size());
            assertEquals(0, loop2000A.getLoop("2400", 0).getLoops().size());
            assertEquals(3, loop2000A.getLoop("2000B", 1).getLoops().size());
            assertEquals(0, loop2000A.getLoop("2010BA", 1).getLoops().size());
            assertEquals(0, loop2000A.getLoop("2010BB", 1).getLoops().size());
            assertEquals(1, loop2000A.getLoop("2300", 1).getLoops().size());
            assertEquals(0, loop2000A.getLoop("2400", 1).getLoops().size());

            assertEquals("2010AA", loop2000A.getLoops().get(0).getId());
            assertEquals("2010AB", loop2000A.getLoops().get(1).getId());
            assertEquals("2000B", loop2000A.getLoops().get(2).getId());
            assertEquals("2000B", loop2000A.getLoops().get(3).getId());
            assertEquals("2010BA", loop2000A.getLoop("2000B", 0).getLoops().get(0).getId());
            assertEquals("2010BB", loop2000A.getLoop("2000B", 0).getLoops().get(1).getId());
            assertEquals("2300", loop2000A.getLoop("2000B", 0).getLoops().get(2).getId());
            assertEquals("2400", loop2000A.getLoop("2300", 0).getLoops().get(0).getId());
            assertEquals("2010BA", loop2000A.getLoop("2000B", 1).getLoops().get(0).getId());
            assertEquals("2010BB", loop2000A.getLoop("2000B", 1).getLoops().get(1).getId());
            assertEquals("2300", loop2000A.getLoop("2000B", 1).getLoops().get(2).getId());
            assertEquals("2400", loop2000A.getLoop("2300", 1).getLoops().get(0).getId());

            // testing grabbing data from each line
            assertEquals("", loop2000A.getSegment("HL").getElementValue("HL02"));
            assertEquals("85", loop2000A.getLoop("2010AA").getSegment("NM1").getElementValue("NM101"));
            assertEquals("N3", loop2000A.getLoop("2010AA").getSegment("N3").getId());
            assertEquals("20705", loop2000A.getLoop("2010AA").getSegment("N4").getElementValue("N403"));
            assertEquals("EI", loop2000A.getLoop("2010AA").getSegment("REF").getElementValue("REF01"));
            assertEquals("JANE JONES", loop2000A.getLoop("2010AA").getSegment("PER").getElementValue("PER02"));
            assertEquals("201", loop2000A.getLoop("2010AA").getSegment("PER", 1).getElementValue("PER06"));
            assertEquals("87", loop2000A.getLoop("2010AB").getSegment("NM1").getElementValue("NM101"));
            assertEquals("227 LASTNER LANE", loop2000A.getLoop("2010AB").getSegment("N3").getElementValue("N301"));
            assertEquals("GREENBELT", loop2000A.getLoop("2010AB").getSegment("N4").getElementValue("N401"));
            assertEquals("22", loop2000A.getLoop("2000B").getSegment("HL").getElementValue("HL03"));
            assertEquals("", loop2000A.getLoop("2000B").getSegment("SBR").getElementValue("SBR02"));
            assertEquals("123456", loop2000A.getLoop("2010BA").getSegment("NM1").getElementValue("NM109"));
            assertEquals("PI", loop2000A.getLoop("2010BB").getSegment("NM1").getElementValue("NM108"));
            assertEquals("A37YH556", loop2000A.getLoop("2300").getSegment("CLM").getElementValue("CLM01"));
            assertEquals("BK", loop2000A.getLoop("2300").getSegment("HI").getElementValue("HI01"));
            assertEquals("1", loop2000A.getLoop("2400").getSegment("LX").getElementValue("LX01"));
            Character compositeSeparator = loop2000A.getLoop("2400").getSegment("SV1").getSeparators().getCompositeElement();
            assertEquals("HC" + compositeSeparator + "99211" + compositeSeparator + "25", loop2000A.getLoop("2400").getSegment("SV1").getElementValue("SV101"));
            assertEquals("RD8", loop2000A.getLoop("2400").getSegment("DTP").getElementValue("DTP02"));

            // Tests of differences between repeating loops
            assertEquals("1", loop2000A.getLoop("2000B", 0).getSegment("HL").getElementValue("HL02"));
            assertEquals("3", loop2000A.getLoop("2000B", 1).getSegment("HL").getElementValue("HL02"));

            assertEquals("SUBSCRIBER GROUP", loop2000A.getLoop("2000B", 0).getSegment("SBR").getElementValue("SBR03"));
            assertEquals("SUBSCRIBER GROUP TWO", loop2000A.getLoop("2000B", 1).getSegment("SBR").getElementValue("SBR03"));

            assertEquals("JOHN", loop2000A.getLoop("2010BA", 0).getSegment("NM1").getElementValue("NM104"));
            assertEquals("DAVID", loop2000A.getLoop("2010BA", 1).getSegment("NM1").getElementValue("NM104"));

            assertEquals("HEALTH INSURANCE COMPANY", loop2000A.getLoop("2010BB", 0).getSegment("NM1").getElementValue("NM103"));
            assertEquals("HEALTH INSURANCE COMPANY TWO", loop2000A.getLoop("2010BB", 1).getSegment("NM1").getElementValue("NM103"));

            assertEquals("A37YH556", loop2000A.getLoop("2300", 0).getSegment("CLM").getElementValue("CLM01"));
            assertEquals("A37YH667", loop2000A.getLoop("2300", 1).getSegment("CLM").getElementValue("CLM01"));
            assertEquals(1, loop2000A.getLoop("2300", 1).getSegment("CLM").getElement("CLM01").getNumOfSubElements());

            assertEquals("8901", loop2000A.getLoop("2300", 0).getSegment("HI").getElementValue("HI02"));
            assertEquals("1987", loop2000A.getLoop("2300", 1).getSegment("HI").getElementValue("HI02"));
            assertEquals(1, loop2000A.getLoop("2300", 1).getSegment("HI").getElement("HI02").getNumOfSubElements());

            assertEquals("1", loop2000A.getLoop("2400", 0).getSegment("LX").getElementValue("LX01"));
            assertEquals("2", loop2000A.getLoop("2400", 1).getSegment("LX").getElementValue("LX01"));

            compositeSeparator = loop2000A.getLoop("2400", 1).getSegment("SV1").getSeparators().getCompositeElement();
            assertEquals("HC" + compositeSeparator + "99211" + compositeSeparator + "25", loop2000A.getLoop("2400", 0).getSegment("SV1").getElementValue("SV101"));
            assertEquals("HC" + compositeSeparator + "478331" + compositeSeparator + "25", loop2000A.getLoop("2400", 1).getSegment("SV1").getElementValue("SV101"));
            assertEquals(3, loop2000A.getLoop("2400", 1).getSegment("SV1").getElement("SV101").getNumOfSubElements());
            assertEquals("478331", loop2000A.getLoop("2400", 1).getSegment("SV1").getElement("SV101", 1));
            assertNull(loop2000A.getLoop("2400", 1).getSegment("SV1").getElement("SV101", 3));
            assertEquals(3, loop2000A.getLoop("2400", 1).getSegment("SV1").getElement("SV107").getNumOfSubElements());
            assertEquals(1, loop2000A.getLoop("2400", 0).getSegment("SV1").getElement("SV107").getNumOfSubElements());

            assertEquals("20050314-20050325", loop2000A.getLoop("2400", 0).getSegment("DTP").getElementValue("DTP03"));
            assertEquals("20050322-20050325", loop2000A.getLoop("2400", 1).getSegment("DTP").getElementValue("DTP03"));
            assertEquals(1, loop2000A.getLoop("2400", 1).getSegment("DTP").getElement("DTP03").getNumOfSubElements());
        }
    }

    @Test
    void testManyTransactions() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_multiple_transactions.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
        assertTrue(reader.getFatalErrors().isEmpty());
        List<Loop> loops = reader.getLoops();
        assertEquals(312, loops.size());

        for (Loop loop : loops) {
            assertEquals("ISA_LOOP", loop.getId());
            assertNotNull(loop.getSegment("ISA"));
            assertEquals("00", loop.getSegment("ISA").getElementValue("ISA01"));
            assertEquals("030101", loop.getSegment("ISA").getElementValue("ISA09"));
            assertNotNull(loop.getSegment("IEA"));
            assertEquals("1", loop.getSegment("IEA").getElementValue("IEA01"));
            assertEquals("000000905", loop.getSegment("IEA").getElementValue("IEA02"));

            assertNotNull(loop.getLoop("GS_LOOP").getSegment("GS"));
            assertEquals("HC", loop.getLoop("GS_LOOP").getSegment("GS").getElementValue("GS01"));
            assertEquals("19991231", loop.getLoop("GS_LOOP").getSegment("GS").getElementValue("GS04"));
            assertNotNull(loop.getLoop("GS_LOOP").getSegment("GE"));
            assertEquals("1", loop.getLoop("GS_LOOP").getSegment("GE").getElementValue("GE01"));
            assertEquals("1", loop.getLoop("GS_LOOP").getSegment("GE").getElementValue("GE02"));

            assertNotNull(loop.getLoop("ST_LOOP").getSegment("ST"));
            assertEquals("837", loop.getLoop("ST_LOOP").getSegment("ST").getElementValue("ST01"));
            assertEquals("987654", loop.getLoop("ST_LOOP").getSegment("ST").getElementValue("ST02"));
            assertNotNull(loop.getLoop("ST_LOOP").getSegment("SE"));
            assertEquals("25", loop.getLoop("ST_LOOP").getSegment("SE").getElementValue("SE01"));
            assertEquals("987654", loop.getLoop("ST_LOOP").getSegment("SE").getElementValue("SE02"));

            assertEquals(2, loop.getLoop("HEADER").getLoops().size());
            assertEquals(0, loop.getLoop("1000A").getLoops().size());
            assertEquals(0, loop.getLoop("1000B").getLoops().size());
            assertEquals(1, loop.getLoop("ST_LOOP").findLoop("2000A").size());

            for (Loop loop2000A : loop.getLoop("ST_LOOP").findLoop("2000A")) {
                assertEquals(4, loop2000A.getLoops().size());
                assertEquals(0, loop2000A.getLoop("2010AA").getLoops().size());
                assertEquals(0, loop2000A.getLoop("2010AB").getLoops().size());
                assertEquals(2, loop2000A.findLoop("2000B").size());
                assertEquals(3, loop2000A.getLoop("2000B", 0).getLoops().size());
                assertEquals(0, loop2000A.getLoop("2010BA", 0).getLoops().size());
                assertEquals(0, loop2000A.getLoop("2010BB", 0).getLoops().size());
                assertEquals(1, loop2000A.getLoop("2300", 0).getLoops().size());
                assertEquals(0, loop2000A.getLoop("2400", 0).getLoops().size());
                assertEquals(3, loop2000A.getLoop("2000B", 1).getLoops().size());
                assertEquals(0, loop2000A.getLoop("2010BA", 1).getLoops().size());
                assertEquals(0, loop2000A.getLoop("2010BB", 1).getLoops().size());
                assertEquals(1, loop2000A.getLoop("2300", 1).getLoops().size());
                assertEquals(0, loop2000A.getLoop("2400", 1).getLoops().size());

                assertEquals("2010AA", loop2000A.getLoops().get(0).getId());
                assertEquals("2010AB", loop2000A.getLoops().get(1).getId());
                assertEquals("2000B", loop2000A.getLoops().get(2).getId());
                assertEquals("2000B", loop2000A.getLoops().get(3).getId());
                assertEquals("2010BA", loop2000A.getLoop("2000B", 0).getLoops().get(0).getId());
                assertEquals("2010BB", loop2000A.getLoop("2000B", 0).getLoops().get(1).getId());
                assertEquals("2300", loop2000A.getLoop("2000B", 0).getLoops().get(2).getId());
                assertEquals("2400", loop2000A.getLoop("2300", 0).getLoops().get(0).getId());
                assertEquals("2010BA", loop2000A.getLoop("2000B", 1).getLoops().get(0).getId());
                assertEquals("2010BB", loop2000A.getLoop("2000B", 1).getLoops().get(1).getId());
                assertEquals("2300", loop2000A.getLoop("2000B", 1).getLoops().get(2).getId());
                assertEquals("2400", loop2000A.getLoop("2300", 1).getLoops().get(0).getId());

                // testing grabbing data from each line
                assertEquals("", loop2000A.getSegment("HL").getElementValue("HL02"));
                assertEquals("85", loop2000A.getLoop("2010AA").getSegment("NM1").getElementValue("NM101"));
                assertEquals("N3", loop2000A.getLoop("2010AA").getSegment("N3").getId());
                assertEquals("20705", loop2000A.getLoop("2010AA").getSegment("N4").getElementValue("N403"));
                assertEquals("EI", loop2000A.getLoop("2010AA").getSegment("REF").getElementValue("REF01"));
                assertEquals("JANE JONES", loop2000A.getLoop("2010AA").getSegment("PER").getElementValue("PER02"));
                assertEquals("201", loop2000A.getLoop("2010AA").getSegment("PER", 1).getElementValue("PER06"));
                assertEquals("87", loop2000A.getLoop("2010AB").getSegment("NM1").getElementValue("NM101"));
                assertEquals("227 LASTNER LANE", loop2000A.getLoop("2010AB").getSegment("N3").getElementValue("N301"));
                assertEquals("GREENBELT", loop2000A.getLoop("2010AB").getSegment("N4").getElementValue("N401"));
                assertEquals("22", loop2000A.getLoop("2000B").getSegment("HL").getElementValue("HL03"));
                assertEquals("", loop2000A.getLoop("2000B").getSegment("SBR").getElementValue("SBR02"));
                assertEquals("123456", loop2000A.getLoop("2010BA").getSegment("NM1").getElementValue("NM109"));
                assertEquals("PI", loop2000A.getLoop("2010BB").getSegment("NM1").getElementValue("NM108"));
                assertEquals("A37YH556", loop2000A.getLoop("2300").getSegment("CLM").getElementValue("CLM01"));
                assertEquals("BK", loop2000A.getLoop("2300").getSegment("HI").getElementValue("HI01"));
                assertEquals("1", loop2000A.getLoop("2400").getSegment("LX").getElementValue("LX01"));
                Character compositeSeparator = loop2000A.getLoop("2400").getSegment("SV1").getSeparators().getCompositeElement();
                assertEquals("HC" + compositeSeparator + "99211" + compositeSeparator + "25", loop2000A.getLoop("2400").getSegment("SV1").getElementValue("SV101"));
                assertEquals("RD8", loop2000A.getLoop("2400").getSegment("DTP").getElementValue("DTP02"));

                // Tests of differences between repeating loops
                assertEquals("1", loop2000A.getLoop("2000B", 0).getSegment("HL").getElementValue("HL02"));
                assertEquals("3", loop2000A.getLoop("2000B", 1).getSegment("HL").getElementValue("HL02"));

                assertEquals("SUBSCRIBER GROUP", loop2000A.getLoop("2000B", 0).getSegment("SBR").getElementValue("SBR03"));
                assertEquals("SUBSCRIBER GROUP TWO", loop2000A.getLoop("2000B", 1).getSegment("SBR").getElementValue("SBR03"));

                assertEquals("JOHN", loop2000A.getLoop("2010BA", 0).getSegment("NM1").getElementValue("NM104"));
                assertEquals("DAVID", loop2000A.getLoop("2010BA", 1).getSegment("NM1").getElementValue("NM104"));

                assertEquals("HEALTH INSURANCE COMPANY", loop2000A.getLoop("2010BB", 0).getSegment("NM1").getElementValue("NM103"));
                assertEquals("HEALTH INSURANCE COMPANY TWO", loop2000A.getLoop("2010BB", 1).getSegment("NM1").getElementValue("NM103"));

                assertEquals("A37YH556", loop2000A.getLoop("2300", 0).getSegment("CLM").getElementValue("CLM01"));
                assertEquals("A37YH667", loop2000A.getLoop("2300", 1).getSegment("CLM").getElementValue("CLM01"));
                assertEquals(1, loop2000A.getLoop("2300", 1).getSegment("CLM").getElement("CLM01").getNumOfSubElements());

                assertEquals("8901", loop2000A.getLoop("2300", 0).getSegment("HI").getElementValue("HI02"));
                assertEquals("1987", loop2000A.getLoop("2300", 1).getSegment("HI").getElementValue("HI02"));
                assertEquals(1, loop2000A.getLoop("2300", 1).getSegment("HI").getElement("HI02").getNumOfSubElements());

                assertEquals("1", loop2000A.getLoop("2400", 0).getSegment("LX").getElementValue("LX01"));
                assertEquals("2", loop2000A.getLoop("2400", 1).getSegment("LX").getElementValue("LX01"));

                compositeSeparator = loop2000A.getLoop("2400", 1).getSegment("SV1").getSeparators().getCompositeElement();
                assertEquals("HC" + compositeSeparator + "99211" + compositeSeparator + "25", loop2000A.getLoop("2400", 0).getSegment("SV1").getElementValue("SV101"));
                assertEquals("HC" + compositeSeparator + "478331" + compositeSeparator + "25", loop2000A.getLoop("2400", 1).getSegment("SV1").getElementValue("SV101"));
                assertEquals(3, loop2000A.getLoop("2400", 1).getSegment("SV1").getElement("SV101").getNumOfSubElements());
                assertEquals("478331", loop2000A.getLoop("2400", 1).getSegment("SV1").getElement("SV101", 1));
                assertNull(loop2000A.getLoop("2400", 1).getSegment("SV1").getElement("SV101", 3));
                assertEquals(3, loop2000A.getLoop("2400", 1).getSegment("SV1").getElement("SV107").getNumOfSubElements());
                assertEquals(1, loop2000A.getLoop("2400", 0).getSegment("SV1").getElement("SV107").getNumOfSubElements());

                assertEquals("20050314-20050325", loop2000A.getLoop("2400", 0).getSegment("DTP").getElementValue("DTP03"));
                assertEquals("20050322-20050325", loop2000A.getLoop("2400", 1).getSegment("DTP").getElementValue("DTP03"));
                assertEquals(1, loop2000A.getLoop("2400", 1).getSegment("DTP").getElement("DTP03").getNumOfSubElements());
            }
        }
    }

    @Test
    void testComplex() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_complex.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));
        assertTrue(reader.getFatalErrors().isEmpty());
        List<Loop> loops = reader.getLoops();
        assertEquals(1, loops.size());

        Loop loop = loops.get(0);
        assertEquals("ISA_LOOP", loop.getId());
        assertNotNull(loop.getSegment("ISA"));
        assertEquals("00", loop.getSegment("ISA").getElementValue("ISA01"));
        assertEquals("030101", loop.getSegment("ISA").getElementValue("ISA09"));
        assertNotNull(loop.getSegment("IEA"));
        assertEquals("1", loop.getSegment("IEA").getElementValue("IEA01"));
        assertEquals("000000905", loop.getSegment("IEA").getElementValue("IEA02"));

        assertNotNull(loop.getLoop("GS_LOOP").getSegment("GS"));
        assertEquals("HC", loop.getLoop("GS_LOOP").getSegment("GS").getElementValue("GS01"));
        assertEquals("19991231", loop.getLoop("GS_LOOP").getSegment("GS").getElementValue("GS04"));
        assertNotNull(loop.getLoop("GS_LOOP").getSegment("GE"));
        assertEquals("1", loop.getLoop("GS_LOOP").getSegment("GE").getElementValue("GE01"));
        assertEquals("1", loop.getLoop("GS_LOOP").getSegment("GE").getElementValue("GE02"));

        assertNotNull(loop.getLoop("ST_LOOP", 0).getSegment("ST"));
        assertEquals("837", loop.getLoop("ST_LOOP", 0).getSegment("ST").getElementValue("ST01"));
        assertEquals("987654", loop.getLoop("ST_LOOP", 0).getSegment("ST").getElementValue("ST02"));
        assertNotNull(loop.getLoop("ST_LOOP", 0).getSegment("SE"));
        assertEquals("25", loop.getLoop("ST_LOOP", 0).getSegment("SE").getElementValue("SE01"));
        assertEquals("987654", loop.getLoop("ST_LOOP", 0).getSegment("SE").getElementValue("SE02"));

        assertNotNull(loop.getLoop("ST_LOOP", 1).getSegment("ST"));
        assertEquals("837", loop.getLoop("ST_LOOP", 1).getSegment("ST").getElementValue("ST01"));
        assertEquals("987655", loop.getLoop("ST_LOOP", 1).getSegment("ST").getElementValue("ST02"));
        assertNotNull(loop.getLoop("ST_LOOP", 1).getSegment("SE"));
        assertEquals("26", loop.getLoop("ST_LOOP", 1).getSegment("SE").getElementValue("SE01"));
        assertEquals("987655", loop.getLoop("ST_LOOP", 1).getSegment("SE").getElementValue("SE02"));

        assertEquals(2, loop.getLoop("HEADER").getLoops().size());
        assertEquals(0, loop.getLoop("1000A").getLoops().size());
        assertEquals(0, loop.getLoop("1000B").getLoops().size());
        assertEquals(2, loop.findLoop("2000A").size());

        for (int i = 0; i < 2; i++) {
            Loop loop2000A = loop.getLoop("ST_LOOP", i).findLoop("2000A").get(0);
            assert2000A(loop2000A);
            assertEquals(1, loop2000A.findLoop("2010AA").size());
            assertEquals(0, loop2000A.getLoop("2010AA").getLoops().size());
            assert2010AA(loop.getLoop("2010AA"));

            assertEquals(1, loop2000A.findLoop("2010AB").size());
            assertEquals(0, loop2000A.getLoop("2010AB").getLoops().size());
            assert2010AB(loop.getLoop("2010AB"));

            assertEquals(1, loop2000A.findLoop("2010AC").size());
            assertEquals(0, loop2000A.getLoop("2010AC").getLoops().size());
            assert2010AC(loop.getLoop("2010AC"));

            assertEquals(2, loop2000A.findLoop("2000B").size());
            assert2000B(loop2000A);

            assertEquals(2, loop2000A.findLoop("2010BA").size());
            assert2010BA(loop2000A);

            assertEquals(2, loop2000A.findLoop("2010BB").size());
            assert2010BB(loop2000A);

            assertEquals(1, loop2000A.findLoop("2000C").size());
            assert2000C(loop2000A.getLoop("2000B", 1));

            assertEquals(3, loop2000A.findLoop("2300").size());
            assertEquals(1, loop2000A.getLoop("2000B", 0).findLoop("2300").size());
            assertEquals(2, loop2000A.getLoop("2000B", 1).getLoop("2000C").findLoop("2300").size());
            assert2300(loop2000A, i);

            assertEquals(4, loop2000A.findLoop("2310A").size());
            assertEquals(2, loop2000A.getLoop("2000B", 0).getLoop("2300").findLoop("2310A").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).findLoop("2310A").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).findLoop("2310A").size());
            assert2310A(loop2000A);

            assertEquals(2, loop2000A.findLoop("2310B").size());
            assertEquals(1, loop2000A.getLoop("2000B", 0).getLoop("2300").findLoop("2310B").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).findLoop("2310B").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).findLoop("2310B").size());
            assert2310B(loop2000A);

            assertEquals(3, loop2000A.findLoop("2310C").size());
            assertEquals(1, loop2000A.getLoop("2000B", 0).getLoop("2300").findLoop("2310C").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).findLoop("2310C").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).findLoop("2310C").size());
            assert2310C(loop2000A);

            assertEquals(1, loop2000A.findLoop("2310D").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).findLoop("2310D").size());
            assert2310D(loop2000A);

            assertEquals(1, loop2000A.findLoop("2310E").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).findLoop("2310E").size());
            assert2310E(loop2000A);

            assertEquals(1, loop2000A.findLoop("2310F").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).findLoop("2310F").size());
            assert2310F(loop2000A);

            assertEquals(4, loop2000A.findLoop("2320").size());
            assertEquals(3, loop2000A.getLoop("2000B", 0).getLoop("2300").findLoop("2320").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).findLoop("2320").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).findLoop("2320").size());
            assert2320(loop2000A);

            assertEquals(4, loop2000A.findLoop("2330A").size());
            assertEquals(1, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 0).findLoop("2330A").size());
            assertEquals(1, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 1).findLoop("2330A").size());
            assertEquals(1, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 2).findLoop("2330A").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).getLoop("2320", 0).findLoop("2330A").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).findLoop("2330A").size());
            assert2330A(loop2000A);

            assertEquals(4, loop2000A.findLoop("2330B").size());
            assertEquals(1, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 0).findLoop("2330B").size());
            assertEquals(1, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 1).findLoop("2330B").size());
            assertEquals(1, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 2).findLoop("2330B").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).getLoop("2320", 0).findLoop("2330B").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).findLoop("2330B").size());
            assert2330B(loop2000A);

            assertEquals(4, loop2000A.findLoop("2330C").size());
            assertEquals(1, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 1).findLoop("2330C").size());
            assertEquals(1, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 2).findLoop("2330C").size());
            assertEquals(2, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).findLoop("2330C").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).findLoop("2330C").size());
            assert2330C(loop2000A);

            assertEquals(1, loop2000A.findLoop("2330D").size());
            assertEquals(0, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 1).findLoop("2330D").size());
            assertEquals(0, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 2).findLoop("2330D").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).findLoop("2330D").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).findLoop("2330D").size());
            assert2330D(loop2000A);

            assertEquals(1, loop2000A.findLoop("2330E").size());
            assertEquals(0, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 1).findLoop("2330E").size());
            assertEquals(0, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 2).findLoop("2330E").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).findLoop("2330E").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).findLoop("2330E").size());
            assert2330E(loop2000A);

            assertEquals(1, loop2000A.findLoop("2330F").size());
            assertEquals(0, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 1).findLoop("2330F").size());
            assertEquals(0, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 2).findLoop("2330F").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).findLoop("2330F").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).findLoop("2330F").size());
            assert2330F(loop2000A);

            assertEquals(2, loop2000A.findLoop("2330G").size());
            assertEquals(0, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 1).findLoop("2330G").size());
            assertEquals(1, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2320", 2).findLoop("2330G").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).findLoop("2330G").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).findLoop("2330G").size());
            assert2330G(loop2000A);

            assertEquals(5, loop2000A.findLoop("2400").size());
            assertEquals(2, loop2000A.getLoop("2000B", 0).getLoop("2300").findLoop("2400").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).findLoop("2400").size());
            assertEquals(2, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).findLoop("2400").size());
            assert2400(loop2000A, i);

            assertEquals(2, loop2000A.findLoop("2410").size());
            assertEquals(1, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2400", 0).findLoop("2410").size());
            assertEquals(0, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2400", 1).findLoop("2410").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).getLoop("2400", 0).findLoop("2410").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).getLoop("2400", 0).findLoop("2410").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).getLoop("2400", 1).findLoop("2410").size());
            assert2410(loop2000A);

            assertEquals(1, loop2000A.findLoop("2420A").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).getLoop("2400", 0).findLoop("2420A").size());
            assert2420A(loop2000A);

            assertEquals(1, loop2000A.findLoop("2420B").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).getLoop("2400", 0).findLoop("2420B").size());
            assert2420B(loop2000A);

            assertEquals(1, loop2000A.findLoop("2420C").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).getLoop("2400", 0).findLoop("2420C").size());
            assert2420C(loop2000A);

            assertEquals(1, loop2000A.findLoop("2420D").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).getLoop("2400", 0).findLoop("2420D").size());
            assert2420D(loop2000A);

            assertEquals(1, loop2000A.findLoop("2420E").size());
            assertEquals(0, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2400", 0).findLoop("2420E").size());
            assertEquals(0, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2400", 1).findLoop("2420E").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).getLoop("2400", 0).findLoop("2420E").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).getLoop("2400", 0).findLoop("2420E").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).getLoop("2400", 1).findLoop("2420E").size());
            assert2420E(loop2000A, i);

            assertEquals(4, loop2000A.findLoop("2420F").size());
            assertEquals(2, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2400", 0).findLoop("2420F").size());
            assertEquals(0, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2400", 1).findLoop("2420F").size());
            assertEquals(2, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).getLoop("2400", 0).findLoop("2420F").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).getLoop("2400", 0).findLoop("2420F").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).getLoop("2400", 1).findLoop("2420F").size());
            assert2420F(loop2000A);

            assertEquals(1, loop2000A.findLoop("2420G").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).getLoop("2400", 0).findLoop("2420G").size());
            assert2420G(loop2000A);

            assertEquals(1, loop2000A.findLoop("2420H").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).getLoop("2400", 0).findLoop("2420H").size());
            assert2420H(loop2000A);

            assertEquals(1, loop2000A.findLoop("2430").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).getLoop("2400", 0).findLoop("2430").size());
            assert2430(loop2000A);

            assertEquals(4, loop2000A.findLoop("2440").size());
            assertEquals(1, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2400", 0).findLoop("2440").size());
            assertEquals(0, loop2000A.getLoop("2000B", 0).getLoop("2300").getLoop("2400", 1).findLoop("2440").size());
            assertEquals(1, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 0).getLoop("2400", 0).findLoop("2440").size());
            assertEquals(0, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).getLoop("2400", 0).findLoop("2440").size());
            assertEquals(2, loop2000A.getLoop("2000B", 1).getLoop("2000C").getLoop("2300", 1).getLoop("2400", 1).findLoop("2440").size());
            assert2440(loop2000A);
        }
    }

    private void assert2000A(Loop loop) {
        assertEquals("", loop.getSegment("HL").getElementValue("HL02"));
        assertEquals("20", loop.getSegment("HL").getElementValue("HL03"));
        assertEquals("PXC", loop.getSegment("PRV").getElementValue("PRV02"));
    }

    private void assert2010AA(Loop loop) {
        assertEquals("85", loop.getSegment("NM1").getElementValue("NM101"));
        assertEquals("N3", loop.getSegment("N3").getId());
        assertEquals("20705", loop.getSegment("N4").getElementValue("N403"));
        assertEquals("EI", loop.getSegment("REF").getElementValue("REF01"));
        assertEquals("JANE JONES", loop.getSegment("PER").getElementValue("PER02"));
        assertEquals("201", loop.getSegment("PER", 1).getElementValue("PER06"));
    }

    private void assert2010AB(Loop loop) {
        assertEquals("87", loop.getSegment("NM1").getElementValue("NM101"));
        assertEquals("227 LASTNER LANE", loop.getSegment("N3").getElementValue("N301"));
        assertEquals("GREENBELT", loop.getSegment("N4").getElementValue("N401"));
    }

    private void assert2010AC(Loop loop) {
        assertEquals("NAME", loop.getSegment("NM1").getElementValue("NM103"));
        assertEquals("ADDRESS", loop.getSegment("N3").getElementValue("N301"));
        assertEquals("GERMANTOWN", loop.getSegment("N4").getElementValue("N401"));
        assertEquals("EI", loop.getSegment("REF").getElementValue("REF01"));
    }

    private void assert2000B(Loop loop) {
        assertEquals("1", loop.getLoop("2000B", 0).getSegment("HL").getElementValue("HL02"));
        assertEquals("SUBSCRIBER GROUP", loop.getLoop("2000B", 0).getSegment("SBR").getElementValue("SBR03"));

        assertEquals("3", loop.getLoop("2000B", 1).getSegment("HL").getElementValue("HL02"));
        assertEquals("SUBSCRIBER GROUP TWO", loop.getLoop("2000B", 1).getSegment("SBR").getElementValue("SBR03"));
    }

    private void assert2010BA(Loop loop) {
        assertEquals("JOHN", loop.getLoop("2010BA", 0).getSegment("NM1").getElementValue("NM104"));
        assertEquals("DAVID", loop.getLoop("2010BA", 1).getSegment("NM1").getElementValue("NM104"));
    }

    private void assert2010BB(Loop loop) {
        assertEquals("HEALTH INSURANCE COMPANY", loop.getLoop("2010BB", 0).getSegment("NM1").getElementValue("NM103"));
        assertEquals("HEALTH INSURANCE COMPANY TWO", loop.getLoop("2010BB", 1).getSegment("NM1").getElementValue("NM103"));
    }

    private void assert2000C(Loop loop) {
        assertEquals("23", loop.getLoop("2000C").getSegment("HL").getElementValue("HL03"));
        assertEquals("01", loop.getLoop("2000C").getSegment("PAT").getElementValue("PAT01"));
    }

    private void assert2300(Loop loop, int i) {
        assertEquals("A37YH556", loop.getLoop("2300", 0).getSegment("CLM").getElementValue("CLM01"));
        assertEquals("8901", loop.getLoop("2300", 0).getSegment("HI").getElementValue("HI02"));
        assertEquals("A37YH667", loop.getLoop("2300", 1).getSegment("CLM").getElementValue("CLM01"));
        assertEquals("1987", loop.getLoop("2300", 1).getSegment("HI").getElementValue("HI02"));
        assertEquals("A37YH668", loop.getLoop("2300", 2).getSegment("CLM").getElementValue("CLM01"));
        assertEquals(i == 0 ? "1988" : "1989", loop.getLoop("2300", 2).getSegment("HI").getElementValue("HI02"));
    }

    private void assert2310A(Loop loop) {
        assertEquals("WELBY", loop.getLoop("2310A", 0).getSegment("NM1").getElementValue("NM103"));
        assertEquals("12345", loop.getLoop("2310A", 0).getSegment("REF").getElementValue("REF02"));

        assertEquals("QELBY", loop.getLoop("2310A", 1).getSegment("NM1").getElementValue("NM103"));
        assertEquals("12348", loop.getLoop("2310A", 1).getSegment("REF").getElementValue("REF02"));

        assertEquals("RELBY", loop.getLoop("2310A", 2).getSegment("NM1").getElementValue("NM103"));
        assertEquals("12347", loop.getLoop("2310A", 2).getSegment("REF").getElementValue("REF02"));

        assertEquals("TELBY", loop.getLoop("2310A", 3).getSegment("NM1").getElementValue("NM103"));
        assertEquals("12346", loop.getLoop("2310A", 3).getSegment("REF").getElementValue("REF02"));
    }

    private void assert2310B(Loop loop) {
        assertEquals("2310B1", loop.getLoop("2310B", 0).getSegment("NM1").getElementValue("NM103"));
        assertEquals("2310B2", loop.getLoop("2310B", 1).getSegment("NM1").getElementValue("NM103"));
    }

    private void assert2310C(Loop loop) {
        assertEquals("2310C1", loop.getLoop("2310C", 0).getSegment("NM1").getElementValue("NM103"));
        assertEquals("2310C STREET 1", loop.getLoop("2310C", 0).getSegment("N3").getElementValue("N301"));
        assertEquals("2310C CITY 1", loop.getLoop("2310C", 0).getSegment("N4").getElementValue("N401"));

        assertEquals("2310C2", loop.getLoop("2310C", 1).getSegment("NM1").getElementValue("NM103"));
        assertEquals("2310C STREET 2", loop.getLoop("2310C", 1).getSegment("N3").getElementValue("N301"));
        assertEquals("2310C CITY 2", loop.getLoop("2310C", 1).getSegment("N4").getElementValue("N401"));

        assertEquals("2310C3", loop.getLoop("2310C", 2).getSegment("NM1").getElementValue("NM103"));
        assertEquals("2310C STREET 3", loop.getLoop("2310C", 2).getSegment("N3").getElementValue("N301"));
        assertEquals("2310C CITY 3", loop.getLoop("2310C", 2).getSegment("N4").getElementValue("N401"));
    }

    private void assert2310D(Loop loop) {
        assertEquals("2310D1", loop.getLoop("2310D", 0).getSegment("NM1").getElementValue("NM103"));
        assertEquals("12345", loop.getLoop("2310D", 0).getSegment("REF").getElementValue("REF02"));
    }

    private void assert2310E(Loop loop) {
        assertEquals("PW", loop.getLoop("2310E", 0).getSegment("NM1").getElementValue("NM101"));
        assertEquals("2310E STREET 1", loop.getLoop("2310E", 0).getSegment("N3").getElementValue("N301"));
        assertEquals("2310E CITY 1", loop.getLoop("2310E", 0).getSegment("N4").getElementValue("N401"));
    }

    private void assert2310F(Loop loop) {
        assertEquals("45", loop.getLoop("2310F", 0).getSegment("NM1").getElementValue("NM101"));
        assertEquals("2310F STREET 1", loop.getLoop("2310F", 0).getSegment("N3").getElementValue("N301"));
        assertEquals("2310F CITY 1", loop.getLoop("2310F", 0).getSegment("N4").getElementValue("N401"));
    }

    private void assert2320(Loop loop) {
        assertEquals("GR00786", loop.getLoop("2320", 0).getSegment("SBR").getElementValue("SBR03"));
        assertEquals("A", loop.getLoop("2320", 0).getSegment("OI").getElementValue("OI04"));

        assertEquals("GR00788", loop.getLoop("2320", 1).getSegment("SBR").getElementValue("SBR03"));
        assertEquals("C", loop.getLoop("2320", 1).getSegment("OI").getElementValue("OI04"));

        assertEquals("GR00789", loop.getLoop("2320", 2).getSegment("SBR").getElementValue("SBR03"));
        assertEquals("D", loop.getLoop("2320", 2).getSegment("OI").getElementValue("OI04"));

        assertEquals("GR00787", loop.getLoop("2320", 3).getSegment("SBR").getElementValue("SBR03"));
        assertEquals("B", loop.getLoop("2320", 3).getSegment("OI").getElementValue("OI04"));
    }

    private void assert2330A(Loop loop) {
        assertEquals("2330A1", loop.getLoop("2330A", 0).getSegment("NM1").getElementValue("NM103"));
        assertEquals("2330A3", loop.getLoop("2330A", 1).getSegment("NM1").getElementValue("NM103"));
        assertEquals("2330A4", loop.getLoop("2330A", 2).getSegment("NM1").getElementValue("NM103"));
        assertEquals("2330A2", loop.getLoop("2330A", 3).getSegment("NM1").getElementValue("NM103"));
    }

    private void assert2330B(Loop loop) {
        assertEquals("2330B1", loop.getLoop("2330B", 0).getSegment("NM1").getElementValue("NM103"));
        assertEquals("2330B3", loop.getLoop("2330B", 1).getSegment("NM1").getElementValue("NM103"));
        assertEquals("2330B4", loop.getLoop("2330B", 2).getSegment("NM1").getElementValue("NM103"));
        assertEquals("2330B2", loop.getLoop("2330B", 3).getSegment("NM1").getElementValue("NM103"));
    }

    private void assert2330C(Loop loop) {
        assertEquals("DN", loop.getLoop("2330C", 0).getSegment("NM1").getElementValue("NM101"));
        assertEquals("DN", loop.getLoop("2330C", 1).getSegment("NM1").getElementValue("NM101"));
    }

    private void assert2330D(Loop loop) {
        assertEquals("82", loop.getLoop("2330D", 0).getSegment("NM1").getElementValue("NM101"));
        assertEquals("2330D1", loop.getLoop("2330D", 0).getSegment("REF").getElementValue("REF02"));
    }

    private void assert2330E(Loop loop) {
        assertEquals("77", loop.getLoop("2330E", 0).getSegment("NM1").getElementValue("NM101"));
        assertEquals("2330E1", loop.getLoop("2330E", 0).getSegment("REF").getElementValue("REF02"));
    }

    private void assert2330F(Loop loop) {
        assertEquals("DQ", loop.getLoop("2330F", 0).getSegment("NM1").getElementValue("NM101"));
        assertEquals("2330F1", loop.getLoop("2330F", 0).getSegment("REF").getElementValue("REF02"));
    }

    private void assert2330G(Loop loop) {
        assertEquals("85", loop.getLoop("2330G", 0).getSegment("NM1").getElementValue("NM101"));
        assertEquals("2330G1", loop.getLoop("2330G", 0).getSegment("REF").getElementValue("REF02"));
    }

    private void assert2400(Loop loop, int i) {
        assertEquals("1", loop.getLoop("2400", 0).getSegment("LX").getElementValue("LX01"));
        assertEquals("12.25", loop.getLoop("2400", 0).getSegment("SV1").getElementValue("SV102"));
        assertEquals("20150330", loop.getLoop("2400", 0).getSegment("DTP").getElementValue("DTP03"));

        assertEquals("2", loop.getLoop("2400", 1).getSegment("LX").getElementValue("LX01"));
        assertEquals("13.26", loop.getLoop("2400", 1).getSegment("SV1").getElementValue("SV102"));
        assertEquals("20150331", loop.getLoop("2400", 1).getSegment("DTP").getElementValue("DTP03"));

        assertEquals("3", loop.getLoop("2400", 2).getSegment("LX").getElementValue("LX01"));
        assertEquals((i == 0 || i == 1) ? "12.26" : "9.98", loop.getLoop("2400", 2).getSegment("SV1").getElementValue("SV102"));
        assertEquals("20160324", loop.getLoop("2400", 2).getSegment("DTP").getElementValue("DTP03"));

        assertEquals("4", loop.getLoop("2400", 3).getSegment("LX").getElementValue("LX01"));
        assertEquals("12.30", loop.getLoop("2400", 3).getSegment("SV1").getElementValue("SV102"));
        assertEquals("20170623", loop.getLoop("2400", 3).getSegment("DTP").getElementValue("DTP03"));

        assertEquals("5", loop.getLoop("2400", 4).getSegment("LX").getElementValue("LX01"));
        assertEquals("12.31", loop.getLoop("2400", 4).getSegment("SV1").getElementValue("SV102"));
        assertEquals("20170624", loop.getLoop("2400", 4).getSegment("DTP").getElementValue("DTP03"));
    }

    private void assert2410(Loop loop) {
        assertEquals("3920293", loop.getLoop("2410", 0).getSegment("LIN").getElementValue("LIN03"));
        assertEquals("2", loop.getLoop("2410", 0).getSegment("CTP").getElementValue("CTP04"));
    }

    private void assert2420A(Loop loop) {
        assertEquals("2420A", loop.getLoop("2420A", 0).getSegment("NM1").getElementValue("NM103"));
        assertEquals("2420A", loop.getLoop("2420A", 0).getSegment("PRV").getElementValue("PRV03"));
    }

    private void assert2420B(Loop loop) {
        assertEquals("2420B", loop.getLoop("2420B", 0).getSegment("NM1").getElementValue("NM109"));
    }

    private void assert2420C(Loop loop) {
        assertEquals("2420C", loop.getLoop("2420C", 0).getSegment("NM1").getElementValue("NM103"));
        assertEquals("2420C STREET 1", loop.getLoop("2420C", 0).getSegment("N3").getElementValue("N301"));
        assertEquals("2420C CITY 1", loop.getLoop("2420C", 0).getSegment("N4").getElementValue("N401"));
    }

    private void assert2420D(Loop loop) {
        assertEquals("2420D", loop.getLoop("2420D", 0).getSegment("NM1").getElementValue("NM103"));
    }

    private void assert2420E(Loop loop, int i) {
        assertEquals((i == 0 || i == 1) ? "2420E" : "2420E-1", loop.getLoop("2420E", 0).getSegment("NM1").getElementValue("NM103"));
    }

    private void assert2420F(Loop loop) {
        assertEquals("PELBY", loop.getLoop("2420F", 0).getSegment("NM1").getElementValue("NM103"));
        assertEquals("VELBY", loop.getLoop("2420F", 1).getSegment("NM1").getElementValue("NM103"));
    }

    private void assert2420G(Loop loop) {
        assertEquals("PW", loop.getLoop("2420G", 0).getSegment("NM1").getElementValue("NM101"));
        assertEquals("2420G STREET 1", loop.getLoop("2420G", 0).getSegment("N3").getElementValue("N301"));
        assertEquals("2420G CITY 1", loop.getLoop("2420G", 0).getSegment("N4").getElementValue("N401"));
    }

    private void assert2420H(Loop loop) {
        assertEquals("45", loop.getLoop("2420H", 0).getSegment("NM1").getElementValue("NM101"));
        assertEquals("2420H STREET 1", loop.getLoop("2420H", 0).getSegment("N3").getElementValue("N301"));
        assertEquals("2420H CITY 1", loop.getLoop("2420H", 0).getSegment("N4").getElementValue("N401"));
    }

    private void assert2430(Loop loop) {
        assertEquals("HC:2430", loop.getLoop("2430", 0).getSegment("SVD").getElementValue("SVD03"));
        assertEquals("20200728", loop.getLoop("2430", 0).getSegment("DTP").getElementValue("DTP03"));
    }

    private void assert2440(Loop loop) {
        assertEquals("01.02", loop.getLoop("2440", 0).getSegment("LQ").getElementValue("LQ02"));
        assertEquals("12N", loop.getLoop("2440", 0).getSegment("FRM").getElementValue("FRM01"));

        assertEquals("01.02", loop.getLoop("2440", 1).getSegment("LQ").getElementValue("LQ02"));
        assertEquals("12N", loop.getLoop("2440", 1).getSegment("FRM").getElementValue("FRM01"));

        assertEquals("01.03", loop.getLoop("2440", 2).getSegment("LQ").getElementValue("LQ02"));
        assertEquals("12M", loop.getLoop("2440", 2).getSegment("FRM").getElementValue("FRM01"));

        assertEquals("01.04", loop.getLoop("2440", 3).getSegment("LQ").getElementValue("LQ02"));
        assertEquals("12Q", loop.getLoop("2440", 3).getSegment("FRM").getElementValue("FRM01"));
    }

    @Test
    void testX223Repeated2320() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x223-test.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X223, new FileInputStream(url.getFile()));

        assertEquals(1, reader.getLoops().size());
        Loop loop = reader.getLoops().get(0);
        assertEquals(1, loop.getLoops().size());
        assertEquals(1, loop.findLoop("2000B").size());
        assertEquals(2, loop.getLoop("2000B").getSegments().size());
        assertEquals("HL", loop.getLoop("2000B").getSegment(0).getId());
        assertEquals("SBR", loop.getLoop("2000B").getSegment(1).getId());
        assertEquals("SUBSCRIBER GROUP", loop.getLoop("2000B").getSegment(1).getElement("SBR03").getValue());
        assertEquals(2, loop.findLoop("2320").size());
        assertEquals(2, loop.getLoop("2000B").findLoop("2320").size());
        assertEquals(1, loop.getLoop("2000B").getLoop("2320", 0).findLoop("2330A").size());
        assertEquals(1, loop.getLoop("2000B").getLoop("2320", 0).findLoop("2330B").size());
        assertEquals(1, loop.getLoop("2000B").getLoop("2320", 1).findLoop("2330A").size());
        assertEquals(1, loop.getLoop("2000B").getLoop("2320", 1).findLoop("2330B").size());
        assertEquals("S", loop.getLoop("2320", 0).getSegment(0).getElement("SBR01").getValue());
        assertEquals("T", loop.getLoop("2320", 1).getSegment(0).getElement("SBR01").getValue());
        assertEquals("JOHN", loop.getLoop("2320", 0).getLoop("2330A").getSegment(0).getElement("NM104").getValue());
        assertEquals("JANE", loop.getLoop("2320", 1).getLoop("2330A").getSegment(0).getElement("NM104").getValue());
        assertEquals("AETNA", loop.getLoop("2320", 0).getLoop("2330B").getSegment(0).getElement("NM103").getValue());
        assertEquals("ANOTHER NAME", loop.getLoop("2320", 1).getLoop("2330B").getSegment(0).getElement("NM103").getValue());
    }

    @Test
    void test277CAAccepted() throws Exception {
        URL url = this.getClass().getResource("/277_5010/x12_277CA_accepted.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI277_5010_X214, new File(url.getFile()));

        List<Loop> loops = reader.getLoops();
        assertEquals(1, loops.size());
        Loop loop = reader.getLoops().get(0);
        assertEquals(1, loop.getLoops().size());
        assertEquals("1107000000014420", loop.getLoop("GS_LOOP").getLoop("ST_LOOP").getLoop("DETAIL")
                .getLoop("2000A")
                .getLoop("2000B")
                .getLoop("2000C")
                .getLoop("2000D")
                .getLoop("2200D")
                .getSegment("REF")
                .getElement("REF02").getValue(), "Should be able to find the claim number");
        Element statusCodeElement = loop.getLoop("GS_LOOP").getLoop("ST_LOOP").getLoop("DETAIL")
                .getLoop("2000A")
                .getLoop("2000B")
                .getLoop("2000C")
                .getLoop("2000D")
                .getLoop("2200D")
                .getSegment("STC")
                .getElement("STC01");
        assertEquals("A2", statusCodeElement.getSubValues().get(0), "Should be able to see a approval status code - CSCC");
        assertEquals("20", statusCodeElement.getSubValues().get(1), "Should be able to see a approval status code - CSC");
    }

    @Test
    void test277CARejected() throws Exception {
        URL url = this.getClass().getResource("/277_5010/x12_277CA_rejected.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI277_5010_X214, new File(url.getFile()));

        List<Loop> loops = reader.getLoops();
        assertEquals(1, loops.size());
        Loop loop = reader.getLoops().get(0);
        assertEquals(1, loop.getLoops().size());
        Element statusCodeElement = loop.getLoop("GS_LOOP").getLoop("ST_LOOP").getLoop("DETAIL")
                .getLoop("2000A")
                .getLoop("2000B")
                .getLoop("2000C")
                .getLoop("2000D")
                .getLoop("2200D")
                .getSegment("STC")
                .getElement("STC01");
        assertEquals("A7", statusCodeElement.getSubValues().get(0), "Should be able to see a approval status code - CSCC");
        assertEquals("562", statusCodeElement.getSubValues().get(1), "Should be able to see a approval status code - CSC");
        assertEquals("85", statusCodeElement.getSubValues().get(2), "Should be able to see a approval status code - EIC");
    }

    @Test
    void test999Accepted() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_999_accepted.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X231, new File(url.getFile()));

        List<Loop> loops = reader.getLoops();
        assertEquals(1, loops.size());
        Loop loop = reader.getLoops().get(0);
        assertEquals(1, loop.getLoops().size());
        assertEquals("A", loop.getLoop("GS_LOOP").getLoop("ST_LOOP").getLoop("HEADER").getLoop("2000").getSegment("IK5").getElement("IK501").getValue());

    }

    @Test
    void test999Rejected() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_999_rejected.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X231, new File(url.getFile()));

        List<Loop> loops = reader.getLoops();
        assertEquals(1, loops.size());
        Loop loop = reader.getLoops().get(0);
        assertEquals(1, loop.getLoops().size());
        assertEquals("R", loop.getLoop("GS_LOOP").getLoop("ST_LOOP").getLoop("HEADER").getLoop("2000").getSegment("IK5").getElement("IK501").getValue());

    }

    @Test
    void test270() throws Exception {
        URL url = this.getClass().getResource("/x270_271/x270.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI270_4010_X092, new File(url.getFile()));

        List<Loop> loops = reader.getLoops();
        assertEquals(1, loops.size());
        Loop loop = reader.getLoops().get(0);
        assertEquals(1, loop.getLoops().size());

        Element statusCodeElement = loop.getLoop("GS_LOOP").getLoop("ST_LOOP").getLoop("DETAIL")
                .getLoop("2000A")
                .getLoop("2000B")
                .getLoop("2000C")
                .getLoop("2100C")
                .getLoop("2110C")
                .getSegment("EQ")
                .getElement("EQ01");
        assertEquals("30", statusCodeElement.getSubValues().get(0));
    }

    @Test
    void test271() throws Exception {
        URL url = this.getClass().getResource("/x270_271/x271.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI271_4010_X092, new File(url.getFile()));

        List<Loop> loops = reader.getLoops();
        assertEquals(1, loops.size());
        Loop loop = reader.getLoops().get(0);
        assertEquals(1, loop.getLoops().size());

        Element statusCodeElement = loop.getLoop("GS_LOOP").getLoop("ST_LOOP").getLoop("DETAIL")
                .getLoop("2000A")
                .getLoop("2000B")
                .getLoop("2000C")
                .getLoop("2100C")
                .getLoop("2110C")
                .getLoop("2120C")
                .getSegment("NM1")
                .getElement("NM101");
        assertEquals("P3", statusCodeElement.getSubValues().get(0));
    }

    @Test
    void testAmbiguousLoop() throws Exception {
        URL url = this.getClass().getResource("/837_5010/x12_ambiguous_loop.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI837_5010_X222, new File(url.getFile()));

        List<Loop> loops = reader.getLoops();
        assertEquals(1, loops.size());
        assertEquals(1, loops.get(0).findLoop("2420A").size());
        assertEquals("PERSON", loops.get(0).findLoop("2420A").get(0).getElement("NM1", "NM103"));
        assertEquals("2420A", loops.get(0).findLoop("2420A").get(0).getElement("PRV", "PRV03"));
        assertTrue(loops.get(0).findLoop("2330D").isEmpty());
        assertTrue(loops.get(0).findLoop("2310B").isEmpty());
    }

    @Test
    void test277X212() throws Exception {
        URL url = this.getClass().getResource("/277_5010/x12_277_x212.txt");
        assertNotNull(url);
        X12Reader reader = new X12Reader(FileType.ANSI277_5010_X212, new File(url.getFile()));

        List<Loop> loops = reader.getLoops();
        assertEquals(1, loops.size());
        Loop loop = reader.getLoops().get(0);
        assertEquals(1, loop.getLoops().size());
        assertEquals("PR", loop.getLoop("GS_LOOP").getLoop("ST_LOOP").getLoop("DETAIL")
                .getLoop("2000A")
                .getLoop("2100A")
                .getSegment("NM1")
                .getElement("NM101").getValue(), "Should be able to find the Information Source Detail - Payer Name");
        assertEquals("41", loop.getLoop("GS_LOOP").getLoop("ST_LOOP").getLoop("DETAIL")
                .getLoop("TABLE2AREA3")
                .getLoop("2000B")
                .getLoop("2100B")
                .getSegment("NM1")
                .getElement("NM101").getValue(), "Should be able to find the Information Receiver Detail - Information Receiver Name");
        assertEquals("EJ", loop.getLoop("GS_LOOP").getLoop("ST_LOOP").getLoop("DETAIL")
                .getLoop("TABLE2AREA5")
                .getLoop("2000D")
                .getLoop("2200D")
                .getSegment("REF")
                .getElement("REF01").getValue(), "Should be able to find the Claim Status Tracking Number - Payer Claim Control Number");
        Element statusCodeElement = loop.getLoop("GS_LOOP").getLoop("ST_LOOP").getLoop("DETAIL")
                .getLoop("TABLE2AREA5")
                .getLoop("2000D")
                .getLoop("2200D")
                .getSegment("STC")
                .getElement("STC01");
        assertEquals("A1", statusCodeElement.getSubValues().get(0), "Should be able to see a health care claim status category code - C043");
        assertEquals("704", statusCodeElement.getSubValues().get(1), "Should be able to see a health care claim status category code - 1271");
    }
}
