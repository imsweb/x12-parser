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

@XStreamAlias("segment")
public class SegmentDefinition implements Positioned {

    @XStreamAlias("xid")
    @XStreamAsAttribute
    private String _xid;
    @XStreamAlias("name")
    private String _name;
    @XStreamAlias("usage")
    @XStreamConverter(UsageConverter.class)
    private Usage _usage;
    @XStreamAlias("pos")
    private String _pos;
    @XStreamAlias("max_use")
    private String _maxUse;
    @XStreamAlias("syntax")
    @XStreamImplicit
    private List<String> _syntax;
    @XStreamAlias("element")
    @XStreamImplicit
    private List<ElementDefinition> _elements;
    @XStreamAlias("composite")
    @XStreamImplicit
    private List<CompositeDefinition> _composites;

    @Override
    public String getXid() {
        return _xid;
    }

    public String getName() {
        return _name;
    }

    public Usage getUsage() {
        return _usage;
    }

    @Override
    public String getPos() {
        return _pos;
    }

    public String getMaxUse() {
        return _maxUse;
    }

    public List<String> getSyntax() {
        return _syntax;
    }

    public List<ElementDefinition> getElements() {
        return _elements;
    }

    public List<CompositeDefinition> getComposites() {
        return _composites;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SegmentDefinition that = (SegmentDefinition) o;
        return Objects.equals(_xid, that._xid) &&
            Objects.equals(_name, that._name) &&
            _usage == that._usage &&
            Objects.equals(_pos, that._pos) &&
            Objects.equals(_maxUse, that._maxUse) &&
            Objects.equals(_syntax, that._syntax) &&
            Objects.equals(_elements, that._elements) &&
            Objects.equals(_composites, that._composites);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_xid, _name, _usage, _pos, _maxUse, _syntax, _elements, _composites);
    }
}
