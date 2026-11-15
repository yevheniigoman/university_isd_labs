package com.runningtracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * A recorded Training. Distance is stored as a whole number of metres and
 * duration as a whole number of seconds (REQ-F-6, REQ-F-7). The date is a
 * calendar date only, with no time or timezone. Every Training belongs to
 * exactly one account and has its own identity (REQ-F-4).
 */
@Entity
@Table(name = "trainings")
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_date", nullable = false)
    private LocalDate date;

    @Column(name = "distance_metres", nullable = false)
    private int distanceMetres;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount owner;

    protected Training() {
    }

    public Training(LocalDate date, int distanceMetres, int durationSeconds, UserAccount owner) {
        this.date = date;
        this.distanceMetres = distanceMetres;
        this.durationSeconds = durationSeconds;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getDistanceMetres() {
        return distanceMetres;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public UserAccount getOwner() {
        return owner;
    }

    /**
     * Replaces the mutable fields of a Training (REQ-F-10). Values are expected
     * to have already passed the field validators.
     */
    public void update(LocalDate date, int distanceMetres, int durationSeconds) {
        this.date = date;
        this.distanceMetres = distanceMetres;
        this.durationSeconds = durationSeconds;
    }
}
