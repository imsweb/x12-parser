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

    private SegmentDefinition _firstSegmentXid;

    public LoopConfig(String loopName, String parentLoop, List<String> childList, String loopRepeats, Usage loopUsage) {
        _loopId = loopName;
        _parentLoop = parentLoop;
        _childList = childList;
        _loopRepeats = loopRepeats;
        _loopUsage = loopUsage;
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

}
