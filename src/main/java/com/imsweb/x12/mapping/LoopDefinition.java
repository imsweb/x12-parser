/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.x12.mapping;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import com.imsweb.x12.mapping.TransactionDefinition.Usage;

@XStreamAlias("loop")
public class LoopDefinition {

    @XStreamAlias("xid")
    @XStreamAsAttribute
    private String _xid;
    @XStreamAlias("type")
    @XStreamAsAttribute
    private String _type;
    @XStreamAlias("usage")
    @XStreamConverter(UsageConverter.class)
    private Usage _usage;
    @XStreamAlias("pos")
    private String _pos;
    @XStreamAlias("repeat")
    private String _repeat;
    @XStreamAlias("name")
    private String _name;
    @XStreamAlias("segment")
    @XStreamImplicit
    private List<SegmentDefinition> _segment;
    @XStreamAlias("loop")
    @XStreamImplicit
    private List<LoopDefinition> _loop;

    public String getXid() {
        return _xid;
    }

    public String getType() {
        return _type;
    }

    public Usage getUsage() {
        return _usage;
    }

    public String getPos() {
        return _pos;
    }

    public String getRepeat() {
        return _repeat;
    }

    public String getName() {
        return _name;
    }

    public List<SegmentDefinition> getSegment() {
        return _segment;
    }

    public List<LoopDefinition> getLoop() {
        return _loop;
    }

}
