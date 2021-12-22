package com.imsweb.x12;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.AbstractJsonWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.WildcardTypePermission;

import com.imsweb.x12.converters.ElementConverter;
import com.imsweb.x12.mapping.LoopDefinition;
import com.imsweb.x12.mapping.Positioned;
import com.imsweb.x12.mapping.SegmentDefinition;

/**
 * The Loop class is the representation of an Loop in a ANSI X12 transaction. The building block of an X12 transaction is an element. Some
 * elements may be made of sub elements. Elements combine to form segments.  Segments are grouped as loops. And a set of loops form an X12
 * transaction.
 */
@XStreamAlias("loop")
public class Loop implements Iterable<Segment> {

    @XStreamOmitField
    private Separators _separators;
    @XStreamAlias("id")
    private String _id;
    @XStreamAlias("segments")
    private List<Segment> _segments = new ArrayList<>();
    @XStreamAlias("loops")
    private List<Loop> _loops = new ArrayList<>();
    @XStreamOmitField
    private Loop _parent;

    public Loop() {
        _separators = new Separators();
        _id = null;
        _parent = null;
    }

    /**
     * The constructor uses the default set of separators
     * @param id loop identifier
     */
    public Loop(String id) {
        _separators = new Separators();
        _id = id;
        _parent = null;
    }

    /**
     * The constructor takes a separators object.
     * @param separators a Separators object
     * @param id loop identifier
     */
    public Loop(Separators separators, String id) {
        _separators = separators;
        _id = id;
        _parent = null;
    }

    /**
     * Returns the id of the current Loop.
     * @return String
     */
    public String getId() {
        return _id;
    }

    /**
     * Sets the id of the current Loop
     * @param id <code>String</code>
     */
    public void setId(String id) {
        _id = id;
    }

    /**
     * @return Parent Loop
     */
    public Loop getParent() {
        return _parent;
    }

    /**
     * @param parent Parent Loop
     */
    public void setParent(Loop parent) {
        _parent = parent;
    }

    /**
     * Returns the separators of the X12 transaction.
     * @return Separators object
     */

    public Separators getSeparators() {
        return _separators;
    }

    /**
     * Sets the separators of the current transaction.
     * @param separators Separators object
     */
    public void setSeparators(Separators separators) {
        _separators = separators;
    }

    /**
     * Returns the segments in the current loop.
     * @return List of Segment entities
     */
    public List<Segment> getSegments() {
        return _segments;
    }

    /**
     * Sets the full list of segments
     * @param segments List of Segment entities
     */
    public void setSegments(List<Segment> segments) {
        _segments = segments;
    }

    /**
     * Returns the loops
     * @return List of Loop entities
     */
    public List<Loop> getLoops() {
        return _loops;
    }

    /**
     * Set the full list of loops
     * @param loops List of Loop entities
     */
    public void setLoops(List<Loop> loops) {
        _loops = loops;
    }

    /**
     * Creates an empty instance of <code>Loop</code> and adds the loop as a
     * child to the current Loop. The returned instance can be used to add
     * segments to the child loop.
     * @param id id of the loop
     * @return a new child Loop object
     */
    public Loop addLoop(String id) {
        Loop l = new Loop(_separators, id);

        l.setParent(this);
        _loops.add(l);

        return l;
    }

    /**
     * Creates an empty instance of <code>Loop</code> and inserts the loop as a
     * child loop at the specified position. The returned instance can be used
     * to add _segments to the child loop.
     * @param index position at which to add the loop
     * @param id _id of the loop
     * @return a new child Loop object
     */
    public Loop addLoop(int index, String id) {
        Loop loop = new Loop(_separators, id);
        addLoop(index, loop);

        return loop;
    }

    /**
     * Inserts supplied Loop as a child loop at the specified position.
     * @param index position at which to add the loop.
     * @param loop Loop
     */
    public void addLoop(int index, Loop loop) {
        loop.setParent(this);
        _loops.add(index, loop);
    }

    /**
     * Creates an empty instance of <code>Segment</code> and adds the segment to
     * current Loop. The returned instance can be used to add elements to the
     * segment.
     * @return a new Segment object
     */
    public Segment addSegment() {
        Segment s = new Segment(_separators);
        _segments.add(s);

        return s;
    }

    /**
     * Takes a <code>String</code> representation of segment, creates a
     * <code>Segment</code> object and adds the segment to the current Loop.
     * @param segment <code>String</code> representation of the Segment.
     * @return a new Segment object
     */
    public Segment addSegment(String segment) {
        Segment s = new Segment(_separators);
        s.addElements(segment);
        _segments.add(s);

        return s;
    }

