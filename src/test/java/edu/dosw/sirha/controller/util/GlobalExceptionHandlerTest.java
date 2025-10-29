package edu.dosw.sirha.controller.util;

import edu.dosw.sirha.controller.utils.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        lenient().when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with validation error")
    void shouldHandleIllegalArgumentException_ValidationError() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input data");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleIllegalArgumentException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation Error", response.getBody().getError());
        assertEquals("Invalid input data", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with unauthorized message")
    void shouldHandleIllegalArgumentException_Unauthorized() {
        IllegalArgumentException exception = new IllegalArgumentException("Usuario no autenticado");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleIllegalArgumentException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with forbidden message")
    void shouldHandleIllegalArgumentException_Forbidden() {
        IllegalArgumentException exception = new IllegalArgumentException("No tienes permisos para esta acción");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleIllegalArgumentException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with not found message")
    void shouldHandleIllegalArgumentException_NotFound() {
        IllegalArgumentException exception = new IllegalArgumentException("Resource not found");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleIllegalArgumentException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with no encontrado message")
    void shouldHandleIllegalArgumentException_NoEncontrado() {
        IllegalArgumentException exception = new IllegalArgumentException("Recurso no encontrado");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleIllegalArgumentException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with session not found message")
    void shouldHandleIllegalArgumentException_SessionNotFound() {
        IllegalArgumentException exception = new IllegalArgumentException("Session not found");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleIllegalArgumentException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with null message")
    void shouldHandleIllegalArgumentException_NullMessage() {
        IllegalArgumentException exception = new IllegalArgumentException();

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleIllegalArgumentException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Should handle DataAccessException")
    void shouldHandleDataAccessException() {
        DataAccessException exception = new DataAccessException("Database connection failed") {};

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleDataAccessException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Database Error", response.getBody().getError());
        assertEquals("An error occurred while accessing the database. Please try again later.",
                response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    @DisplayName("Should handle EmptyResultDataAccessException")
    void shouldHandleEmptyResultDataAccessException() {
        EmptyResultDataAccessException exception = new EmptyResultDataAccessException(1);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleEmptyResultDataAccessException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Entity Not Found", response.getBody().getError());
        assertEquals("The requested entity was not found and cannot be deleted.",
                response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle DateTimeParseException")
    void shouldHandleDateTimeParseException() {
        DateTimeParseException exception = new DateTimeParseException("Invalid date", "2023-13-45", 0);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleDateTimeParseException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Date Format Error", response.getBody().getError());
        assertEquals("Invalid date format. Expected format: yyyy-MM-dd",
                response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle NullPointerException")
    void shouldHandleNullPointerException() {
        NullPointerException exception = new NullPointerException("Null value encountered");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleNullPointerException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred. Please contact support.",
                response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle RuntimeException")
    void shouldHandleRuntimeException() {
        RuntimeException exception = new RuntimeException("Unexpected runtime error");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleRuntimeException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Runtime Error", response.getBody().getError());
        assertEquals("An unexpected error occurred while processing your request.",
                response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with single field error")
    void shouldHandleMethodArgumentNotValidException_SingleError() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "Field is required");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleValidationException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation Error", response.getBody().getError());
        assertEquals("Field is required", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with multiple field errors")
    void shouldHandleMethodArgumentNotValidException_MultipleErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error1 = new FieldError("object", "field1", "Field1 is required");
        FieldError error2 = new FieldError("object", "field2", "Field2 must be positive");
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(error1, error2));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleValidationException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Field1 is required"));
        assertTrue(response.getBody().getMessage().contains("Field2 must be positive"));
        assertTrue(response.getBody().getMessage().contains(";"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with no errors")
    void shouldHandleMethodArgumentNotValidException_NoErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleValidationException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation error", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle DuplicateKeyException")
    void shouldHandleDuplicateKeyException() {
        DuplicateKeyException exception = new DuplicateKeyException("Duplicate key error: email already exists");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleDuplicateKeyException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Duplicate Resource", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void shouldHandleGenericException() {
        Exception exception = new Exception("Unexpected error");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleGenericException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred. Please contact support.",
                response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should extract path from WebRequest correctly")
    void shouldExtractPathCorrectly() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/users/123");

        IllegalArgumentException exception = new IllegalArgumentException("Test error");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleIllegalArgumentException(exception, webRequest);

        assertEquals("/api/users/123", response.getBody().getPath());
    }

    @Test
    @DisplayName("Should handle WebRequest without uri prefix")
    void shouldHandleWebRequestWithoutUriPrefix() {
        when(webRequest.getDescription(false)).thenReturn("/api/custom/path");

        IllegalArgumentException exception = new IllegalArgumentException("Test error");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleIllegalArgumentException(exception, webRequest);

        assertEquals("/api/custom/path", response.getBody().getPath());
    }

    @Test
    @DisplayName("ErrorResponse builder should work correctly")
    void errorResponseBuilder_ShouldWorkCorrectly() {
        GlobalExceptionHandler.ErrorResponse errorResponse = GlobalExceptionHandler.ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(400)
                .error("Test Error")
                .message("Test message")
                .path("/test/path")
                .build();

        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Test Error", errorResponse.getError());
        assertEquals("Test message", errorResponse.getMessage());
        assertEquals("/test/path", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("ErrorResponse no-args constructor should work")
    void errorResponseNoArgsConstructor_ShouldWork() {
        GlobalExceptionHandler.ErrorResponse errorResponse = new GlobalExceptionHandler.ErrorResponse();

        assertNotNull(errorResponse);
        assertNull(errorResponse.getTimestamp());
        assertEquals(0, errorResponse.getStatus());
    }

    @Test
    @DisplayName("ErrorResponse all-args constructor should work")
    void errorResponseAllArgsConstructor_ShouldWork() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        GlobalExceptionHandler.ErrorResponse errorResponse = new GlobalExceptionHandler.ErrorResponse(
                now, 404, "Not Found", "Resource not found", "/api/resource"
        );

        assertNotNull(errorResponse);
        assertEquals(now, errorResponse.getTimestamp());
        assertEquals(404, errorResponse.getStatus());
        assertEquals("Not Found", errorResponse.getError());
        assertEquals("Resource not found", errorResponse.getMessage());
        assertEquals("/api/resource", errorResponse.getPath());
    }

    @Test
    @DisplayName("ErrorResponse setters should work")
    void errorResponseSetters_ShouldWork() {
        GlobalExceptionHandler.ErrorResponse errorResponse = new GlobalExceptionHandler.ErrorResponse();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        errorResponse.setTimestamp(now);
        errorResponse.setStatus(500);
        errorResponse.setError("Server Error");
        errorResponse.setMessage("Internal error");
        errorResponse.setPath("/api/error");

        assertEquals(now, errorResponse.getTimestamp());
        assertEquals(500, errorResponse.getStatus());
        assertEquals("Server Error", errorResponse.getError());
        assertEquals("Internal error", errorResponse.getMessage());
        assertEquals("/api/error", errorResponse.getPath());
    }

    @Test
    @DisplayName("Should verify WebRequest is called for all exceptions")
    void shouldVerifyWebRequestCalled() {
        IllegalArgumentException exception = new IllegalArgumentException("Test");

        exceptionHandler.handleIllegalArgumentException(exception, webRequest);

        verify(webRequest, atLeastOnce()).getDescription(false);
    }

    @Test
    @DisplayName("Should handle case-insensitive permission messages")
    void shouldHandleCaseInsensitivePermissionMessages() {
        IllegalArgumentException exception1 = new IllegalArgumentException("NO TIENES PERMISOS");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response1 =
                exceptionHandler.handleIllegalArgumentException(exception1, webRequest);
        assertEquals(HttpStatus.FORBIDDEN, response1.getStatusCode());

        IllegalArgumentException exception2 = new IllegalArgumentException("no tienes permisos");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response2 =
                exceptionHandler.handleIllegalArgumentException(exception2, webRequest);
        assertEquals(HttpStatus.FORBIDDEN, response2.getStatusCode());

        IllegalArgumentException exception3 = new IllegalArgumentException("No Tienes Permisos");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response3 =
                exceptionHandler.handleIllegalArgumentException(exception3, webRequest);
        assertEquals(HttpStatus.FORBIDDEN, response3.getStatusCode());
    }
}