package com.developerChallenge.developer_Challenge_backend.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.developerChallenge.developer_Challenge_backend.DTOs.CreateLessonDto;
import com.developerChallenge.developer_Challenge_backend.DTOs.LessonResponse;
import com.developerChallenge.developer_Challenge_backend.DTOs.LessonStatusUpdateDto;
import com.developerChallenge.developer_Challenge_backend.Models.Lesson;
import com.developerChallenge.developer_Challenge_backend.Models.User;
import com.developerChallenge.developer_Challenge_backend.Repositories.UserRepository;
import com.developerChallenge.developer_Challenge_backend.Services.JwtService;
import com.developerChallenge.developer_Challenge_backend.Services.LessonService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "http://localhost:8081")
public class LessonController {

	private final LessonService lessonService;
	private final UserRepository userRepository;
	private final JwtService jwtService;

	public LessonController(LessonService lessonService, UserRepository userRepository, JwtService jwtService) {
		this.lessonService = lessonService;
		this.userRepository = userRepository;
		this.jwtService = jwtService;
	}

	private String extractToken(String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new IllegalArgumentException("Authorization header missing or invalid");
		}
		return authHeader.substring(7);
	}

	@PostMapping
	public ResponseEntity<?> createLesson(@Valid @RequestBody CreateLessonDto dto,
			@RequestHeader("Authorization") String authHeader) {

		try {
			String token = extractToken(authHeader);

			String role = jwtService.extractRole(token);
			if (role == null || !role.equalsIgnoreCase("TEACHER")) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(Map.of("message", "User does not have TEACHER role"));
			}

			String username = jwtService.extractUsername(token);
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("User not found"));

			Lesson lesson = lessonService.createLesson(dto, user.getId());

			return ResponseEntity.status(HttpStatus.CREATED).body(mapToLessonResponse(lesson));

		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token expired"));
		} catch (io.jsonwebtoken.JwtException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid token"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Unexpected error occurred", "details", e.getMessage()));
		}
	}

	@GetMapping("/week")
	public ResponseEntity<?> getWeeklyTimetable(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
			@RequestHeader("Authorization") String authHeader) {

		try {
			String token = extractToken(authHeader);

			String role = jwtService.extractRole(token);
			if (role == null || !role.equalsIgnoreCase("TEACHER")) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(Map.of("message", "User does not have TEACHER role"));
			}

			String username = jwtService.extractUsername(token);
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("User not found"));

			List<Lesson> lessons = lessonService.getWeeklyTimetable(user.getId(), weekStartDate);

			List<LessonResponse> response = lessons.stream().map(this::mapToLessonResponse)
					.collect(Collectors.toList());

			return ResponseEntity.ok(response);

		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token expired"));
		} catch (io.jsonwebtoken.JwtException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid token"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Unexpected error occurred", "details", e.getMessage()));
		}
	}

	private LessonResponse mapToLessonResponse(Lesson lesson) {
		LessonResponse response = new LessonResponse();

		response.setId(lesson.getId());
		response.setSubject(lesson.getSubject());
		response.setDescription(lesson.getDescription());
		response.setDate(lesson.getDate());
		response.setStartTime(lesson.getStartTime());
		response.setEndTime(lesson.getEndTime());
		response.setClassroom(lesson.getClassroom());
		response.setType(lesson.getType().name());
		response.setStatus(lesson.getStatus());

		return response;
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getLessonById(@PathVariable Long id) {
		try {
			Lesson lesson = lessonService.getLessonById(id);
			return ResponseEntity.ok(mapToLessonResponse(lesson));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateLesson(@PathVariable Long id, @Valid @RequestBody CreateLessonDto dto,
			@RequestHeader("Authorization") String authHeader) {

		try {
			String token = extractToken(authHeader);
			String role = jwtService.extractRole(token);
			if (!"TEACHER".equalsIgnoreCase(role)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied"));
			}

			String username = jwtService.extractUsername(token);
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("User not found"));

			Lesson updatedLesson = lessonService.updateLesson(id, dto, user.getId());
			return ResponseEntity.ok(mapToLessonResponse(updatedLesson));

		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		}

	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteLesson(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {

		try {
			
			String token = extractToken(authHeader);
			String role = jwtService.extractRole(token);
			String username = jwtService.extractUsername(token);
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("User not found"));
			if (!"TEACHER".equalsIgnoreCase(role)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied"));
			}

			lessonService.deleteLesson(id,user.getId());
			return ResponseEntity.ok(Map.of("message", "Lesson deleted successfully"));

		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		}
	}

	@GetMapping
	public ResponseEntity<?> getAllLessons(@RequestHeader("Authorization") String authHeader) {
		try {
			String token = extractToken(authHeader);
			String role = jwtService.extractRole(token);
			if (!"TEACHER".equalsIgnoreCase(role)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied"));
			}

			String username = jwtService.extractUsername(token);
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("User not found"));

			List<Lesson> lessons = lessonService.getAllLessonsByTeacher(user.getId());
			List<LessonResponse> response = lessons.stream().map(this::mapToLessonResponse)
					.collect(Collectors.toList());

			return ResponseEntity.ok(response);

		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		}
	}

	@GetMapping("/notifications")
	public ResponseEntity<?> getUpcomingNotifications(@RequestHeader("Authorization") String authHeader) {
		try {
			String token = extractToken(authHeader);
			String role = jwtService.extractRole(token);

			if (!"TEACHER".equalsIgnoreCase(role)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied"));
			}

			String username = jwtService.extractUsername(token);
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("User not found"));

			List<Lesson> notifications = lessonService.getLessonsStartingSoon(user.getId());

			List<LessonResponse> response = notifications.stream().map(this::mapToLessonResponse)
					.collect(Collectors.toList());

			return ResponseEntity.ok(response);

		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Unexpected error occurred", "details", e.getMessage()));
		}
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<?> updateLessonStatus(@PathVariable Long id, @Valid @RequestBody LessonStatusUpdateDto dto,
			@RequestHeader("Authorization") String authHeader) {

		try {
			String token = extractToken(authHeader);
			String role = jwtService.extractRole(token);

			if (token == null || token.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("message", "Missing or invalid token"));
			}

			if (!"TEACHER".equalsIgnoreCase(role)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(Map.of("message", "Access denied: TEACHER role required"));
			}

			String username = jwtService.extractUsername(token);
			User user = userRepository.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("User not found"));

			Lesson lesson = lessonService.updateLessonStatus(id, dto.getStatus(), user.getId());

			return ResponseEntity.ok(mapToLessonResponse(lesson));

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "An unexpected error occurred", "error", e.getMessage()));
		}
	}

}
