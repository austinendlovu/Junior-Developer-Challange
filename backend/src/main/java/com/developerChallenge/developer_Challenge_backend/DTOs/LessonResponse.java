package com.developerChallenge.developer_Challenge_backend.DTOs;

import java.time.LocalDate;
import java.time.LocalTime;

import com.developerChallenge.developer_Challenge_backend.Models.LessonStatus;

public class LessonResponse {
    private Long id;
    private String subject;
    private String description;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String classroom;
    private String type;
   private LessonStatus status;
public LessonResponse() {
	super();
}
public LessonResponse(Long id, String subject, String description, LocalDate date, LocalTime startTime,
		LocalTime endTime, String classroom, String type, LessonStatus status) {
	super();
	this.id = id;
	this.subject = subject;
	this.description = description;
	this.date = date;
	this.startTime = startTime;
	this.endTime = endTime;
	this.classroom = classroom;
	this.type = type;
	this.status = status;
}
public Long getId() {
	return id;
}
public void setId(Long id) {
	this.id = id;
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
public String getType() {
	return type;
}
public void setType(String type) {
	this.type = type;
}
public LessonStatus getStatus() {
	return status;
}
public void setStatus(LessonStatus status) {
	this.status = status;
}
   
   
}
