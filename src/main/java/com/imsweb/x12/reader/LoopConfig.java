package com.imsweb.x12.reader;

import java.util.List;

import com.imsweb.x12.mapping.SegmentDefinition;
import com.imsweb.x12.mapping.TransactionDefinition.Usage;

public class LoopConfig {

    private String _loopId;
    private Integer _loopRepeatCount = 0;
    private String _loopRepeats;
    private Usage _loopUsage;
    private List<String> _childList;
    private String _parentLoop;
    private boolean _hasDataSegments = true;

    private SegmentDefinition _firstSegmentXid;
    private SegmentDefinition _lastSegmentXid; // used for ISA_LOOP, GS_LOOP, ST_LOOP since the segments for these loops appear only at the beginning and end of a transaction

    public LoopConfig(String loopName, String parentLoop, List<String> childList, String loopRepeats, Usage loopUsage, boolean hasDataSegments) {
        _loopId = loopName;
        _parentLoop = parentLoop;
        _childList = childList;
        _loopRepeats = loopRepeats;
        _loopUsage = loopUsage;
        _hasDataSegments = hasDataSegments;
    }

    public String getLoopId() {
        return _loopId;
    }

    public Integer getLoopRepeatCount() {
        return _loopRepeatCount;
    }

    public void incrementLoopRepeatCount() {
        _loopRepeatCount = _loopRepeatCount + 1;
    }

    public List<String> getChildList() {
        return _childList;
    }

    public String getParentLoop() {
        return _parentLoop;
    }

    public Usage getLoopUsage() {
        return _loopUsage;
    }

    public String getLoopRepeats() {
        return _loopRepeats;
    }

    public SegmentDefinition getFirstSegmentXid() {
        return _firstSegmentXid;
    }

    public void setFirstSegmentXid(SegmentDefinition firstSegmentXid) {
        _firstSegmentXid = firstSegmentXid;
    }

    public SegmentDefinition getLastSegmentXid() {
        return _lastSegmentXid;
    }

    public void setLastSegmentXid(SegmentDefinition lastSegmentXid) {
        _lastSegmentXid = lastSegmentXid;
    }

    public boolean hasDataSegments() {
        return _hasDataSegments;
    }

    public void setHasDataSegments(boolean hasDataSegments) {
        _hasDataSegments = hasDataSegments;
    }
}