    /**
     * Adds <code>Segment</code> at the end of the current Loop
     * @param segment <code>Segment</code>
     */
    public void addSegment(Segment segment) {
        _segments.add(segment);
    }

    /**
     * Takes a <code>String</code> representation of segment, creates a
     * <code>Segment</code> object and adds the segment at the specified
     * position in the current Loop.
     * @param index position to add the segment.
     * @param segmentText <code>String</code> representation of the segment.
     * @return a new Segment object
     */
    public Segment addSegment(int index, String segmentText) {
        Segment segment = new Segment(_separators);
        segment.addElements(segmentText);

        addSegment(index, segment);

        return segment;
    }

    /**
     * Adds <code>Segment</code> at the specified position in current Loop.
     * @param index position to add the segment.
     * @param segment <code>String</code> representation of the segment.
     */
    public void addSegment(int index, Segment segment) {
        _segments.add(index, segment);
    }

    /**
     * Checks if the Loop contains the specified child Loop. It will check the
     * complete child hierarchy.
     * @param id id of a child loop
     * @return boolean
     */
    public boolean hasLoop(String id) {
        if (getId().contains(id))
            return true;
        for (Loop l : getLoops()) {
            if (id.equals(l.getId()))
                return true;
            if (l.hasLoop(id))
                return true;
        }

        return false;
    }

    /**
     * Returns the loop with the first occurrence of the loopId
     * @param loopId is the loop we are searching for
     * @return loop with corresponding loopId. Returns null if there no loops matching the loop ID.
     */
    public Loop getLoop(String loopId) {
        List<Loop> loops = findLoop(loopId);
        if (!loops.isEmpty())
            return findLoop(loopId).get(0);
        return null;
    }

    /**
     * Returns the child loop at the specified index of the loop with the corresponding loopID.
     * @param loopId is the loop we are searching for
     * @param index is the child loop index we want.
     * @return loop with corresponding loopId. Returns null if the index is greater than or equal to the number of loops matching the loop ID.
     */
    public Loop getLoop(String loopId, int index) {
        List<Loop> loops = findLoop(loopId);
        if (index < loops.size())
            return findLoop(loopId).get(index);
        return null;
    }

    /**
     * Get the loop in the X12 transaction It will check the complete child
     * hierarchy.
     * @param id id of a loop
     * @return List of Loop entities
     */
    public List<Loop> findLoop(String id) {
        List<Loop> foundLoops = new ArrayList<>();

        for (Loop loop : getLoops()) {
            if (id.equals(loop.getId()))
                foundLoops.add(loop);

            List<Loop> moreLoops = loop.findLoop(id);
            if (!moreLoops.isEmpty())
                foundLoops.addAll(moreLoops);
        }

        return foundLoops;
    }

    public List<Loop> findAllLoops(String id) {
        List<Loop> foundLoops = new ArrayList<>();

        if (_id != null && _id.equals(id))
            foundLoops.add(this);

        foundLoops.addAll(findLoop(id));

        return foundLoops;
    }

    /**
     * Get the segment in the X12 transaction It will check the current loop.
     * @param id id of a segment
     * @return List of Segment entities
     */
    public List<Segment> findSegment(String id) {
        return _segments.stream().filter(segment -> id.equals(segment.getId())).collect(Collectors.toList());
    }

    /**
     * Returns the Loop at the specified position.
     * @param index index of loop
     * @return Loop at the specified index. Returns null if the index is greater than or equal to the size of the loop list.
     */
    public Loop getLoop(int index) {
        if (index < _loops.size())
            return _loops.get(index);
        return null;

    }

    /**
     * Returns the Segment at the specified position.
     * @param index index of the Segment
     * @return Segment at the specified index. Returns null if the index is greater than or equal to the size of the segment list.
     */
    public Segment getSegment(int index) {
        if (index < _segments.size())
            return _segments.get(index);
        return null;

    }

    /**
     * Finds the segment with the first occurrence of the id and returns it
     * @param id of the segment to find.
     * @return the first occurrence of a loop with that segement ID. Returns null if the number of segments matching that id is zero.
     */
    public Segment getSegment(String id) {
        List<Segment> segs = findSegment(id);
        if (!segs.isEmpty())
            return findSegment(id).get(0);
        return null;
    }

