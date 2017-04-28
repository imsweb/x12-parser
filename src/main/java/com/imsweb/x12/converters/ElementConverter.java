/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.x12.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import com.imsweb.x12.Element;

public class ElementConverter implements Converter {

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Element element = (Element)source;
        writer.addAttribute("id", element.getId());
        writer.setValue(element.getValue());
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return null;
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(Element.class);
    }
}
