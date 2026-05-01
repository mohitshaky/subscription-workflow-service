package com.mohit.subscription.workflow.exception;

import org.springframework.http.HttpStatus;

/**
 * SubscriptionWorkflowException class - Custom runtime exception for subscription workflow errors
 *
 * @author mohit
 */
public class SubscriptionWorkflowException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    /**
     * Constructor with message, httpStatus and errorCode
     *
     * @param message   error message
     * @param httpStatus HTTP status code
     * @param errorCode  application error code
     */
    public SubscriptionWorkflowException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    /**
     * Constructor with message, cause, httpStatus and errorCode
     *
     * @param message    error message
     * @param cause      root cause
     * @param httpStatus HTTP status code
     * @param errorCode  application error code
     */
    public SubscriptionWorkflowException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    /**
     * Gets the HTTP status associated with this exception
     *
     * @return HttpStatus
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * Gets the application-specific error code
     *
     * @return errorCode string
     */
    public String getErrorCode() {
        return errorCode;
    }
}