    /**
     * Finds all elements with the segment id and returns the specified index
     * @param id to search for
     * @param index to return for that id
     * @return Returns a segment with they correspoding segement ID. Returns null if the number of segments matching that id is less than the index requested.
     */
    public Segment getSegment(String id, int index) {
        List<Segment> segs = findSegment(id);
        if (index < segs.size())
            return findSegment(id).get(index);
        return null;
    }

    /**
     * Returns and Iterator to the segments in the loop.
     * @return Iterator to the segments in the loop
     */
    @Override
    public Iterator<Segment> iterator() {
        return _segments.iterator();
    }

    /**
     * Removes the loop at the specified position in this list.
     * @param index loop index
     * @return Loop
     */
    public Loop removeLoop(int index) {
        return _loops.remove(index);
    }

    /**
     * Removes the segment at the specified position in this list.
     * @param index segment index
     * @return Segment
     */
    public Segment removeSegment(int index) {
        return _segments.remove(index);
    }

    /**
     * Returns number of segments in Loop and child loops
     * @return number of segments in loop and child loops
     */
    public int size() {
        int size = _segments.size();

        for (Loop loops : getLoops())
            size += loops.size();

        return size;
    }

    /**
     * Creates a new Loop and replaces the child loop at the
     * specified position. The returned instance can be used to add segments to
     * the child loop.
     * @param id id of the loop
     * @param index position at which to add the loop.
     * @return a new child Loop object
     */
    public Loop setLoop(int index, String id) {
        Loop loop = new Loop(_separators, id);

        loop.setParent(this);
        _loops.set(index, loop);

        return loop;
    }

    /**
     * Replaces child Loop at the specified position.
     * @param index position at which to add the loop.
     * @param loop Loop to add
     */
    public void setLoop(int index, Loop loop) {
        loop.setParent(this);
        _loops.set(index, loop);
    }

    /**
     * Takes a String representation of segment, creates a
     * Segment object and replaces the segment at the specified
     * position in the X12 transaction.
     * @param index position of the segment to be replaced.
     * @param segmentText String representation of the Segment.
     * @return a new Segment object
     */
    public Segment setSegment(int index, String segmentText) {
        Segment segment = new Segment(_separators);
        segment.addElements(segmentText);
        _segments.set(index, segment);

        return segment;
    }

    /**
     * Replaces
     * Segment at the specified position in X12 transaction.
     * @param index position of the segment to be replaced.
     * @param segment Segment
     */
    public void setSegment(int index, Segment segment) {
        _segments.set(index, segment);
    }

    /**
     * Gets the element at the location specified by the loop, segment and element ids. Loop and segment indices can be specified
     * in case multiple instances occur.
     * @param loopId the loop id to be searched for
     * @param loopIndex the index to of the loop to be searched
     * @param segmentId the segment to be searched for
     * @param segIndex the index of the segment to be searched for
     * @param elementId element id of the element that is desired.
     * @return a string showing the value of the element requested. If the loop, segment or element specified do not exist, null is returned
     */
    public String getElement(String loopId, int loopIndex, String segmentId, int segIndex, String elementId) {
        Loop requestedLoop = getLoop(loopId, loopIndex);
        if (requestedLoop == null)
            return null;

        Segment requestedSegment = requestedLoop.getSegment(segmentId, segIndex);

        if (requestedSegment == null)
            return null;

        return requestedSegment.getElementValue(elementId);

    }

    /**
     * Gets the element at the location specified by the loop, segment and element ids.
     * @param loopId the loop id to be searched for
     * @param segmentId the segment to be searched for
     * @param elementId element id of the element that is desired.
     * @return a string showing the value of the element requested. If the loop, segment or element specified do not exist, null is returned
     */
    public String getElement(String loopId, String segmentId, String elementId) {
        Loop requestedLoop = getLoop(loopId);
        if (requestedLoop == null)
            return null;

        Segment requestedSegment = requestedLoop.getSegment(segmentId);

        if (requestedSegment == null)
            return null;

        return requestedSegment.getElementValue(elementId);

    }

    /**
     * Gets the element at the location specified by the segment and element ids. Segment index can be speficified in case multiple instances occur.
     * @param segmentId the segment to be searched for
     * @param segIndex the index of the segment to be searched for
     * @param elementId element id of the element that is desired.
     * @return a string showing the value of the element requested. If the Segment or element specified do not exist, null is returned
     */
    public String getElement(String segmentId, int segIndex, String elementId) {
        Segment requestedSegment = getSegment(segmentId, segIndex);

        if (requestedSegment == null)
            return null;

        return requestedSegment.getElementValue(elementId);

    }

