package com.imsweb.x12.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import com.imsweb.x12.Loop;
import com.imsweb.x12.Segment;
import com.imsweb.x12.Separators;
import com.imsweb.x12.mapping.CompositeDefinition;
import com.imsweb.x12.mapping.ElementDefinition;
import com.imsweb.x12.mapping.LoopDefinition;
import com.imsweb.x12.mapping.SegmentDefinition;
import com.imsweb.x12.mapping.TransactionDefinition;
import com.imsweb.x12.mapping.TransactionDefinition.Usage;

public class X12Reader {

    private static int _ISA_LENGTH = 106;
    private static int _ELEMENT_SEPARATOR_POS = 3; // array position
    private static int _COMPOSITE_SEPARATOR_POS = 104; // array position
    private static int _SEGMENT_SEPARATOR_POS = 105; // array position

    private Loop _dataLoop;
    private List<String> _errors = new ArrayList<>();
    private List<LoopConfig> _config = new ArrayList<>();
    TransactionDefinition _definition;

    /**
     * All supported X12 file definitions
     */
    public enum FileType {
        ANSI835_5010_X221("mapping/835.5010.X221.A1.xml"),
        ANSI835_4010_X091("mapping/835.4010.X091.A1.xml"),
        ANSI837_4010_X096("mapping/837.4010.X096.A1.xml"),
        ANSI837_4010_X097("mapping/837.4010.X097.A1.xml"),
        ANSI837_4010_X098("mapping/837.4010.X098.A1.xml"),
        ANSI837_5010_X222("mapping/837.5010.X222.A1.xml");

        private String _mapping;

        private static Map<String, TransactionDefinition> _DEFINITIONS = new HashMap<>();

        FileType(String mapping) {
            _mapping = mapping;
        }

        /**
         * Load definition from file
         * @return a TransactionDefinition
         */
        protected synchronized TransactionDefinition getDefinition() {
            if (!_DEFINITIONS.containsKey(_mapping)) {
                XStream xstream = new XStream(new StaxDriver());
                xstream.autodetectAnnotations(true);
                xstream.alias("transaction", TransactionDefinition.class);

                TransactionDefinition def = (TransactionDefinition)xstream.fromXML(Thread.currentThread().getContextClassLoader().getResourceAsStream(_mapping));
                _DEFINITIONS.put(_mapping, def);
            }

            return _DEFINITIONS.get(_mapping);
        }
    }

