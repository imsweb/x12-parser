/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.x12.mapping;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import com.imsweb.x12.mapping.TransactionDefinition.Usage;

@XStreamAlias("element")
public class ElementDefinition {

    @XStreamAlias("xid")
    @XStreamAsAttribute
    private String _xid;
    @XStreamAlias("data_ele")
    private String _dataEle;
    @XStreamAlias("name")
    private String _name;
    @XStreamAlias("usage")
    @XStreamConverter(UsageConverter.class)
    private Usage _usage;
    @XStreamAlias("seq")
    private String _seq;
    @XStreamAlias("refdes")
    private String _refDes;
    @XStreamAlias("repeat")
    private String _repeat;
    @XStreamAlias("regex")
    private String _regex;
    @XStreamAlias("valid_codes")
    private ValidCodesDefinition _validCodes;

    public String getXid() {
        return _xid;
    }

    public String getDataEle() {
        return _dataEle;
    }

    public String getName() {
        return _name;
    }

    public Usage getUsage() {
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

    public String getRegex() {
        return _regex;
    }

    public ValidCodesDefinition getValidCodes() {
        return _validCodes;
    }

}
