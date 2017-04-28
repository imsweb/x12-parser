/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.x12.mapping;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("valid_codes")
public class ValidCodesDefinition {

    @XStreamAlias("external")
    @XStreamAsAttribute
    private String _external;
    @XStreamAlias("code")
    @XStreamImplicit
    private List<String> _codes;

    public String getExternal() {
        return _external;
    }

    public List<String> getCodes() {
        return _codes;
    }
}
