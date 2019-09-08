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

    public Loop getLoop() {
        Loop masterLoop = _interchangeLoop;
        for (TransactionSet set : _transactionSets) {
            Loop tsLoop = set.getHeaderLoop();
            set.getDataLoops().forEach(d -> tsLoop.getLoops().add(d));
            if (masterLoop.getLoops().isEmpty())
                masterLoop.getLoops().add(tsLoop);
            else
                masterLoop.getLoop(masterLoop.getLoops().size() -1).getLoops().add(tsLoop);
        }
        return masterLoop;
    }
}
