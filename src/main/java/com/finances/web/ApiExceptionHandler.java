package com.finances.web;

import com.finances.backup.BackupException;
import com.finances.formula.FormulaException;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(basePackages = "com.finances.web")
class ApiExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, FormulaException.class, BackupException.class,
            MethodArgumentTypeMismatchException.class})
    ResponseEntity<String> badRequest(Exception e) {
        return text(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    ResponseEntity<String> invalidForm(BindException e) {
        var message = e.getFieldErrors().stream()
                .map((FieldError error) -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return text(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<String> notFound(NoSuchElementException e) {
        return text(HttpStatus.NOT_FOUND, e.getMessage());
    }

    private static ResponseEntity<String> text(HttpStatus status, String message) {
        return ResponseEntity.status(status).contentType(MediaType.TEXT_PLAIN).body(message);
    }
}
