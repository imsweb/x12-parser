/*
 * Copyright (C) 2019 Information Management Services, Inc.
 */
package com.imsweb.x12;

import java.util.ArrayList;
import java.util.List;

public class FunctionalGroup {

    private Loop _groupHeaderLoop;
    private List<TransactionSet> _transactionSets = new ArrayList<>();

    public Loop getGroupHeaderLoop() {
        return _groupHeaderLoop;
    }

    public void setGroupHeaderLoop(Loop groupHeaderLoop) {
        _groupHeaderLoop = groupHeaderLoop;
    }

    public List<TransactionSet> getTransactionSets() {
        return _transactionSets;
    }

    public void setTransactionSets(List<TransactionSet> transactionSets) {
        _transactionSets = transactionSets;
    }
}
