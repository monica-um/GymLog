package com.mum.gymlog.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.mum.gymlog.database.GymLogDatabase;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity (tableName = GymLogDatabase.GYM_LOG_TABLE) // this object will be stored in the database
public class GymLog {
    @PrimaryKey (autoGenerate = true)
    private int id;

    private String exercise;
    private double weight;
    private int reps;
    private LocalDateTime date;

    // Constructor
    public GymLog(String exercise, double weight, int reps) {
        this.exercise = exercise;
        this.weight = weight;
        this.reps = reps;
        date = LocalDateTime.now(); // gives time stamp for current time
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GymLog gymLog = (GymLog) o;
        return id == gymLog.id && Double.compare(weight, gymLog.weight) == 0 && reps == gymLog.reps && Objects.equals(exercise, gymLog.exercise) && Objects.equals(date, gymLog.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, exercise, weight, reps, date);
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getExercise() {
        return exercise;
    }
    public void setExercise(String exercise) {
        this.exercise = exercise;
    }

    public double getWeight() {
        return weight;
    }
    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getReps() {
        return reps;
    }
    public void setReps(int reps) {
        this.reps = reps;
    }

    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