    /**
     * Constructs an X12Reader using a File with default character encoding
     * @param type the type of x12 file
     * @param file a File object representing the input file
     * @throws IOException if there was an error reading the input file
     */
    public X12Reader(FileType type, File file) throws IOException {
        parse(type, new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset())));
    }

    /**
     * Constructs an X12Reader using a File
     * @param type the type of x12 file
     * @param file a File object representing the input file
     * @param charset character encoding
     * @throws IOException if there was an error reading the input file
     */
    public X12Reader(FileType type, File file, Charset charset) throws IOException {
        parse(type, new BufferedReader(new InputStreamReader(new FileInputStream(file), charset)));
    }

    /**
     * Constructs an X12Reader using an InputStream with default character encoding
     * @param type the type of x12 file
     * @param input an InputStream to an input file
     * @throws IOException if there was an error reading the input file
     */
    public X12Reader(FileType type, InputStream input) throws IOException {
        parse(type, new BufferedReader(new InputStreamReader(input, Charset.defaultCharset())));
    }

    /**
     * Constructs an X12Reader using an InputStream
     * @param type the type of x12 file
     * @param input an InputStream to an input file
     * @throws IOException if there was an error reading the input file
     */
    public X12Reader(FileType type, InputStream input, Charset charset) throws IOException {
        parse(type, new BufferedReader(new InputStreamReader(input, charset)));
    }

    /**
     * Constructs an X12Reader using a Reader
     * @param type the type of x12 file
     * @param reader a Reader pointing to an input file
     * @throws IOException if there was an error reading the input file
     */
    public X12Reader(FileType type, Reader reader) throws IOException {
        // the Reader must support mark; if it does not, wrap the reader in a BufferedReader
        if (!reader.markSupported())
            parse(type, new BufferedReader(reader));
        else
            parse(type, reader);
    }

    /**
     * Return the resulting loop
     * @return the loop
     */
    public Loop getLoop() {
        return _dataLoop;
    }

    /**
     * Return the list of errors, if any
     * @return a list of errors
     */
    public List<String> getErrors() {
        return _errors;
    }

    /**
     * Parse a Readable into a Loop
     * @param type file type definition
     * @param reader reader
     * @return
     */
    private void parse(FileType type, Reader reader) throws IOException {
        Scanner scanner = new Scanner(reader);

        // set up delimiters
        Separators separators = getSeparators(reader);
        if (separators != null) {
            Character segmentSeparator = separators.getSegment();
            String quotedSegmentSeparator = Pattern.quote(segmentSeparator.toString());
            scanner.useDelimiter(quotedSegmentSeparator + "\r\n|" + quotedSegmentSeparator + "\n|" + quotedSegmentSeparator);

            List<String> loopLines = new ArrayList<>(); // holds the lines from the claims files that all belong to the same loop
            String loopId;
            String previousLoopId = null;
            String currentLoopId = null;

            // parse _definition file
            _definition = type.getDefinition();

            // cache definitions of loop starting segments
            getLoopConfiguration(_definition.getLoop(), null);

            _dataLoop = new Loop(null);
            _errors = new ArrayList<>();

            String line = scanner.next().trim();
            while (scanner.hasNext()) {
                // Determine if we have started a new loop
                loopId = getMatchedLoop(line.split(Pattern.quote(separators.getElement().toString())), previousLoopId);
                if (loopId != null) {
                    updateLoopCounts(loopId);

                    // validate the lines and add to list of errors if there are any
                    validateLines(loopLines, previousLoopId, separators);

                    if (_dataLoop.getId() == null && loopLines.size() > 0) {
                        _dataLoop.setId(previousLoopId);
                        for (String s : loopLines) {
                            Segment seg = new Segment();
                            seg.addElements(s);
                            _dataLoop.addSegment(seg);
                        }
                    }
                    else if (_dataLoop.getId() != null) {
                        if (currentLoopId == null)
                            currentLoopId = previousLoopId;
                        currentLoopId = storeData(previousLoopId, loopLines, currentLoopId);
                    }

                    loopLines = new ArrayList<>();
                    loopLines.add(line);
                    previousLoopId = loopId;
                }
                else
                    loopLines.add(line);

                try {
                    line = scanner.next().trim();
                }
                catch (NoSuchElementException e) {
                    // do nothing for now
                }
            }

            storeData(previousLoopId, loopLines, currentLoopId);

            //checking the loop data to see if there any requirements violations
            for (LoopConfig lc : _config) {
                if (Usage.REQUIRED.equals(lc.getLoopUsage()) && lc.getParentLoop() != null && lc.getLoopRepeatCount() != 0 && !compareRepeats(lc.getLoopRepeatCount(), lc.getLoopRepeats(),
                        lc.getParentLoop())) { //checks to see if a loop appears too many times
                    //(takes into account that the parent loop may appear more than once)
                    _errors.add(lc.getLoopId() + " appears too many times");
                }

                else if (Usage.SITUATIONAL.equals(lc.getLoopUsage()) && lc.getLoopRepeatCount() > 0) {  //For situational loops that appear!
                    if (lc.getParentLoop() != null && !compareRepeats(lc.getLoopRepeatCount(), lc.getLoopRepeats(), lc.getParentLoop())) {    //checks to see if a loop appears too many times
                        _errors.add(lc.getLoopId() + " appears too many times");
                    }
                }

                Set<String> requiredChildLoops = new HashSet<>();
                requiredChildLoops = getRequiredChildLoops(_definition.getLoop(), lc.getLoopId(), requiredChildLoops);
                for (int i = 0; i < _dataLoop.findLoop(lc.getLoopId()).size(); i++) {
                    List<String> childLoops = getChildLoopsFromData(_dataLoop.findLoop(lc.getLoopId()).get(i).getLoops());
                    for (String ids : requiredChildLoops)
                        if (!childLoops.contains(ids))
                            _errors.add(ids + " is required but not found in " + lc.getLoopId() + " iteration #" + (i + 1));

                }
            }
        }
    }

    private List<String> getChildLoopsFromData(List<Loop> loops) {
        return loops.stream().map(Loop::getId).collect(Collectors.toList());
    }

    private Set<String> getRequiredChildLoops(LoopDefinition loop, String id, Set<String> requiredChildList) {
        if (!loop.getXid().equals(id)) {
            if (loop.getLoop() != null)
                for (LoopDefinition subloop : loop.getLoop()) {
                    requiredChildList = getRequiredChildLoops(subloop, id, requiredChildList);
                }
        }
        else if (loop.getLoop() != null)
            for (LoopDefinition l : loop.getLoop())
                if (l.getUsage().equals(Usage.REQUIRED))
                    requiredChildList.add(l.getXid());

        return requiredChildList;
    }

    /**
     * Determines the characters for each separator used in an x12 file
     * @param reader that is used to read the x12 file
     * @return Separator object instantiated with the appropriate separators.
     * @throws IOException
     */
    private Separators getSeparators(Reader reader) throws IOException {
        reader.mark(1);
        char[] firstLine = new char[_ISA_LENGTH];
        int ret = reader.read(firstLine);
        boolean isAlphaNumeric = Character.isDigit(firstLine[_SEGMENT_SEPARATOR_POS]) || Character.isDigit(firstLine[_ELEMENT_SEPARATOR_POS]) || Character.isDigit(firstLine[_COMPOSITE_SEPARATOR_POS])
                ||
                Character.isLetter(firstLine[_SEGMENT_SEPARATOR_POS]) || Character.isLetter(firstLine[_ELEMENT_SEPARATOR_POS]) || Character.isLetter(firstLine[_COMPOSITE_SEPARATOR_POS]);

        boolean isWhiteSpace = Character.isSpaceChar(firstLine[_SEGMENT_SEPARATOR_POS]) || Character.isSpaceChar(firstLine[_ELEMENT_SEPARATOR_POS]) || Character.isSpaceChar(
                firstLine[_COMPOSITE_SEPARATOR_POS]);
        if (ret != _ISA_LENGTH || (isAlphaNumeric || isWhiteSpace)) {
            _errors.add("Error getting separators");
            return null;
        }
        reader.reset();

        return new Separators(firstLine[_SEGMENT_SEPARATOR_POS], firstLine[_ELEMENT_SEPARATOR_POS], firstLine[_COMPOSITE_SEPARATOR_POS]);
    }

    /**
     * Stores data, one loop at a time, into the _dataLoop structure.
     * @param currentLoopId---the loopID of the current loop
     * @param loopLines---the data file segments that belong to this loop
     * @param prevousLoopId---the previous loop id that was stored
     * @return loopID----the id of the loop that was just stored
     */

    private String storeData(String currentLoopId, List<String> loopLines, String prevousLoopId) {
        Loop newLoop = new Loop(currentLoopId);
        for (String s : loopLines) {
            Segment seg = new Segment();
            seg.addElements(s);
            newLoop.addSegment(seg);
        }
        String parentName = getParentLoop(currentLoopId, prevousLoopId);
        int primaryIndex = 0;
        int secondaryIndex = 0;

        if (_dataLoop.getLoops().size() > 0)
            primaryIndex = _dataLoop.getLoops().size() - 1;
        if (_dataLoop.getLoops().size() > 0 && _dataLoop.getLoop(primaryIndex).getLoops().size() > 0)
            secondaryIndex = _dataLoop.getLoop(primaryIndex).getLoops().size() - 1;

        if (_dataLoop.getLoops().size() == 0) // if no child loops have been stored yet.
            _dataLoop.addLoop(0, newLoop);
        else if (_dataLoop.getId().equals(parentName)) {
            int index = _dataLoop.getLoops().size();
            _dataLoop.addLoop(index, newLoop);
        }
        else if (_dataLoop.getLoop(primaryIndex).getLoops().size() != 0 && !_dataLoop.getLoop(primaryIndex).getLoop(secondaryIndex).hasLoop(parentName) && !_dataLoop.getLoop(primaryIndex).getId()
                .equals(
                        parentName)) { //if the parent loop for the current loop has not been stored---we need to create it. (Happens for loops with no segements!!!)
            String oldParentName = parentName;
            parentName = getParentLoop(parentName, prevousLoopId);

            if (!_dataLoop.getLoop(primaryIndex).getLoop(secondaryIndex).getId().equals(parentName)) { //if the parent loop of the loop we want to create is NOT the second loop in the path
                int index = _dataLoop.getLoop(primaryIndex).getLoop(secondaryIndex).getLoop(parentName).getLoops().size();
                _dataLoop.getLoop(primaryIndex).getLoop(secondaryIndex).getLoop(parentName).addLoop(index, new Loop(oldParentName));

            }
            else { //parent loop of the loop we want to create is the second loop in the path
                int index = _dataLoop.getLoop(primaryIndex).getLoop(parentName, secondaryIndex).getLoops().size();
                _dataLoop.getLoop(primaryIndex).getLoop(parentName, secondaryIndex).addLoop(index, new Loop(oldParentName));

            }
            _dataLoop.getLoop(primaryIndex).getLoop(secondaryIndex).getLoop(oldParentName).addLoop(0, newLoop);

        }

        else {
            int parentIndex;
            int index;

            //the primary loop path has no loops in it
            if (_dataLoop.getLoop(primaryIndex).getLoops().size() == 0 || _dataLoop.getLoop(primaryIndex).getLoop(secondaryIndex).findLoop(parentName).size() == 0) {
                parentIndex = _dataLoop.findLoop(parentName).size() - 1;
                index = _dataLoop.getLoop(parentName, parentIndex).getLoops().size();
            }
            else {
                parentIndex = _dataLoop.getLoop(primaryIndex).getLoop(secondaryIndex).findLoop(parentName).size() - 1;
                index = _dataLoop.getLoop(primaryIndex).getLoop(secondaryIndex).getLoop(parentName, parentIndex).getLoops().size();
            }
            //if the primary loop path has child loops and the first loop is NOT the parent loop
            if (_dataLoop.getLoop(primaryIndex).getLoops().size() != 0 && _dataLoop.getLoop(primaryIndex).getLoop(secondaryIndex).getLoops().size() != 0 && !_dataLoop.getLoop(primaryIndex).getId()
                    .equals(
                            parentName))
                _dataLoop.getLoop(primaryIndex).getLoop(secondaryIndex).getLoop(parentName, parentIndex).addLoop(index, newLoop);

            else
                _dataLoop.getLoop(parentName, parentIndex).addLoop(index, newLoop);
        }
        return currentLoopId;
    }

    /**
     * Stores some data from the XML defintion into a loop configuration object
     * @param loop loop to be proceesed
     * @param parentID parent loop id of the loop being processed
     */

    private void getLoopConfiguration(LoopDefinition loop, String parentID) {
        if (!containsLoop(loop.getXid())) {
            if (loop.getLoop() != null) {
                LoopConfig loopConfig = new LoopConfig(loop.getXid(), parentID, getChildLoops(loop), loop.getRepeat(), loop.getUsage());
                if (loop.getSegment() != null)
                    loopConfig.setFirstSegmentXid(loop.getSegment().get(0));

                parentID = loop.getXid();
                _config.add(loopConfig);
                for (LoopDefinition loops : loop.getLoop()) {
                    getLoopConfiguration(loops, parentID);
                }

            }
            else {
                LoopConfig loopConfig = new LoopConfig(loop.getXid(), parentID, null, loop.getRepeat(), loop.getUsage());
                if (loop.getSegment() != null)
                    loopConfig.setFirstSegmentXid(loop.getSegment().get(0));
                _config.add(loopConfig);

            }
        }
    }

    /**
     * Get the list of child loops from a particular loop
     * @param loop loop we are getting the child loops for
     * @return childLoops list of the child loops
     */
    private List<String> getChildLoops(LoopDefinition loop) {
        return loop.getLoop().stream().map(LoopDefinition::getXid).collect(Collectors.toList());
    }

    /**
     * checks to see if the current loop ID is already in the config object
     * @param id id of the loop we want to test
     * @return true if the loop id is found in the config structure, false if it is not
     */
    private boolean containsLoop(String id) {
        for (LoopConfig loop : _config)
            if (loop.getLoopId().equals(id))
                return true;

        return false;
    }

    /**
     * Return the parent loop
     * @param loopId loop identifier
     * @return patient loop if found, otherwise null
     */
    private String getParentLoop(String loopId, String previousLoopId) {
        List<String> parentLoops = new ArrayList<>();
        List<String> previousParentLoops = new ArrayList<>();

        parentLoops = getParentLoopsFromDefintion(_definition.getLoop(), loopId, parentLoops);
        previousParentLoops = getParentLoopsFromDefintion(_definition.getLoop(), previousLoopId, previousParentLoops);

        if (previousLoopId != null)
            for (String parentLoop : parentLoops)
                if (previousParentLoops.contains(parentLoop))
                    return parentLoop;

        if (parentLoops.size() != 0)
            return parentLoops.get(0);
        else
            return null;
    }

    /**
     * Gets all possible parent loops from the defintion object. Need a list since it is possible for one loop to have two different parents.
     * @param loop ---- object to loop over
     * @param id --- the id of the loop we want to find parents for
     * @param parentLoop --- list of parent loops.
     * @return
     */
    private List<String> getParentLoopsFromDefintion(LoopDefinition loop, String id, List<String> parentLoop) {
        if (loop.getLoop() != null)
            for (LoopDefinition subloop : loop.getLoop()) {
                if (subloop.getXid().equals(id)) {
                    parentLoop.add(loop.getXid());
                }
                parentLoop = getParentLoopsFromDefintion(subloop, id, parentLoop);
            }

        return parentLoop;
    }

    /**
     * updates the number of times a loop appears in the data
     * @param loopId id of the loop we need to count
     */
    private void updateLoopCounts(String loopId) {
        _config.stream().filter(config -> config.getLoopId().equals(loopId)).forEach(LoopConfig::incrementLoopRepeatCount);
    }

    /**
     * Determines if a segment data line is the start of a new loop
     * @param tokens array of the data split on element separators
     * @param previousLoopID the id of the previous loop that was matched
     * @return the matched loop
     */
    private String getMatchedLoop(String[] tokens, String previousLoopID) {
        List<String> matchedLoops = new ArrayList<>();
        for (LoopConfig config : _config) {
            SegmentDefinition id = config.getFirstSegmentXid();
            if (id != null && tokens[0].equals(id.getXid()) && codesValidatedForLoopId(tokens, id)) {

                if (isChildSegment(previousLoopID, tokens))
                    return null;

                if (!matchedLoops.contains(config.getLoopId()))
                    matchedLoops.add(config.getLoopId());
            }
        }

        if (matchedLoops.size() > 1)
            return getFinalizedMatch(previousLoopID, matchedLoops);
        else if (matchedLoops.size() == 1)
            return matchedLoops.get(0);

        return null;
    }

    /**
     * Check if the current line is a child segment of the current loop. if it is then we should assume we are not starting new loop.
     * @param previousLoopId the previous loop that was matched
     * @param tokens array of the data split on element separators
     * @return true if it is a child segment, false if it is not.
     */
    private boolean isChildSegment(String previousLoopId, String[] tokens) {

        List<SegmentDefinition> loopSegs = getSegmentDefintions(_definition.getLoop(), previousLoopId);
        if (loopSegs != null) {
            for (int i = 1; i < loopSegs.size(); i++) { // we want to skip the first segment
                SegmentDefinition seg = loopSegs.get(i);
                if (seg.getXid().equals(tokens[0]))
                    return true;
            }
        }
        return false;

    }

    /**
     * Removes ambiguity in matched loops. We check the current matched loops to see if they are a child loop or sibling of the previous
     * matched loop. If either of those conditions are met, we return that loop as the finalized match. If not, we return the first match
     * found
     * @param previousLoopId the id of the previous loop that was matched
     * @param matchedLoopsId list of loop ids that match
     * @return the finalized loop match
     */
    private String getFinalizedMatch(String previousLoopId, List<String> matchedLoopsId) {
        for (LoopConfig lc : _config) {
            if (lc.getLoopId().equals(previousLoopId)) {
                for (String s1 : matchedLoopsId)
                    if (lc.getChildList() != null && lc.getChildList().contains(s1))
                        return s1;
                for (String s2 : matchedLoopsId) {
                    String parentLoop = getParentLoop(previousLoopId, null);
                    if (parentLoop != null && parentLoop.equals(getParentLoop(s2, null)))
                        return s2;
                }
            }
        }
        return matchedLoopsId.get(0);
    }

    /**
     * Validates the segments for a single loop
     * @param segments list of segments from the data file that belong to a specific loop
     * @param loopId loop id we are validating segments for
     * @return boolean indicating validation success
     */
    private boolean validateLines(List<String> segments, String loopId, Separators separators) {
        List<SegmentDefinition> format = getSegmentDefintions(_definition.getLoop(), loopId);
        int[] segmentCounter = new int[format.size()];
        boolean lineMatchesFormat = false;

        for (String segment : segments) {
            int i = 0;
            for (SegmentDefinition segmentConf : format) {
                String[] tokens = segment.split(Pattern.quote(separators.getElement().toString()));
                if (tokens[0].equals(segmentConf.getXid()) && codesValidated(tokens, segmentConf)) {
                    segmentCounter[i]++;
                    lineMatchesFormat = true;
                    break;
                }
                i++;
            }

            if (!lineMatchesFormat)
                _errors.add("Unable to find a matching segment format in loop " + loopId);

            lineMatchesFormat = false;
        }

        return validateSegments(segments, format, loopId, segmentCounter, separators);
    }

    /**
     * Checks that the valid codes for eah required element are there----loop ID purposes only
     * @param tokens array of the data split on element separators
     * @param segmentConf information on the current segment being processed.
     * @return false if the codes are found to be valid, false otherwise
     */
    private boolean codesValidatedForLoopId(String[] tokens, SegmentDefinition segmentConf) {
        Map<List<String>, Integer> validCodes = getValidCodes(segmentConf);
        List<Integer> requiredElements = getRequiredElementPositions(segmentConf);
        List<Integer> positions = new ArrayList<>();
        List<List<String>> codes = new ArrayList<>();
        positions.addAll(validCodes.values());
        codes.addAll(validCodes.keySet());

        for (int i = 1; i < tokens.length; i++)
            if (tokens[i] != null && !tokens[i].isEmpty() && positions.contains(i) && requiredElements.contains(i))
                if (!codes.get(positions.indexOf(i)).contains(tokens[i]))
                    return false;

        return true;

    }

    /**
     * Checks that the valid codes for each element are there
     * @param tokens array of the data split on element separators
     * @param segmentConf information on the current segment being processed.
     * @return false if the codes are found to be valid, false otherwise
     */
    private boolean codesValidated(String[] tokens, SegmentDefinition segmentConf) {
        Map<List<String>, Integer> validCodes = getValidCodes(segmentConf);
        List<Integer> positions = new ArrayList<>();
        List<List<String>> codes = new ArrayList<>();
        positions.addAll(validCodes.values());
        codes.addAll(validCodes.keySet());

        for (int i = 1; i < tokens.length; i++)
            if (tokens[i] != null && !tokens[i].isEmpty() && positions.contains(i))
                if (!codes.get(positions.indexOf(i)).contains(tokens[i]))
                    return false;

        return true;

    }

    /**
     * Validates the usage, repeat count, and if all required data appears for each segment in a loop.
     * @param segments list of data segments for a particular loop
     * @param format the format information for each segment.
     * @param loopId the loop being processed
     * @param segmentCounter counter that keeps track of the number of times each segment appears in the data
     * @return true if there are no new errors reported, false otherwise
     */
    private boolean validateSegments(List<String> segments, List<SegmentDefinition> format, String loopId, int[] segmentCounter, Separators separators) {
        int errorCountInitial = _errors.size();

        for (int i = 0; i < format.size(); i++) {
            SegmentDefinition segmentConf = format.get(i);
            if (!checkUsage(segmentConf.getUsage(), segmentCounter[i]) && !(segmentConf.getXid().equals("IEA") || segmentConf.getXid().equals("GE") || segmentConf
                    .getXid().equals("SE"))) {
                _errors.add(segmentConf.getXid() + " in loop " + loopId + " is required but not found");
            }
            if (!checkRepeats(segmentConf.getMaxUse(), segmentCounter[i])) {
                _errors.add(segmentConf.getXid() + " in loop " + loopId + " appears too many times");
            }
            for (String s : segments) {
                String[] tokens = s.split(Pattern.quote(separators.getElement().toString()));
                if (segmentCounter[i] > 0 && tokens[0].equals(segmentConf.getXid())) {
                    checkRequiredElements(tokens, segmentConf, loopId);
                    checkRequiredComposites(tokens, segmentConf, loopId);

                }
            }
        }

        return _errors.size() - errorCountInitial == 0;
    }

    /**
     * Checks to make sure all required segments in a loop appear at least once
     * @param usage status of whether a segment is required or not
     * @param count the number of times the segment appears in a loop
     * @return true if required segment does appear at least once, false if it does not.
     */
    private boolean checkUsage(Usage usage, Integer count) {
        return !(Usage.REQUIRED.equals(usage) && count <= 0);
    }

    /**
     * Check that the number of times a segment appears in a loop is less than the maximum number of allow repeats
     * @param repeats number of repeats allows
     * @param count the number of times the segment appears
     * @return true if the number of counts is less than or equal to the number of allow repeats, false otherwise
     */
    private boolean checkRepeats(String repeats, Integer count) {
        if ((repeats.equals(">1") && count <= 0))
            return false;
        else if (repeats.equals(">1"))
            return true;
        return count <= Integer.parseInt(repeats);
    }

    /**
     * Checks that all required elements are present in each data segment
     * @param tokens array of the data split on element separators
     * @param seg segment format information
     * @param loopId the loop we are testing segments from
     * @return true if all required elements are present, false otherwise
     */
    private boolean checkRequiredElements(String[] tokens, SegmentDefinition seg, String loopId) {
        for (int requiredPositions : getRequiredElementPositions(seg)) {
            if (requiredPositions >= tokens.length) {
                _errors.add(seg.getXid() + " in loop " + loopId + " element at position " + requiredPositions + " does not exist!!!!");
                return false;
            }
            if (tokens[requiredPositions].isEmpty()) {
                _errors.add(seg.getXid() + " in loop " + loopId + " is missing a required element at " + requiredPositions);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that all required composite elements are present in each data segment
     * @param tokens array of the data split on element separators
     * @param seg segment format information
     * @param loopId the loop we are testing segments from
     * @return true if all required composites are present, false otherwise
     */
    private boolean checkRequiredComposites(String[] tokens, SegmentDefinition seg, String loopId) {
        for (int requiredPositions : getRequiredCompositePositions(seg)) {
            if (requiredPositions >= tokens.length) {
                _errors.add(seg.getXid() + " in loop " + loopId + " composite element at position " + requiredPositions + " does not exist!!!!");
                return false;
            }
            if (tokens[requiredPositions].isEmpty()) {
                _errors.add(seg.getXid() + " in loop " + loopId + " is missing a required composite element at " + requiredPositions);
                return false;
            }
        }

        return true;
    }

    /**
     * check number of times each loop appears for loops that do have parents
     * @param count number of loop counts
     * @param repeatCondition maximum allowed repeats
     * @param parentId id of the parent loop
     * @return
     */
    private boolean compareRepeats(int count, String repeatCondition, String parentId) {
        //get the max usage of the parent loop
        int parentCount = 1;
        for (LoopConfig lc : _config) {
            if (lc.getLoopId().equals(parentId))
                parentCount = lc.getLoopRepeatCount();
        }

        return (repeatCondition.equals(">1") && count > 0) || (!repeatCondition.contains(">") && Math.ceil(((float)count) / parentCount) <= Integer.parseInt(repeatCondition));
    }

    /**
     * Gets the format infomration for each segment of a loop
     * @param loop loop that is being checked for the desired segment id
     * @param id id of the loop we want to get segment information for
     * @return the list of segment format information
     */
    private List<SegmentDefinition> getSegmentDefintions(LoopDefinition loop, String id) {
        List<SegmentDefinition> segs = new ArrayList<>();

        if (!loop.getXid().equals(id)) {
            if (loop.getLoop() != null)
                for (LoopDefinition subloop : loop.getLoop()) {
                    segs = getSegmentDefintions(subloop, id);
                    if (segs == null || segs.size() != 0)
                        break;
                }
        }
        else
            return loop.getSegment();

        return segs;
    }

    /**
     * Returns map of the valid codes and their positions for a given segment format
     * @param seg segment format we want valid code information for
     * @return map of codes and their positions
     */
    private Map<List<String>, Integer> getValidCodes(SegmentDefinition seg) {
        List<ElementDefinition> elems = seg.getElements();
        Map<List<String>, Integer> codePositionMap = new HashMap<>();

        if (elems != null) {
            for (ElementDefinition element : elems)
                if (element.getValidCodes() != null && element.getValidCodes().getCodes() != null)
                    codePositionMap.put(element.getValidCodes().getCodes(), Integer.parseInt(element.getSeq()));
        }

        return codePositionMap;
    }

    /**
     * Returns the positions that must have data in them in a segment
     * @param seg segmetn format we want to to know the required element positions for
     * @return list of positions that have required elements
     */
    private List<Integer> getRequiredElementPositions(SegmentDefinition seg) {
        List<ElementDefinition> elems = seg.getElements();
        List<Integer> requiredPositions = new ArrayList<>();

        if (elems != null) {
            for (ElementDefinition element : elems)
                if (Usage.REQUIRED.equals(element.getUsage()))
                    requiredPositions.add(Integer.parseInt(element.getSeq()));
        }

        return requiredPositions;
    }

    /**
     * Returns the positions that must have composite data in them in a segment
     * @param seg segmetn format we want to to know the required element composites for
     * @return list of positions that have required composites
     */
    private List<Integer> getRequiredCompositePositions(SegmentDefinition seg) {
        List<CompositeDefinition> composites = seg.getComposites();
        List<Integer> requiredPositions = new ArrayList<>();

        if (composites != null) {
            for (CompositeDefinition comps : composites)
                if (comps.getUsage().equals("R"))
                    requiredPositions.add(Integer.parseInt(comps.getSeq()));
        }

        return requiredPositions;
    }

}