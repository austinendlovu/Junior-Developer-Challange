 package com.developerChallenge.developer_Challenge_backend.Services;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.developerChallenge.developer_Challenge_backend.Models.Lesson;

@Service
public class LessonReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(LessonReminderScheduler.class);

    private final LessonService lessonService;

    public LessonReminderScheduler(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @Scheduled(fixedRate = 60000) // every 60 seconds
    public void sendLessonReminders() {
        logger.info("Running lesson reminder scheduler at {}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();

        List<Lesson> lessonsIn30Mins = lessonService.findLessonsStartingBetween(now.plusMinutes(29), now.plusMinutes(31));
        logger.info("Found {} lessons starting in 30 minutes", lessonsIn30Mins.size());
        lessonsIn30Mins.forEach(lesson -> {
            logger.info("Sending 30-min reminder for lesson id {}", lesson.getId());
            lessonService.sendReminder(lesson, 30);
        });

        List<Lesson> lessonsIn10Mins = lessonService.findLessonsStartingBetween(now.plusMinutes(9), now.plusMinutes(11));
        logger.info("Found {} lessons starting in 10 minutes", lessonsIn10Mins.size());
        lessonsIn10Mins.forEach(lesson -> {
            logger.info("Sending 10-min reminder for lesson id {}", lesson.getId());
            lessonService.sendReminder(lesson, 10);
        });
    }
}
