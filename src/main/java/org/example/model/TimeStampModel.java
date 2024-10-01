package org.example.model;

import java.time.Instant;

public class TimeStampModel implements Comparable<TimeStampModel> {

    private int id;

    private Instant time;

    public TimeStampModel(int id, Instant time) {
        this.id = id;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public Instant getTime() {
        return time;
    }

    @Override
    public int compareTo(TimeStampModel o) {
        return time.compareTo(o.time);
    }
}
