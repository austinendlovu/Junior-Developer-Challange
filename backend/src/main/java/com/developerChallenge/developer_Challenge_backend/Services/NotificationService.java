package com.developerChallenge.developer_Challenge_backend.Services;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

 
    private final Map<Long, List<String>> userNotifications = new ConcurrentHashMap<>();

    public void createNotification(Long userId, String message) {
        userNotifications
            .computeIfAbsent(userId, id -> Collections.synchronizedList(new ArrayList<>()))
            .add(message);
    }

  
    public void createNotification(com.developerChallenge.developer_Challenge_backend.Models.Lesson lesson, String message) {
        if (lesson.getTeacher() == null || lesson.getTeacher().getId() == null) {
            throw new IllegalArgumentException("Lesson must have a teacher with an ID");
        }
        createNotification(lesson.getTeacher().getId(), message);
    }

   
    public List<String> getNotifications(Long userId) {
        return userNotifications.getOrDefault(userId, Collections.emptyList());
    }

  
    public void clearNotifications(Long userId) {
        userNotifications.remove(userId);
    }
}
