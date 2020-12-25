package com.dht.pags.wallet.transactionprocessor.domain;

import com.dht.pags.wallet.domain.TransactionCreatedEvent;

import java.util.HashSet;
import java.util.Set;

public class TransactionCreatedEventSet {

    private Set<TransactionCreatedEvent> eventSet;

    public TransactionCreatedEventSet() {
        eventSet = new HashSet<>();
    }

    public TransactionCreatedEventSet(Set<TransactionCreatedEvent> eventSet) {
        this.eventSet = eventSet;
    }

    public Set<TransactionCreatedEvent> getEventSet() {
        return eventSet;
    }

    public void setEventSet(Set<TransactionCreatedEvent> eventSet) {
        this.eventSet = eventSet;
    }
}
