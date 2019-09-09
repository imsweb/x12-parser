/*
 * Copyright (C) 2019 Information Management Services, Inc.
 */
package com.imsweb.x12;

import java.util.ArrayList;
import java.util.List;

public class TransactionSet {

    private Loop _headerLoop;
    private List<Loop> _dataLoops= new ArrayList<>();

    public Loop getHeaderLoop() {
        return _headerLoop;
    }

    public void setHeaderLoop(Loop headerLoop) {
        _headerLoop = headerLoop;
    }

    public List<Loop> getDataLoops() {
        return _dataLoops;
    }

    public void setDataLoops(List<Loop> dataLoops) {
        _dataLoops = dataLoops;
    }
}