    /**
     * Gets the element at the location specified by the segment and element ids. Segment index can be speficified in case multiple instances occur.
     * @param segmentId the segment to be searched for
     * @param elementId element id of the element that is desired.
     * @return a string showing the value of the element requested. If the Segment or element specified do not exist, null is returned
     */
    public String getElement(String segmentId, String elementId) {
        Segment requestedSegment = getSegment(segmentId);

        if (requestedSegment == null)
            return null;

        return requestedSegment.getElementValue(elementId);

    }

    public Loop findTopParentById(String parentId) {
        Loop result = null;
        if (parentId != null) {
            Loop parentLoop = _parent;
            while (parentLoop != null && !parentId.equals(parentLoop.getId()))
                parentLoop = parentLoop.getParent();

            if (parentLoop != null && parentId.equals(parentLoop.getId()))
                result = parentLoop;
        }
        return result;
    }

    /**
     * Returns an X12 String for this loop, but it will not be
     * properly ordered. For properly ordered X12 string use toX12String.
     *
     * @return String representation
     */
    @Override
    public String toString() {
        StringBuilder dump = new StringBuilder();

        for (Segment segment : getSegments()) {
            dump.append(segment.toString());
            dump.append(_separators.getSegment());
        }
        for (Loop loop : getLoops())
            dump.append(loop.toString());

        return dump.toString();
    }

    /**
     * Returns the Loop in X12 String format. This method is used to convert the X12 object into a X12 transaction.
     * 
     * This will first go through each segment and will return the properly separated string for the segment.
     * 
     * After the segments are seriaized to X12 strings, it will then go through the Loops (in the correct order) and
     * recursively call this function for the child loops.
     * 
     * @param loopDefinition The definition of the loop that we are currently on.
     * @return String representation The segments from this loop, including child loops.
     */
    public String toX12String(LoopDefinition loopDefinition) {
        StringBuilder dump = new StringBuilder();

        Set<Positioned> segmentsAndLoops = new TreeSet<>();
        if (loopDefinition.getLoop() != null) {
            segmentsAndLoops.addAll(loopDefinition.getLoop());
        }
        if (loopDefinition.getSegment() != null) {
            segmentsAndLoops.addAll(loopDefinition.getSegment());
        }
        for (Positioned positioned : segmentsAndLoops) {
            if (positioned instanceof SegmentDefinition) {
                SegmentDefinition segmentDefinition = (SegmentDefinition)positioned;
                int idx = 0;
                Segment segment;
                while ((segment = getSegment(segmentDefinition.getXid(), idx++)) != null) {
                    dump.append(segment);
                    dump.append(_separators.getSegment());
                    dump.append(_separators.getLineBreak().getLineBreakString());
                }
            }
            else if (positioned instanceof LoopDefinition) {
                LoopDefinition innerLoopDefinition = (LoopDefinition)positioned;
                int idx = 0;
                Loop innerLoop;
                while ((innerLoop = getLoopForPrinting(innerLoopDefinition, idx++)) != null) {
                    dump.append(innerLoop.toX12String(innerLoopDefinition));
                }
            }
        }
        return dump.toString();
    }

    public String toHtml(LoopDefinition loopDefinition, List<String> parentIds) {
        StringBuilder dump = new StringBuilder();
        dump.append("<div id=\"")
            .append(Separators.getIdString(parentIds))
            .append("\" class=\"x12-loop\"><p>");
        dump.append(loopDefinition.getName())
            .append(" (")
            .append(loopDefinition.getXid())
            .append(")</p>");

        ArrayList<String> newParentIds = new ArrayList<>();
        newParentIds.addAll(parentIds);
        newParentIds.add(getId());

        Set<Positioned> segmentsAndLoops = new TreeSet<>();
        if (loopDefinition.getLoop() != null) {
            segmentsAndLoops.addAll(loopDefinition.getLoop());
        }
        if (loopDefinition.getSegment() != null) {
            segmentsAndLoops.addAll(loopDefinition.getSegment());
        }
        for (Positioned positioned : segmentsAndLoops) {
            if (positioned instanceof SegmentDefinition) {
                SegmentDefinition segmentDefinition = (SegmentDefinition)positioned;
                int idx = 0;
                Segment segment;
                while ((segment = getSegment(segmentDefinition.getXid(), idx++)) != null) {
                    dump.append(segment.toHtml(segmentDefinition, newParentIds));
                }
            }
            else if (positioned instanceof LoopDefinition) {
                LoopDefinition innerLoopDefinition = (LoopDefinition)positioned;
                int idx = 0;
                Loop innerLoop;
                while ((innerLoop = getLoopForPrinting(innerLoopDefinition, idx++)) != null) {
                    dump.append(innerLoop.toHtml(innerLoopDefinition, newParentIds));
                }
            }
        }
        dump.append("</div>");
        return dump.toString();
    }


