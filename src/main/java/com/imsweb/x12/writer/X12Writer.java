/*
 * Copyright (C) 2020 Information Management Services, Inc.
 */
package com.imsweb.x12.writer;

import java.util.ArrayList;
import java.util.List;

import com.imsweb.x12.LineBreak;
import com.imsweb.x12.Loop;
import com.imsweb.x12.Separators;
import com.imsweb.x12.mapping.TransactionDefinition;
import com.imsweb.x12.reader.X12Reader;
import com.imsweb.x12.reader.X12Reader.FileType;

public class X12Writer {
    
    private static List<Loop> _DATA_LOOPS;
    private static Separators _SEPARATORS;
    private static TransactionDefinition _DEFINITION;

    public X12Writer(FileType fileType, List<Loop> loops, Separators separators) {
        _DATA_LOOPS = loops;
        _DEFINITION = fileType.getDefinition();
        _SEPARATORS = separators;
    }
    
    public X12Writer(FileType fileType, List<Loop> loops) {
        _DATA_LOOPS = loops;
        _DEFINITION = fileType.getDefinition();
        _SEPARATORS = new Separators();
    }
    
    public X12Writer(X12Reader reader) {
        _DATA_LOOPS = reader.getLoops();
        _DEFINITION = reader.getDefinition();
        _SEPARATORS = reader.getSeparators();
    }


    /**
     * Gets an X12 formatted string representing this X12 reader. Will use no line
     * breaks after separators.
     *
     * @return X12 formatted string representing this X12 reader.
     */
    public String toX12String() {
        return toX12String(LineBreak.NONE);
    }

    /**
     * Gets an X12 formatted string representing this X12 reader.
     *
     * @param lineBreak Line break to use for separators.
     * @return X12 formatted string representing this X12 reader.
     */
    public String toX12String(LineBreak lineBreak) {
        _SEPARATORS.setLineBreak(lineBreak);
        return toX12StringImpl();
    }

    private String toX12StringImpl() {
        StringBuilder builder = new StringBuilder();
        for (Loop loop : _DATA_LOOPS) {
            builder.append(loop.toX12String(_DEFINITION.getLoop()));
            builder.append(_SEPARATORS.getLineBreak().getLineBreakString());
        }
        return builder.toString();
    }

    /**
     * To HTML string will create an HTML segment from this X12 file.
     *
     * @return Human readable html segment representation of the X12 file.
     */
    public String toHtml() {
        StringBuilder builder = new StringBuilder();
        for (Loop loop : _DATA_LOOPS) {
            builder.append(loop.toHtml(_DEFINITION.getLoop(), new ArrayList<>()));
        }
        return builder.toString();
    }
}
