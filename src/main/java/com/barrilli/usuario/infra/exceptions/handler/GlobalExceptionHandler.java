package com.barrilli.usuario.infra.exceptions.handler;

import com.barrilli.usuario.infra.exceptions.ConflictException;
import com.barrilli.usuario.infra.exceptions.IllegalArgumentException;
import com.barrilli.usuario.infra.exceptions.ResourceNotFoundException;
import com.barrilli.usuario.infra.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
                                                                HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex,
                                                        HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        var message = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(msg -> msg != null && !msg.isBlank())
                .findFirst()
                .orElse("Dados inválidos.");

        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex,
                                                       HttpServletRequest request) {
        log.error("Erro não tratado", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno. Tente novamente mais tarde.", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                               HttpServletRequest request) {
        log.error("Erro não tratado", ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex,
                                                            HttpServletRequest request) {
        log.error("Erro de autenticação", ex);
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message,
                                                        HttpServletRequest request) {
        var body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
