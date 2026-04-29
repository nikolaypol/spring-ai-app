package com.example.springai;

import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(NonTransientAiException.class)
    ResponseEntity<ApiError> handleNonTransientAiException(NonTransientAiException exception) {
        if (isQuotaError(exception)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ApiError(
                    "openai_quota_exceeded",
                    "OpenAI API quota is exhausted. Check billing, credits, and project limits in OpenAI Platform."
                ));
        }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(new ApiError(
                "ai_request_failed",
                "OpenAI rejected the request. Check API key, project permissions, model access, and billing."
            ));
    }

    @ExceptionHandler(TransientAiException.class)
    ResponseEntity<ApiError> handleTransientAiException() {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(new ApiError(
                "ai_service_unavailable",
                "The AI service is temporarily unavailable. Try again later."
            ));
    }

    private boolean isQuotaError(RuntimeException exception) {
        String message = exception.getMessage();
        return message != null
            && (message.contains("insufficient_quota") || message.contains("exceeded your current quota"));
    }

    record ApiError(String code, String message) {
    }
}
