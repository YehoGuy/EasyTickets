package com.example.easytickets.domain.model;

import java.io.Serializable;

public class EventDetails implements Serializable {

    private final EventSummary summary;

    public EventDetails(EventSummary summary) {
        this.summary = summary;
    }

    public EventSummary getSummary() {
        return summary;
    }
}
