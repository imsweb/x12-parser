/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.x12.mapping;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("transaction")
public class TransactionDefinition {

    public enum Usage {
        REQUIRED,
        SITUATIONAL,
        NOT_USED
    }

    @XStreamAlias("xid")
    @XStreamAsAttribute
    private String _xid;
    @XStreamAlias("name")
    private String _name;
    @XStreamAlias("loop")
    private LoopDefinition _loop;

    public String getXid() {
        return _xid;
    }

    public String getName() {
        return _name;
    }

    public LoopDefinition getLoop() {
        return _loop;
    }

}
