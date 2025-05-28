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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "http://localhost:8081")
@Tag(name = "Lessons", description = "Endpoints for managing teacher lessons")
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

	@Operation(
		summary = "Create a new lesson",
		description = "Allows a teacher to create a new lesson",
		responses = {
			@ApiResponse(responseCode = "201", description = "Lesson created successfully"),
			@ApiResponse(responseCode = "403", description = "User does not have TEACHER role"),
			@ApiResponse(responseCode = "401", description = "Invalid or expired token"),
			@ApiResponse(responseCode = "400", description = "Invalid request data")
		}
	)
	@PostMapping
	public ResponseEntity<?> createLesson(
		@Valid @RequestBody CreateLessonDto dto,
		@RequestHeader("Authorization") String authHeader) {

		try {
			String token = extractToken(authHeader);
			String role = jwtService.extractRole(token);

			if (!"TEACHER".equalsIgnoreCase(role)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "User does not have TEACHER role"));
			}

			String username = jwtService.extractUsername(token);
			User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found"));

			Lesson lesson = lessonService.createLesson(dto, user.getId());

			return ResponseEntity.status(HttpStatus.CREATED).body(mapToLessonResponse(lesson));

		} catch (Exception e) {
			return handleJwtException(e);
		}
	}

	@Operation(
		summary = "Get lessons for the week",
		description = "Fetch all lessons for a teacher in a given week starting from a specified date",
		parameters = {
			@Parameter(name = "weekStartDate", description = "Start date of the week (yyyy-MM-dd)")
		},
		responses = {
			@ApiResponse(responseCode = "200", description = "Weekly lessons fetched successfully"),
			@ApiResponse(responseCode = "401", description = "Invalid or expired token"),
			@ApiResponse(responseCode = "403", description = "User does not have TEACHER role")
		}
	)
	@GetMapping("/week")
	public ResponseEntity<?> getWeeklyTimetable(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
		@RequestHeader("Authorization") String authHeader) {

		try {
			String token = extractToken(authHeader);
			String role = jwtService.extractRole(token);

			if (!"TEACHER".equalsIgnoreCase(role)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "User does not have TEACHER role"));
			}

			String username = jwtService.extractUsername(token);
			User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found"));

			List<Lesson> lessons = lessonService.getWeeklyTimetable(user.getId(), weekStartDate);
			List<LessonResponse> response = lessons.stream().map(this::mapToLessonResponse).toList();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return handleJwtException(e);
		}
	}

	@Operation(summary = "Get lesson by ID", description = "Fetch a single lesson using its ID")
	@GetMapping("/{id}")
	public ResponseEntity<?> getLessonById(@PathVariable Long id) {
		try {
			Lesson lesson = lessonService.getLessonById(id);
			return ResponseEntity.ok(mapToLessonResponse(lesson));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
		}
	}

	@Operation(summary = "Update a lesson", description = "Update an existing lesson as a teacher")
	@PutMapping("/{id}")
	public ResponseEntity<?> updateLesson(
		@PathVariable Long id,
		@Valid @RequestBody CreateLessonDto dto,
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

	@Operation(summary = "Delete a lesson", description = "Delete a specific lesson by ID")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteLesson(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {

		try {
			String token = extractToken(authHeader);
			String role = jwtService.extractRole(token);
			String username = jwtService.extractUsername(token);

			if (!"TEACHER".equalsIgnoreCase(role)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied"));
			}

			User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found"));

			lessonService.deleteLesson(id, user.getId());

			return ResponseEntity.ok(Map.of("message", "Lesson deleted successfully"));

		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		}
	}

	@Operation(summary = "Get all lessons", description = "Retrieve all lessons created by a teacher")
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
			List<LessonResponse> response = lessons.stream().map(this::mapToLessonResponse).toList();

			return ResponseEntity.ok(response);

		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		}
	}

	@Operation(summary = "Get upcoming lesson notifications", description = "Return lessons starting soon for the teacher")
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
			List<LessonResponse> response = notifications.stream().map(this::mapToLessonResponse).toList();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("message", "Unexpected error occurred", "details", e.getMessage()));
		}
	}

	@Operation(summary = "Update lesson status", description = "Patch the status of a lesson by ID")
	@PatchMapping("/{id}/status")
	public ResponseEntity<?> updateLessonStatus(
		@PathVariable Long id,
		@Valid @RequestBody LessonStatusUpdateDto dto,
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

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("message", "An unexpected error occurred", "error", e.getMessage()));
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

	private ResponseEntity<Map<String, String>> handleJwtException(Exception e) {
		if (e instanceof io.jsonwebtoken.ExpiredJwtException) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token expired"));
		}
		if (e instanceof io.jsonwebtoken.JwtException) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid token"));
		}
		if (e instanceof IllegalArgumentException) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Unexpected error occurred", "details", e.getMessage()));
	}
}
