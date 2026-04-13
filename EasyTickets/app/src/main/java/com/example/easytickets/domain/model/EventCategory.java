package com.example.easytickets.domain.model;

import java.io.Serializable;
import java.util.Objects;

public class EventCategory implements Serializable {

    private final String id;
    private final String name;

    public EventCategory(String id, String name) {
        this.id = id == null ? "" : id;
        this.name = name == null ? "" : name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventCategory)) {
            return false;
        }
        EventCategory that = (EventCategory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
