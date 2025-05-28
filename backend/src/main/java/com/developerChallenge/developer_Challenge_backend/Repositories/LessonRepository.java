package com.developerChallenge.developer_Challenge_backend.Repositories;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.developerChallenge.developer_Challenge_backend.Models.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

  
    @Query("SELECT l FROM Lesson l WHERE " +
           "l.teacher.id = :teacherId AND " +
           "l.date = :date AND " +
           "((l.startTime < :endTime AND l.endTime > :startTime))")
    List<Lesson> findByTeacherIdAndDateAndTimeRange(
        @Param("teacherId") Long teacherId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );

   
    List<Lesson> findByTeacherIdAndDateBetweenOrderByDateAscStartTimeAsc(
        Long teacherId,
        LocalDate startDate,
        LocalDate endDate
    );

    
    List<Lesson> findByTeacherIdAndDateOrderByStartTimeAsc(
        Long teacherId,
        LocalDate date
    );

    List<Lesson> findByDateAndStartTimeBetween(LocalDate date, LocalTime start, LocalTime end);

    @Query("SELECT l FROM Lesson l WHERE l.date = :date AND l.startTime BETWEEN :startTime AND :endTime")
    List<Lesson> findLessonsStartingBetween(
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
    List<Lesson> findAllByTeacherId(Long teacherId);

    List<Lesson> findAllByTeacherIdAndDateAndStartTimeBetween(
    	    Long teacherId,
    	    LocalDate date,
    	    LocalTime start,
    	    LocalTime end
    	);

}
