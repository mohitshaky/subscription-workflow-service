package com.mohit.subscription.workflow.exception;

import com.mohit.subscription.workflow.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * GlobalExceptionHandler class - Centralized exception handling for all REST controllers
 *
 * @author mohit
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles SubscriptionWorkflowException thrown by service layer
     *
     * @param ex the SubscriptionWorkflowException
     * @return ResponseEntity with ErrorResponse body
     */
    @ExceptionHandler(SubscriptionWorkflowException.class)
    public ResponseEntity<ErrorResponse> handleSubscriptionWorkflowException(SubscriptionWorkflowException ex) {
        LOG.error("Exception in handleSubscriptionWorkflowException: ", ex);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status("ERROR")
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .details(ex.getClass().getSimpleName())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    /**
     * Handles IllegalArgumentException for bad request scenarios
     *
     * @param ex the IllegalArgumentException
     * @return ResponseEntity with ErrorResponse body
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        LOG.error("Exception in handleIllegalArgumentException: ", ex);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status("ERROR")
                .errorCode("SW-400")
                .message(ex.getMessage())
                .details("Bad Request")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all unhandled exceptions as internal server errors
     *
     * @param ex the Exception
     * @return ResponseEntity with ErrorResponse body
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        LOG.error("Exception in handleGenericException: ", ex);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status("ERROR")
                .errorCode("SW-500")
                .message("Internal server error occurred")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
