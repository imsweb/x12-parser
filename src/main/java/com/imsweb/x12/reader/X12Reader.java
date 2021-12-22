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
import java.util.EnumMap;
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
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.WildcardTypePermission;

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

    private static final int _ISA_LENGTH = 106;
    private static final int _ELEMENT_SEPARATOR_POS = 3; // array position
    private static final int _COMPOSITE_SEPARATOR_POS = 104; // array position
    private static final int _SEGMENT_SEPARATOR_POS = 105; // array position

    private static final String _X091_ANSI_VERSION = "004010X091A1";
    private static final String _X221_ANSI_VERSION = "005010X221A1";
    private static final String _X096_ANSI_VERSION = "004010X096A1";
    private static final String _X097_ANSI_VERSION = "004010X097A1";
    private static final String _X098_ANSI_VERSION = "004010X098A1";
    private static final String _X222_ANSI_VERSION = "005010X222A1";
    private static final String _X223_ANSI_VERSION = "005010X223A2";
    private static final String _X231_ANSI_VERSION = "005010X231A1";
    private static final String _X214_ANSI_VERSION = "005010X214";
    private static final String _X270_271_092_ANSI_VERSION = "004010X092A1";
    private static final String _X212_ANSI_VERSION = "005010X212";
    private static final EnumMap<FileType, String> _TYPES = new EnumMap<>(FileType.class);

    private List<String> _errors = new ArrayList<>();
    private final List<String> _fatalErrors = new ArrayList<>(); // structure issues so bad we should stop processing.
    private final List<LoopConfig> _config = new ArrayList<>();
    private final List<Loop> _dataLoops = new ArrayList<>();
    private final Map<String, List<Set<String>>> _childLoopTracker = new HashMap<>();
    private Separators _separators;
    TransactionDefinition _definition;
    private final FileType _type;

    /**
     * All supported X12 file definitions
     */
    public enum FileType {
        ANSI835_5010_X221("mapping/835.5010.X221.A1.xml"),
        ANSI835_4010_X091("mapping/835.4010.X091.A1.xml"),
        ANSI837_4010_X096("mapping/837.4010.X096.A1.xml"),
        ANSI837_4010_X097("mapping/837.4010.X097.A1.xml"),
        ANSI837_4010_X098("mapping/837.4010.X098.A1.xml"),
        ANSI837_5010_X222("mapping/837.5010.X222.A1.xml"),
        ANSI837_5010_X223("mapping/837Q3.I.5010.X223.A1.xml"),
        ANSI837_5010_X231("mapping/999.5010.xml"),
        ANSI277_5010_X214("mapping/277.5010.X214.xml"),
        ANSI270_4010_X092("mapping/270.4010.X092.A1.xml"),
        ANSI271_4010_X092("mapping/271.4010.X092.A1.xml"),
        ANSI277_5010_X212("mapping/277.5010.X212.xml");

        private final String _mapping;

        private static final Map<String, TransactionDefinition> _DEFINITIONS = new HashMap<>();

        FileType(String mapping) {
            _mapping = mapping;
        }

        /**
         * Load definition from file
         * @return a TransactionDefinition
         */
        public synchronized TransactionDefinition getDefinition() {
            return _DEFINITIONS.computeIfAbsent(_mapping, k -> {
                XStream xstream = new XStream(new StaxDriver());
                xstream.autodetectAnnotations(true);
                xstream.alias("transaction", TransactionDefinition.class);

                // setup proper security by limiting what classes can be loaded by XStream
                xstream.addPermission(NoTypePermission.NONE);
                xstream.addPermission(new WildcardTypePermission(new String[] {"com.imsweb.x12.**"}));

                return (TransactionDefinition)xstream.fromXML(Thread.currentThread().getContextClassLoader().getResourceAsStream(_mapping));
            });
        }
    }

    static {
        _TYPES.put(FileType.ANSI835_4010_X091, _X091_ANSI_VERSION);
        _TYPES.put(FileType.ANSI837_4010_X096, _X096_ANSI_VERSION);
        _TYPES.put(FileType.ANSI837_4010_X097, _X097_ANSI_VERSION);
        _TYPES.put(FileType.ANSI837_4010_X098, _X098_ANSI_VERSION);
        _TYPES.put(FileType.ANSI835_5010_X221, _X221_ANSI_VERSION);
        _TYPES.put(FileType.ANSI837_5010_X222, _X222_ANSI_VERSION);
        _TYPES.put(FileType.ANSI837_5010_X223, _X223_ANSI_VERSION);
        _TYPES.put(FileType.ANSI277_5010_X214, _X214_ANSI_VERSION);
        _TYPES.put(FileType.ANSI837_5010_X231, _X231_ANSI_VERSION);
        _TYPES.put(FileType.ANSI270_4010_X092, _X270_271_092_ANSI_VERSION);
        _TYPES.put(FileType.ANSI271_4010_X092, _X270_271_092_ANSI_VERSION);
        _TYPES.put(FileType.ANSI277_5010_X212, _X212_ANSI_VERSION);
    }

    /**
     * Constructs an X12Reader using a File with default character encoding
     * @param type the type of x12 file
     * @param file a File object representing the input file
     * @throws IOException if there was an error reading the input file
     */
    public X12Reader(FileType type, File file) throws IOException {
        this._type = type;
        parse(new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset())));
    }

    /**
     * Constructs an X12Reader using a File
     * @param type the type of x12 file
     * @param file a File object representing the input file
     * @param charset character encoding
     * @throws IOException if there was an error reading the input file
     */
    public X12Reader(FileType type, File file, Charset charset) throws IOException {
        this._type = type;
        parse(new BufferedReader(new InputStreamReader(new FileInputStream(file), charset)));
    }

    /**
     * Constructs an X12Reader using an InputStream with default character encoding
     * @param type the type of x12 file
     * @param input an InputStream to an input file
     * @throws IOException if there was an error reading the input file
     */
    public X12Reader(FileType type, InputStream input) throws IOException {
        this._type = type;
        parse(new BufferedReader(new InputStreamReader(input, Charset.defaultCharset())));
    }

    /**
     * Constructs an X12Reader using an InputStream
     * @param type the type of x12 file
     * @param input an InputStream to an input file
     * @param charset character encoding
     * @throws IOException if there was an error reading the input file
     */
    public X12Reader(FileType type, InputStream input, Charset charset) throws IOException {
        this._type = type;
        parse(new BufferedReader(new InputStreamReader(input, charset)));
    }

    /**
     * Constructs an X12Reader using a Reader
     * @param type the type of x12 file
     * @param reader a Reader pointing to an input file
     * @throws IOException if there was an error reading the input file
     */
    public X12Reader(FileType type, Reader reader) throws IOException {
        this._type = type;
        // the Reader must support mark; if it does not, wrap the reader in a BufferedReader
        if (!reader.markSupported())
            parse(new BufferedReader(reader));
        else
            parse(reader);
    }

    public TransactionDefinition getDefinition() {
        return _definition;
    }

    /**
     * Return the resulting loops, this would possible if multiple ISA segments were included in one single file
     * @return the loop list
     */
    public List<Loop> getLoops() {
        return _dataLoops;
    }

    /**
     * Return the list of errors, if any
     * @return a list of errors
     */
    public List<String> getErrors() {
        return _errors;
    }

    public List<String> getFatalErrors() {
        return _fatalErrors;
    }

    public Separators getSeparators() {
        return _separators;
    }

    /**
     * Parse a Readable into a Loop
     * @param reader reader
     */
    private void parse(Reader reader) throws IOException {
        Scanner scanner = new Scanner(reader);

        // set up delimiters
        _separators = getSeparators(reader);

        if (_separators != null && checkVersionsAreConsistent(_separators, reader)) {
            Character segmentSeparator = _separators.getSegment();
            String quotedSegmentSeparator = Pattern.quote(segmentSeparator.toString());

            // The following delimiter patterns will accept the segment delimiter with
            // optional line breaks.
            scanner.useDelimiter(quotedSegmentSeparator + "\r\n|" + quotedSegmentSeparator + "\n|" + quotedSegmentSeparator);

            List<String> loopLines = new ArrayList<>(); // holds the lines from the claims files that all belong to the same loop
            LoopConfig loopConfig;
            LoopConfig currentLoopConfig = null;
            Loop lastLoopStored = null;

            // parse _definition file
            _definition = _type.getDefinition();

            // cache definitions of loop starting segments
            getLoopConfiguration(_definition.getLoop(), null);

            _errors = new ArrayList<>();

            String line = scanner.next().trim();
            while (scanner.hasNext()) {
                // Determine if we have started a new loop
                loopConfig = getMatchedLoop(_separators.splitElement(line), currentLoopConfig == null ? null : currentLoopConfig.getLoopId());
                if (loopConfig == null)
                    loopLines.add(line); // didn't start a new loop, just add the lines for the current loop
                else {
                    if (loopConfig.getLastSegmentXid() != null && line.startsWith(loopConfig.getLastSegmentXid().getXid()) && !loopConfig.equals(currentLoopConfig)) {
                        lastLoopStored = appendEndingSegment(lastLoopStored, currentLoopConfig, loopConfig, _separators, line, loopLines);
                        if (lastLoopStored != null) {
                            loopLines = new ArrayList<>();
                            currentLoopConfig = loopConfig;
                        }
                        else
                            break; // fatal error found when appending the segment
                    }
                    else if (loopConfig.getLoopId().equals(_definition.getLoop().getXid())) {
                        // we are processing a new transaction - store any old data if necessary
                        if (lastLoopStored != null && !loopLines.isEmpty()) {
                            if (storeData(currentLoopConfig, loopLines, lastLoopStored, _separators) == null)
                                break;
                            loopLines = new ArrayList<>();
                        }
                        currentLoopConfig = loopConfig;
                        lastLoopStored = null;
                        Loop loop = new Loop(null);
                        loop.setSeparators(_separators);
                        _dataLoops.add(loop);
                        loopLines.add(line);
                    }
                    else {
                        if (currentLoopConfig == null) {
                            _fatalErrors.add("Current loop is unknown. Bad structure detected");
                            break;
                        }
                        updateLoopCounts(loopConfig.getLoopId());
                        // store the data from processing the last loop
                        if (!loopLines.isEmpty())
                            lastLoopStored = storeData(currentLoopConfig, loopLines, lastLoopStored, _separators);

                        if (lastLoopStored == null)
                            break; // fatal error recorded during storing the loop

                        // start processing the new loop we found
                        loopLines = new ArrayList<>();
                        loopLines.add(line);
                        currentLoopConfig = loopConfig;
                    }
                }

                try {
                    line = scanner.next().trim();
                }
                catch (NoSuchElementException e) {
                    // break out of the loop, we have apparently hit the end of the file
                    break;
                }
            }

            // store the final segment if the last line of the file has data.
            if (!line.isEmpty() && _fatalErrors.isEmpty()) {
                if (currentLoopConfig != null) {
                    loopConfig = getMatchedLoop(_separators.splitElement(line), currentLoopConfig.getLoopId());
                    lastLoopStored = appendEndingSegment(lastLoopStored, currentLoopConfig, loopConfig, _separators, line, loopLines);
                    if (lastLoopStored == null || !_definition.getLoop().getXid().equals(lastLoopStored.getId()))
                        _fatalErrors.add("Unable to find end of transaction");
                }
                else
                    _fatalErrors.add("Last line of data and we don't know the current loop.");
            }
            if (_fatalErrors.isEmpty())
                checkLoopErrors();
        }
        else
            _fatalErrors.add("Unable to process transaction!");
    }

    /**
     * This is method is used for loops that don't have their segments grouped together in the transaction.
     * For example the ISA segment starts the ISA_LOOP, the IEA segment ends the ISA_LOOP.
     * The ISA segment is the first line in a transaction while the IEA is the last line the transaction
     */
    private Loop appendEndingSegment(Loop lastLoopStored, LoopConfig previousLoopConfig, LoopConfig currentLoopConfig, Separators separators, String currentLine, List<String> loopLines) {
        Loop lastLoopUpdated = null;
        // store any previous data
        if (!loopLines.isEmpty())
            lastLoopStored = storeData(previousLoopConfig, loopLines, lastLoopStored, separators);

        if (lastLoopStored != null) {
            Segment segment = new Segment(separators);
            segment.addElements(currentLine);
            lastLoopUpdated = lastLoopStored.findTopParentById(currentLoopConfig.getLoopId());
            if (lastLoopUpdated != null)
                lastLoopUpdated.addSegment(segment);
            else {
                _fatalErrors.add("We found an ending segment but we never stored the first part of the loop!");
            }
        }
        return lastLoopUpdated;
    }

    private void checkLoopErrors() {
        // check overall loop structure
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

            // check internal loop data
            Set<String> requiredChildLoops = new HashSet<>();
            requiredChildLoops = getRequiredChildLoops(_definition.getLoop(), lc.getLoopId(), requiredChildLoops);
            if (_childLoopTracker.get(lc.getLoopId()) != null) {
                for (int i = 0; i < _childLoopTracker.get(lc.getLoopId()).size(); i++) {
                    Set<String> childLoops = _childLoopTracker.get(lc.getLoopId()).get(i);
                    for (String ids : requiredChildLoops)
                        if (!childLoops.contains(ids))
                            _errors.add(ids + " is required but not found in " + lc.getLoopId() + " iteration #" + (i + 1));
                }
            }
        }
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

    private boolean checkVersionsAreConsistent(Separators separators, Reader reader) throws IOException {
        if (reader == null || separators == null || _type == null)
            return false;

        char segmentSeparator = separators.getSegment();
        char elementSeparator = separators.getElement();
        int c;
        StringBuilder line = new StringBuilder();

        while ((c = reader.read()) != -1 && c != segmentSeparator)
            line.append((char)c);

        // The version is the last element
        // If we got to the end of the file before the end of the line, don't get the version
        String version = null;
        if (c == segmentSeparator) {
            String lineString = line.toString();
            int versionStartPos = lineString.lastIndexOf(elementSeparator);
            if (versionStartPos != -1)
                version = lineString.substring(versionStartPos + 1);
        }
        reader.reset();

        boolean result = _TYPES.get(_type).equals(version);

        if (!result)
            _errors.add("ANSI version " + version + " not consistent with version specified " + _type);

        return result;
    }

    /**
     * Determines the characters for each separator used in an x12 file
     * @param reader that is used to read the x12 file
     * @return Separator object instantiated with the appropriate separators.
     * @throws IOException exception with Reader
     */
    private Separators getSeparators(Reader reader) throws IOException {
        reader.mark(1);
        char[] firstLine = new char[_ISA_LENGTH];
        int ret = reader.read(firstLine);
        boolean isAlphaNumeric = Character.isDigit(firstLine[_SEGMENT_SEPARATOR_POS]) ||
                Character.isDigit(firstLine[_ELEMENT_SEPARATOR_POS]) ||
                Character.isDigit(firstLine[_COMPOSITE_SEPARATOR_POS]) ||
                Character.isLetter(firstLine[_SEGMENT_SEPARATOR_POS]) ||
                Character.isLetter(firstLine[_ELEMENT_SEPARATOR_POS]) ||
                Character.isLetter(firstLine[_COMPOSITE_SEPARATOR_POS]);

        boolean isWhiteSpace = Character.isWhitespace(firstLine[_SEGMENT_SEPARATOR_POS]) ||
                Character.isWhitespace(firstLine[_ELEMENT_SEPARATOR_POS]) ||
                Character.isWhitespace(firstLine[_COMPOSITE_SEPARATOR_POS]);
        if (ret != _ISA_LENGTH || (isAlphaNumeric || isWhiteSpace)) {
            _errors.add("Error getting separators");
            return null;
        }
        // don't need to reset the reader---we need to check the version on the next line
        return new Separators(firstLine[_SEGMENT_SEPARATOR_POS],
                firstLine[_ELEMENT_SEPARATOR_POS],
                firstLine[_COMPOSITE_SEPARATOR_POS]);
    }

    /**
     * Stores data, one loop at a time, into the _dataLoop structure.
     * @param currentLoopConfig---the loopID of the current loop
     * @param loopLines---the data file segments that belong to this loop
     * @param lastLoopStored---the previous loop id that was stored
     * @return loopID----the id of the loop that was just stored
     */
    private Loop storeData(LoopConfig currentLoopConfig, List<String> loopLines, Loop lastLoopStored, Separators separators) {
        // validate the individual segments
        validateLines(loopLines, currentLoopConfig.getLoopId(), separators);

        // create the segments that will be stored
        List<Segment> segments = new ArrayList<>();
        for (String s : loopLines) {
            Segment seg = new Segment(separators);
            seg.addElements(s);
            segments.add(seg);
        }

        Loop newLoop = null;
        if (lastLoopStored == null) {
            // we haven't stored any loops so this is the start of the transaction
            Loop topLoop = _dataLoops.get(_dataLoops.size() - 1);
            topLoop.setId(currentLoopConfig.getLoopId());
            topLoop.setSeparators(separators);
            segments.forEach(topLoop::addSegment);
            newLoop = topLoop;
            _childLoopTracker.put(newLoop.getId(), new ArrayList<>());
            _childLoopTracker.get(newLoop.getId()).add(new HashSet<>());
        }
        else {
            // find the parent loop and add the new loop we are storing to that.
            Loop parentLoop = findParentLoop(currentLoopConfig, lastLoopStored);

            // if the parent loop was not found above - it could be that the parent loop is a segmentless loop
            // need to confirm that and add the segmentless loop and then add the current loop to that
            if (parentLoop == null) {
                LoopConfig parentLoopInfo = null;

                String parentLoopId = currentLoopConfig.getParentLoop();
                for (LoopConfig lc : _config) {
                    if (lc.getLoopId() != null && lc.getLoopId().equals(parentLoopId)) {
                        parentLoopInfo = lc;
                        break;
                    }
                }
                if (parentLoopInfo == null)
                    _fatalErrors.add("Parent loop " + parentLoopId + " does not exist in loop configuration!");
                else if (parentLoopInfo.hasDataSegments())
                    _fatalErrors.add("Parent loop " + parentLoopId + " is missing and should already exist");
                else {
                    parentLoop = lastLoopStored.findTopParentById(parentLoopInfo.getParentLoop());
                    if (parentLoop == null)
                        _fatalErrors.add("Parent loop of " + parentLoopId + " is not found!");
                    else {
                        newLoop = new Loop(separators, parentLoopInfo.getLoopId());

                        Loop currentLoop = new Loop(separators, currentLoopConfig.getLoopId());
                        segments.forEach(currentLoop::addSegment);

                        newLoop.addLoop(0, currentLoop);
                    }
                }
            }
            else {
                newLoop = new Loop(separators, currentLoopConfig.getLoopId());
                segments.forEach(newLoop::addSegment);
            }

            if (parentLoop != null) {
                parentLoop.addLoop(parentLoop.getLoops().size(), newLoop);

                // if we had to add the segment less parent make sure we return the current child loop as the last loop added.
                if (!newLoop.getId().equals(currentLoopConfig.getLoopId())) {
                    // add segmentless loop to its parent list and create new lists for that loop
                    updateChildLoopTracker(parentLoop.getId(), newLoop.getId());
                    // add loop with segments that is a child of the segmentless loop to the segmentless loop's list, create lists for this child loop
                    updateChildLoopTracker(newLoop.getId(), newLoop.getLoop(0).getId());
                    newLoop = newLoop.getLoop(0);
                }
                else
                    updateChildLoopTracker(parentLoop.getId(), newLoop.getId());

            }

            // final safety check
            if (parentLoop == null && _fatalErrors.isEmpty())
                _fatalErrors.add("Something is wrong. Check loop structure.");
        }

        if (newLoop == null)
            _fatalErrors.add("Failed to store loop data for " + currentLoopConfig.getLoopId());

        return newLoop;
    }

    private void updateChildLoopTracker(String parentLoopId, String newLoopId) {
        _childLoopTracker.get(parentLoopId).get(_childLoopTracker.get(parentLoopId).isEmpty() ? 0 : _childLoopTracker.get(parentLoopId).size() - 1).add(newLoopId);

        if (!_childLoopTracker.containsKey(newLoopId)) {
            _childLoopTracker.put(newLoopId, new ArrayList<>());
            _childLoopTracker.get(newLoopId).add(new HashSet<>());
        }
        else
            _childLoopTracker.get(newLoopId).add(new HashSet<>());
    }

    private Loop findParentLoop(LoopConfig currentLoopConfig, Loop lastLoopStored) {
        Loop result;

        Set<String> parentLoopIds = new HashSet<>(getParentLoopsFromDefinition(_definition.getLoop(), currentLoopConfig.getLoopId(), new ArrayList<>()));

        if (parentLoopIds.isEmpty())
            result = lastLoopStored;
        else if (parentLoopIds.size() == 1)
            result = lastLoopStored.getId().equals(currentLoopConfig.getParentLoop()) ? lastLoopStored : lastLoopStored.findTopParentById(currentLoopConfig.getParentLoop());
        else {
            // dealing with ambiguous parent loop
            result = lastLoopStored;
            while (!parentLoopIds.contains(result.getId())) {
                if (result.getParent() == null) {
                    result = null;
                    break;
                }
                result = result.getParent();
            }
        }

        return result;
    }

    /**
     * Stores some data from the XML definition into a loop configuration object
     * @param loop loop to be proceesed
     * @param parentID parent loop id of the loop being processed
     */
    private void getLoopConfiguration(LoopDefinition loop, String parentID) {
        if (!containsLoop(loop.getXid())) {
            if (loop.getLoop() != null) {
                LoopConfig loopConfig = new LoopConfig(loop.getXid(), parentID, getChildLoops(loop), loop.getRepeat(), loop.getUsage(), loop.getSegment() != null);
                if (loop.getSegment() != null) {
                    loopConfig.setFirstSegmentXid(loop.getSegment().get(0));

                    if (loop.getSegment().size() > 1)
                        loopConfig.setLastSegmentXid(loop.getSegment().get(loop.getSegment().size() - 1));
                }

                parentID = loop.getXid();
                _config.add(loopConfig);
                for (LoopDefinition loops : loop.getLoop())
                    getLoopConfiguration(loops, parentID);
            }
            else {
                LoopConfig loopConfig = new LoopConfig(loop.getXid(), parentID, null, loop.getRepeat(), loop.getUsage(), loop.getSegment() != null);
                if (loop.getSegment() != null)
                    loopConfig.setFirstSegmentXid(loop.getSegment().get(0));

                if (loop.getSegment() != null && loop.getSegment().size() > 1)
                    loopConfig.setLastSegmentXid(loop.getSegment().get(loop.getSegment().size() - 1));

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

        parentLoops = getParentLoopsFromDefinition(_definition.getLoop(), loopId, parentLoops);
        previousParentLoops = getParentLoopsFromDefinition(_definition.getLoop(), previousLoopId, previousParentLoops);

        if (previousLoopId != null)
            for (String parentLoop : parentLoops)
                if (previousParentLoops.contains(parentLoop))
                    return parentLoop;
        if (!parentLoops.isEmpty())
            return parentLoops.get(0);
        else
            return null;
    }

    /**
     * Gets all possible parent loops from the definition object. Need a list since it is possible for one loop to have two different parents.
     * @param loop ---- object to loop over
     * @param id --- the id of the loop we want to find parents for
     * @param parentLoop --- list of parent loops.
     * @return list of parent loops
     */
    private List<String> getParentLoopsFromDefinition(LoopDefinition loop, String id, List<String> parentLoop) {
        if (loop.getLoop() != null)
            for (LoopDefinition subloop : loop.getLoop()) {
                if (subloop.getXid().equals(id)) {
                    parentLoop.add(loop.getXid());
                }
                parentLoop = getParentLoopsFromDefinition(subloop, id, parentLoop);
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
    private LoopConfig getMatchedLoop(String[] tokens, String previousLoopID) {
        LoopConfig result = null;
        if (tokens != null) {
            List<LoopConfig> matchedLoops = new ArrayList<>();
            for (LoopConfig config : _config) {
                SegmentDefinition firstId = config.getFirstSegmentXid();
                boolean firstIdCheck = firstId != null && tokens[0].equals(firstId.getXid()) && codesValidatedForLoopId(tokens, firstId);
                SegmentDefinition lastId = config.getLastSegmentXid();
                boolean lastIdCheck = lastId != null && tokens[0].equals(lastId.getXid()) && !config.getLoopId().equals(previousLoopID) && codesValidatedForLoopId(tokens, lastId);
                if (firstIdCheck || lastIdCheck) {

                    // clear the potential loop matches if the current segment is a child loop of the loop currently being processed
                    if (isChildSegment(previousLoopID, tokens)) {
                        matchedLoops.clear();
                        break;
                    }

                    if (!matchedLoops.stream().map(LoopConfig::getLoopId).collect(Collectors.toList()).contains(config.getLoopId()))
                        matchedLoops.add(config);
                }
            }

            if (matchedLoops.size() > 1) {
                // starting a new loop but we aren't quite sure which one yet. Remove loops where the segment is known to be the last segment of that loop - clearly we aren't in a new loop then
                matchedLoops = matchedLoops.stream().filter(lc -> lc.getLastSegmentXid() == null || !(lc.getLastSegmentXid().getXid().equals(tokens[0]) && codesValidatedForLoopId(tokens,
                        lc.getLastSegmentXid()))).collect(
                        Collectors.toList());
                result = matchedLoops.isEmpty() ? null : (matchedLoops.size() == 1 ? matchedLoops.get(0) : getFinalizedMatch(previousLoopID, matchedLoops));
            }
            else if (matchedLoops.size() == 1)
                result = matchedLoops.get(0);
        }
        else
            _errors.add("Unable to split elements for loop matching!");

        return result;
    }

    /**
     * Check if the current line is a child segment of the current loop. if it is then we should assume we are not starting new loop.
     * @param previousLoopId the previous loop that was matched
     * @param tokens array of the data split on element separators
     * @return true if it is a child segment, false if it is not.
     */
    private boolean isChildSegment(String previousLoopId, String[] tokens) {

        List<SegmentDefinition> loopSegs = getSegmentDefinitions(_definition.getLoop(), previousLoopId);
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
     * @param matchedLoops list of loop ids that match
     * @return the finalized loop match
     */
    private LoopConfig getFinalizedMatch(String previousLoopId, List<LoopConfig> matchedLoops) {
        LoopConfig result = null;
        for (LoopConfig lc : _config) {
            if (lc.getLoopId().equals(previousLoopId)) {
                // if one of the ambiguous loops is a child loop of the previous loop then we should use that one
                if (lc.getChildList() != null)
                    result = matchedLoops.stream().filter(matchedLoop -> lc.getChildList().contains(matchedLoop.getLoopId())).findFirst().orElse(null);

                // otherwise, if one of the ambiguous loops has the same parent as the previous loop's parent then we should use that loop
                if (result == null) {
                    String parentLoop = getParentLoop(previousLoopId, null);
                    if (parentLoop != null)
                        result = matchedLoops.stream().filter(matchedLoop -> parentLoop.equals(getParentLoop(matchedLoop.getLoopId(), null))).findFirst().orElse(null);
                }
                break;
            }
        }
        return result;
    }

    /**
     * Validates the segments for a single loop
     * @param segments list of segments from the data file that belong to a specific loop
     * @param loopId loop id we are validating segments for
     * @return boolean indicating validation success
     */
    private boolean validateLines(List<String> segments, String loopId, Separators separators) {
        List<SegmentDefinition> format = getSegmentDefinitions(_definition.getLoop(), loopId);
        int[] segmentCounter = new int[format.size()];
        boolean lineMatchesFormat = false;

        String previousPos = null;
        for (String segment : segments) {
            int i = 0;
            for (SegmentDefinition segmentConf : format) {
                String[] tokens = separators.splitElement(segment);
                if (tokens != null && tokens[0].equals(segmentConf.getXid()) && codesValidated(tokens, segmentConf)) {
                    String currentPos = segmentConf.getPos();
                    if (previousPos != null && Integer.parseInt(previousPos) > Integer.parseInt(currentPos))
                        _errors.add("Segment " + segmentConf.getXid() + " in loop " + loopId + " is not in the correct position.");

                    segmentCounter[i]++;
                    lineMatchesFormat = true;
                    previousPos = currentPos;
                    break;
                }
                else if (tokens == null)
                    _errors.add("Unable to split elements to validate segment ID!");
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
        List<Integer> positions = new ArrayList<>(validCodes.values());
        List<List<String>> codes = new ArrayList<>(validCodes.keySet());

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
        List<Integer> positions = new ArrayList<>(validCodes.values());
        List<List<String>> codes = new ArrayList<>(validCodes.keySet());

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
            if (!checkUsage(segmentConf.getUsage(), segmentCounter[i]) && !(segmentConf.getXid().equals("IEA") || segmentConf.getXid().equals("GE") || segmentConf.getXid().equals("SE")))
                _errors.add(segmentConf.getXid() + " in loop " + loopId + " is required but not found");
            if (!checkRepeats(segmentConf.getMaxUse(), segmentCounter[i]))
                _errors.add(segmentConf.getXid() + " in loop " + loopId + " appears too many times");
            for (String s : segments) {
                String[] tokens = separators.splitElement(s);
                if (tokens != null && segmentCounter[i] > 0 && tokens[0].equals(segmentConf.getXid())) {
                    checkRequiredElements(tokens, segmentConf, loopId);
                    checkRequiredComposites(tokens, segmentConf, loopId);
                }
                else if (tokens == null)
                    _errors.add("Unable to split elements for validation!");
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
     * @return false if loop repeat too many times
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
    private List<SegmentDefinition> getSegmentDefinitions(LoopDefinition loop, String id) {
        List<SegmentDefinition> segs = new ArrayList<>();

        if (!loop.getXid().equals(id)) {
            if (loop.getLoop() != null)
                for (LoopDefinition subloop : loop.getLoop()) {
                    segs = getSegmentDefinitions(subloop, id);
                    if (segs == null || !segs.isEmpty())
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
     * @param seg segment format we want to to know the required element composites for
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
