package com.imsweb.x12.mapping;

import java.io.InputStream;

/**
 * An {@link X12Mapping} built from a user-supplied mapping definition XML document. Use this to
 * parse X12 files whose specification is not one of the built-in {@code X12Reader.FileType} values.
 * <p>
 * The mapping XML must follow the same structure as the built-in mapping files (see the
 * {@code mapping} resources, e.g. {@code 999.5010.xml}).
 */
public class CustomX12Mapping implements X12Mapping {

    private final TransactionDefinition _definition;
    private final String _version;

    /**
     * @param version the ANSI version identifier expected in the GS08 element (e.g. {@code 005010X279A1})
     * @param mapping an input stream to the mapping definition XML; it is read fully by this constructor
     *                but not closed (the caller retains ownership)
     */
    public CustomX12Mapping(String version, InputStream mapping) {
        if (version == null)
            throw new IllegalArgumentException("version cannot be null");
        if (mapping == null)
            throw new IllegalArgumentException("mapping cannot be null");
        _version = version;
        _definition = X12Mapping.load(mapping);
    }

    @Override
    public TransactionDefinition getTransactionDefinition() {
        return _definition;
    }

    @Override
    public String getVersion() {
        return _version;
    }
}
