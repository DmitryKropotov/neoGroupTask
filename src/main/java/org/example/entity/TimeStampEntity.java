package org.example.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "timestamps")
public class TimeStampEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "time")
    private Instant time;

    public TimeStampEntity() {
    }

    public TimeStampEntity(Instant time) {
        this.time = time;
    }

    public Integer getId() {
        return id;
    }

    public Instant getTime() {
        return time;
    }
}