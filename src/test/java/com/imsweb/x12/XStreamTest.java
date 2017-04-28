/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.x12;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import com.imsweb.x12.mapping.TransactionDefinition;
import com.imsweb.x12.mapping.TransactionDefinition.Usage;

public class XStreamTest {

    @Test
    public void testXStream() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.autodetectAnnotations(true);
        xstream.alias("transaction", TransactionDefinition.class);

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("mapping/837.5010.X222.A1.xml");

        TransactionDefinition config = (TransactionDefinition)xstream.fromXML(is);

        Assert.assertEquals("837", config.getXid());
        Assert.assertNotNull(config.getLoop());
        Assert.assertEquals("ISA_LOOP", config.getLoop().getXid());
        Assert.assertEquals(Usage.REQUIRED, config.getLoop().getUsage());
        Assert.assertEquals("ISA", config.getLoop().getSegment().get(0).getXid());
        Assert.assertEquals(Usage.REQUIRED, config.getLoop().getSegment().get(0).getElements().get(0).getUsage());
    }

}
