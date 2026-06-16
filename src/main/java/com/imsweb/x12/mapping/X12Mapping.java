package com.imsweb.x12.mapping;

import java.io.InputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.WildcardTypePermission;

/**
 * Describes an X12 transaction mapping used by {@code X12Reader}: the parsed transaction
 * definition and the ANSI version identifier (the value of the GS08 element) expected for
 * files using this mapping.
 * <p>
 * Implement this interface (or use {@link CustomX12Mapping}) to parse X12 files against a
 * mapping that is not one of the built-in {@code X12Reader.FileType} values.
 */
public interface X12Mapping {

    /**
     * @return the transaction definition that drives parsing
     */
    TransactionDefinition getTransactionDefinition();

    /**
     * @return the ANSI version identifier expected in the GS08 element for this mapping
     */
    String getVersion();

    /**
     * Parses a mapping definition XML document into a {@link TransactionDefinition}, using the
     * same secured XStream configuration as the built-in mappings.
     * @param mapping an input stream to the mapping definition XML; the caller is responsible for closing it
     * @return the parsed transaction definition
     */
    static TransactionDefinition load(InputStream mapping) {
        XStream xstream = new XStream(new StaxDriver());
        xstream.autodetectAnnotations(true);
        xstream.alias("transaction", TransactionDefinition.class);

        // setup proper security by limiting what classes can be loaded by XStream
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(new WildcardTypePermission(new String[] {"com.imsweb.x12.**"}));

        return (TransactionDefinition)xstream.fromXML(mapping);
    }
}
