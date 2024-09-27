package org.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "timestamps")
public class TimeStampEntity {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "time")
    private Instant time;

    public TimeStampEntity() {
    }

    public TimeStampEntity(int id, Instant time) {
        this.id = id;
        this.time = time;
    }

    public Integer getId() {
        return id;
    }

    public Instant getTime() {
        return time;
    }
}