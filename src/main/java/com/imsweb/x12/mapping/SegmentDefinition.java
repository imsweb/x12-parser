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

@XStreamAlias("segment")
public class SegmentDefinition {

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

    public String getXid() {
        return _xid;
    }

    public String getName() {
        return _name;
    }

    public Usage getUsage() {
        return _usage;
    }

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

}
