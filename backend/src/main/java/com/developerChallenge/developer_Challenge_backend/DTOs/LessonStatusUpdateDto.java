package com.developerChallenge.developer_Challenge_backend.DTOs;

import com.developerChallenge.developer_Challenge_backend.Models.LessonStatus;

public class LessonStatusUpdateDto {
   
    private LessonStatus status;

    public LessonStatus getStatus() {
        return status;
    }

    public void setStatus(LessonStatus status) {
        this.status = status;
    }
}
