/*
 * Copyright (C) 2019 Information Management Services, Inc.
 */
package com.imsweb.x12;

import java.util.ArrayList;
import java.util.List;

public class InterchangeTransaction {

    private Loop _interchangeLoop;
    private List<TransactionSet> _transactionSets = new ArrayList<>();

    public Loop getInterchangeLoop() {
        return _interchangeLoop;
    }

    public void setInterchangeLoop(Loop interchangeLoop) {
        _interchangeLoop = interchangeLoop;
    }

    public List<TransactionSet> getTransactionSets() {
        return _transactionSets;
    }

    public void setTransactionSets(List<TransactionSet> transactionSets) {
        _transactionSets = transactionSets;
    }
}
