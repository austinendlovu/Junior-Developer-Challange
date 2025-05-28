package com.developerChallenge.developer_Challenge_backend.Services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developerChallenge.developer_Challenge_backend.DTOs.CreateLessonDto;
import com.developerChallenge.developer_Challenge_backend.Exceptions.ConflictException;
import com.developerChallenge.developer_Challenge_backend.Exceptions.ResourceNotFoundException;
import com.developerChallenge.developer_Challenge_backend.Models.Lesson;
import com.developerChallenge.developer_Challenge_backend.Models.LessonStatus;
import com.developerChallenge.developer_Challenge_backend.Models.User;
import com.developerChallenge.developer_Challenge_backend.Repositories.LessonRepository;
import com.developerChallenge.developer_Challenge_backend.Repositories.UserRepository;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public LessonService(LessonRepository lessonRepository,
                         UserRepository userRepository,
                         EmailService emailService,
                         NotificationService notificationService) {
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Lesson createLesson(CreateLessonDto dto, Long teacherId) {
        User teacher = userRepository.findTeacherById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found or user is not a teacher"));

        validateTimeSlot(dto, teacherId);

        Lesson lesson = new Lesson();
        lesson.setSubject(dto.getSubject());
        lesson.setDescription(dto.getDescription());
        lesson.setDate(dto.getDate());
        lesson.setStartTime(dto.getStartTime());
        lesson.setEndTime(dto.getEndTime());
        lesson.setClassroom(dto.getClassroom());
        lesson.setType(dto.getType());
        lesson.setTeacher(teacher);

        return lessonRepository.save(lesson);
    }

    private void validateTimeSlot(CreateLessonDto dto, Long teacherId) {
        boolean invalidRange = dto.getEndTime().isBefore(dto.getStartTime()) || dto.getEndTime().equals(dto.getStartTime());

        if (invalidRange) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        List<Lesson> overlappingLessons = lessonRepository.findByTeacherIdAndDateAndTimeRange(
                teacherId,
                dto.getDate(),
                dto.getStartTime(),
                dto.getEndTime()
        );

        if (!overlappingLessons.isEmpty()) {
            throw new ConflictException("Time slot conflicts with existing lesson");
        }
    }

    public List<Lesson> getWeeklyTimetable(Long teacherId, LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        return lessonRepository.findByTeacherIdAndDateBetweenOrderByDateAscStartTimeAsc(
                teacherId,
                weekStartDate,
                weekEndDate
        );
    }

    public List<Lesson> findLessonsStartingBetween(LocalDateTime start, LocalDateTime end) {
        LocalDate date = start.toLocalDate();
        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();

        return lessonRepository.findLessonsStartingBetween(date, startTime, endTime);
    }

    public void sendReminder(Lesson lesson, int minutesLeft) {
        String email = lesson.getTeacher().getEmail();
        String subject = "Lesson in " + minutesLeft + " minutes";

        Map<String, Object> model = Map.of(
                "name", lesson.getTeacher().getUsername(),
                "subject", lesson.getSubject(),
                "date", lesson.getDate().toString(),
                "time", lesson.getStartTime().toString(),
                "classroom", lesson.getClassroom(),
                "minutesLeft", minutesLeft
        );

        emailService.sendLessonReminderEmail(email, subject, model);

        notificationService.createNotification(
                lesson.getTeacher().getId(),
                "You have a lesson on " + lesson.getSubject() + " in " + minutesLeft + " minutes"
        );
    }
    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + id));
    }
    public Lesson updateLesson(Long id, CreateLessonDto dto, Long teacherId) {
        Lesson existingLesson = lessonRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + id));

     
        if (!existingLesson.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("You are not authorized to update this lesson");
        }

     
        existingLesson.setSubject(dto.getSubject());
        existingLesson.setDescription(dto.getDescription());
        existingLesson.setDate(dto.getDate());
        existingLesson.setStartTime(dto.getStartTime());
        existingLesson.setEndTime(dto.getEndTime());
        existingLesson.setClassroom(dto.getClassroom());
        existingLesson.setType(dto.getType());

        return lessonRepository.save(existingLesson);
    }
    public void deleteLesson(Long id, Long teacherId) {
        Lesson lesson = lessonRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + id));

        // Authorization check
        if (!lesson.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("You are not authorized to delete this lesson");
        }

        lessonRepository.deleteById(id);
    }
    public List<Lesson> getAllLessonsByTeacher(Long teacherId) {
        return lessonRepository.findAllByTeacherId(teacherId);
    }

    public List<Lesson> getLessonsStartingSoon(Long teacherId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        LocalTime in10 = now.plusMinutes(10);
        LocalTime in30 = now.plusMinutes(30);

        return lessonRepository.findAllByTeacherIdAndDateAndStartTimeBetween(
            teacherId,
            today,
            now,
            in30
        );
    }

    public Lesson updateLessonStatus(Long lessonId, LessonStatus status, Long teacherId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        if (!lesson.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Unauthorized to update this lesson");
        }

        lesson.setStatus(status);
        return lessonRepository.save(lesson);
    }



}
