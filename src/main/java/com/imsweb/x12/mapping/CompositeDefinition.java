/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.x12.mapping;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("composite")
public class CompositeDefinition {

    @XStreamAlias("xid")
    @XStreamAsAttribute
    private String _xid;
    @XStreamAlias("data_ele")
    private String _dataEle;
    @XStreamAlias("name")
    private String _name;
    @XStreamAlias("usage")
    private String _usage;
    @XStreamAlias("seq")
    private String _seq;
    @XStreamAlias("refdes")
    private String _refDes;
    @XStreamAlias("repeat")
    private String _repeat;
    @XStreamAlias("element")
    @XStreamImplicit
    private List<ElementDefinition> _elements;

    public String getXid() {
        return _xid;
    }

    public String getDataEle() {
        return _dataEle;
    }

    public String getName() {
        return _name;
    }

    public String getUsage() {
        return _usage;
    }

    public String getSeq() {
        return _seq;
    }

    public String getRefDes() {
        return _refDes;
    }

    public String getRepeat() {
        return _repeat;
    }

    public List<ElementDefinition> getElements() {
        return _elements;
    }

}
