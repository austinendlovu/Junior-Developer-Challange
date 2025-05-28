package com.developerChallenge.developer_Challenge_backend.DTOs;

import java.time.LocalDate;
import java.time.LocalTime;

import com.developerChallenge.developer_Challenge_backend.Models.LessonType;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class CreateLessonDto {

    @NotBlank
    private String subject;

    @NotBlank
    private String description;

    @NotNull
    @FutureOrPresent
    private LocalDate date;

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotBlank
    private String classroom;

    @NotNull
    private LessonType type;

    public CreateLessonDto() {
        super();
    }

    public CreateLessonDto(@NotBlank String subject, @NotBlank String description,
                           @NotNull @FutureOrPresent LocalDate date,
                           @NotNull LocalTime startTime, @NotNull LocalTime endTime,
                           @NotBlank String classroom, @NotNull LessonType type) {
        this.subject = subject;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classroom = classroom;
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public LessonType getType() {
        return type;
    }

    public void setType(LessonType type) {
        this.type = type;
    }
}
