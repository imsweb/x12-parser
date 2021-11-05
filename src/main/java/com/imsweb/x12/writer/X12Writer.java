/*
 * Copyright (C) 2020 Information Management Services, Inc.
 */
package com.imsweb.x12.writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.imsweb.x12.LineBreak;
import com.imsweb.x12.Loop;
import com.imsweb.x12.Separators;
import com.imsweb.x12.mapping.TransactionDefinition;
import com.imsweb.x12.reader.X12Reader;
import com.imsweb.x12.reader.X12Reader.FileType;

public class X12Writer {
    
    private final List<Loop> _dataLoops;
    private final Separators _separators;
    private final TransactionDefinition _definition;

    public X12Writer(FileType fileType, List<Loop> loops, Separators separators) {
        _dataLoops = loops;
        _definition = fileType.getDefinition();
        _separators = separators;
    }
    
    public X12Writer(FileType fileType, List<Loop> loops) {
        _dataLoops = loops;
        _definition = fileType.getDefinition();
        _separators = new Separators();
    }
    
    public X12Writer(X12Reader reader) {
        _dataLoops = reader.getLoops();
        _definition = reader.getDefinition();
        _separators = reader.getSeparators();
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
        _separators.setLineBreak(lineBreak);
        return toX12StringImpl();
    }

    private String toX12StringImpl() {
        StringBuilder builder = new StringBuilder();
        for (Loop loop : _dataLoops) {
            builder.append(loop.toX12String(_definition.getLoop()));
            builder.append(_separators.getLineBreak().getLineBreakString());
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
        for (Loop loop : _dataLoops) {
            builder.append(loop.toHtml(_definition.getLoop(), new ArrayList<>()));
        }
        return builder.toString();
    }

    public List<Map<String, Object>> toListOfMap() {
        List<Map<String, Object>> res = new ArrayList<>();
        int idx = 0;
        for (Loop loop : _dataLoops) {
            res.add(loop.toMap(_definition.getLoop(), new ArrayList<>(), idx, idx));
            ++idx;
        }
        return res;
    }
}