    public Map<String, Object> toMap(LoopDefinition loopDefinition, List<String> parentIds, int rootLoopIndex, int loopIndex) {
        List<String> newParentIds = new ArrayList<>(parentIds);
        Map<String, Object> res = new HashMap<>();
        res.put("parentIds", parentIds);
        res.put("xid", _id);
        res.put("name", loopDefinition.getName());
        res.put("type", "loop");
        Set<Positioned> segmentsAndLoops = new TreeSet<>();
        if (loopDefinition.getLoop() != null) {
            segmentsAndLoops.addAll(loopDefinition.getLoop());
        }
        if (loopDefinition.getSegment() != null) {
            segmentsAndLoops.addAll(loopDefinition.getSegment());
        }
        List<Map<String, Object>> children = new ArrayList<>();
        for (Positioned positioned : segmentsAndLoops) {
            if (positioned instanceof SegmentDefinition) {
                SegmentDefinition segmentDefinition = (SegmentDefinition)positioned;
                int idx = 0;
                Segment segment;
                while ((segment = getSegment(segmentDefinition.getXid(), idx)) != null) {
                    children.add(segment.toMap(segmentDefinition, newParentIds, idx));
                    ++idx;
                }
            }
            else if (positioned instanceof LoopDefinition) {
                LoopDefinition innerLoopDefinition = (LoopDefinition)positioned;
                int idx = 0;
                Loop innerLoop;
                while ((innerLoop = getLoopForPrinting(innerLoopDefinition, idx)) != null) {
                    children.add(innerLoop.toMap(innerLoopDefinition, newParentIds, rootLoopIndex, idx));
                    ++idx;
                }
            }
        }
        if (!children.isEmpty()) {
            res.put("children", children);
        }
        return res;
    }

    /**
     * Send a LoopDefinition and the index of an loop, fetch the loop from the 
     * child loops that matches, given that the parentLoop has this loop as a child.
     * @param loopDefinition Loop definition for this spot in the x12 document.
     * @param idx The index of the loops returned thus far.
     * @return The child loop from the loops, or null otherwise.
     */
    private Loop getLoopForPrinting(LoopDefinition loopDefinition, int idx) {
        Loop loop = getLoop(loopDefinition.getXid(), idx);

        // We need to check that the loop we have gotten from getLoop
        // is actually a direct child of the current loop we are processing from the
        // loop definition.

        if (loop != null && _loops.stream().noneMatch(parentLoop -> parentLoop.getId().equals(loop.getId()))) {
            return null;
        }

        return loop;
    }

    /**
     * Returns the Loop in XML String format.
     * @return XML String
     */
    public String toXML() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.autodetectAnnotations(true);

        xstream.useAttributeFor(Loop.class, "_id");
        xstream.useAttributeFor(Segment.class, "_id");
        xstream.registerConverter(new ElementConverter());

        // setup proper security by limiting what classes can be loaded by XStream
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(new WildcardTypePermission(new String[] {"com.imsweb.x12.**"}));

        StringWriter writer = new StringWriter();
        xstream.marshal(this, new PrettyPrintWriter(writer));

        return writer.toString();
    }

    /**
     * Returns the Loop in JSON String format.
     * @return JSON String
     */
    public String toJson() {
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver() {
            @Override
            public HierarchicalStreamWriter createWriter(Writer writer) {
                return new JsonWriter(writer, AbstractJsonWriter.DROP_ROOT_MODE);
            }
        });

        xstream.autodetectAnnotations(true);

        // setup proper security by limiting what classes can be loaded by XStream
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(new WildcardTypePermission(new String[] {"com.imsweb.x12.**"}));

        return xstream.toXML(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Loop segments = (Loop)o;
        return Objects.equals(_separators, segments._separators) &&
                Objects.equals(_id, segments._id) &&
                Objects.equals(_segments, segments._segments) &&
                Objects.equals(_loops, segments._loops) &&
                Objects.equals(_parent, segments._parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_separators, _id, _segments, _loops, _parent);
    }
}
