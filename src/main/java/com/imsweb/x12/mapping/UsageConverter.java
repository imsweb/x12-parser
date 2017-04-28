/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.x12.mapping;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import com.imsweb.x12.mapping.TransactionDefinition.Usage;

public class UsageConverter implements Converter {

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        // TODO
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        if ("R".equals(reader.getValue()))
            return Usage.REQUIRED;
        if ("S".equals(reader.getValue()))
            return Usage.SITUATIONAL;
        if ("N".equals(reader.getValue()))
            return Usage.NOT_USED;

        throw new RuntimeException("Unexpected usage value: " + reader.getValue());
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(Usage.class);
    }
}
