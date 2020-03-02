/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.x12.mapping;

import java.util.List;
import java.util.Objects;

import com.imsweb.x12.mapping.TransactionDefinition.Usage;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("loop")
public class LoopDefinition implements Positioned {

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

    @Override
    public String getXid() {
        return _xid;
    }

    public String getType() {
        return _type;
    }

    public Usage getUsage() {
        return _usage;
    }

    @Override
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoopDefinition that = (LoopDefinition) o;
        return Objects.equals(_xid, that._xid) &&
            Objects.equals(_type, that._type) &&
            _usage == that._usage &&
            Objects.equals(_pos, that._pos) &&
            Objects.equals(_repeat, that._repeat) &&
            Objects.equals(_name, that._name) &&
            Objects.equals(_segment, that._segment) &&
            Objects.equals(_loop, that._loop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_xid, _type, _usage, _pos, _repeat, _name, _segment, _loop);
    }
}
