/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.x12;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.WildcardTypePermission;

import com.imsweb.x12.mapping.TransactionDefinition;
import com.imsweb.x12.mapping.TransactionDefinition.Usage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class XStreamTest {

    @Test
    void testXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.autodetectAnnotations(true);
        xstream.alias("transaction", TransactionDefinition.class);

        // setup proper security by limiting what classes can be loaded by XStream
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(new WildcardTypePermission(new String[] {"com.imsweb.x12.**"}));

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("mapping/837.5010.X222.A1.xml");

        TransactionDefinition config = (TransactionDefinition)xstream.fromXML(is);

        assertEquals("837", config.getXid());
        assertNotNull(config.getLoop());
        assertEquals("ISA_LOOP", config.getLoop().getXid());
        assertEquals(Usage.REQUIRED, config.getLoop().getUsage());
        assertEquals("ISA", config.getLoop().getSegment().get(0).getXid());
        assertEquals(Usage.REQUIRED, config.getLoop().getSegment().get(0).getElements().get(0).getUsage());
    }

}
