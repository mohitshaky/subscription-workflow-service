package com.mohit.subscription.workflow.service;

import com.mohit.subscription.workflow.exception.SubscriptionWorkflowException;
import com.mohit.subscription.workflow.request.SubscriptionRequest;
import com.mohit.subscription.workflow.response.SubscriptionResponse;
import com.mohit.subscription.workflow.response.SuccessResponse;
import com.mohit.subscription.workflow.wrapper.ISubscriptionWorkflowWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SubscriptionServiceTest class - Unit tests for SubscriptionService business logic
 *
 * @author mohit
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Mock
    private ISubscriptionWorkflowWrapper subscriptionWorkflowWrapper;

    private SubscriptionRequest subscriptionRequest;
    private SubscriptionResponse subscriptionResponse;

    @BeforeEach
    void setUp() {
        subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setSubscriptionId("SUB-001");
        subscriptionRequest.setCustomerId("CUST-001");
        subscriptionRequest.setTenantId("TENANT-001");
        subscriptionRequest.setSubscriptionType("MOBILE");

        subscriptionResponse = new SubscriptionResponse();
        subscriptionResponse.setSubscriptionId("SUB-001");
        subscriptionResponse.setProcessInstanceId("PI-001");
        subscriptionResponse.setStatus("RECEIVED");
        subscriptionResponse.setMessage("Subscription process started successfully");
    }

    @Test
    @DisplayName("startSubscriptionProcess - should return SuccessResponse when wrapper returns SubscriptionResponse")
    void startSubscriptionProcess_success() {
        when(subscriptionWorkflowWrapper.startSubscriptionProcess(anyString(), any(SubscriptionRequest.class), anyString(), anyString()))
                .thenReturn(subscriptionResponse);

        SuccessResponse result = subscriptionService.startSubscriptionProcess(
                "subscriptionFulfillmentProcess", subscriptionRequest, "TXN-001", "CORR-001");

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("Subscription process started successfully", result.getMessage());
        assertNotNull(result.getData());
        verify(subscriptionWorkflowWrapper, times(1)).startSubscriptionProcess(
                "subscriptionFulfillmentProcess", subscriptionRequest, "TXN-001", "CORR-001");
    }

    @Test
    @DisplayName("startSubscriptionProcess - should propagate SubscriptionWorkflowException from wrapper")
    void startSubscriptionProcess_wrapperThrowsException() {
        when(subscriptionWorkflowWrapper.startSubscriptionProcess(anyString(), any(SubscriptionRequest.class), anyString(), anyString()))
                .thenThrow(new SubscriptionWorkflowException("Process start failed",
                        HttpStatus.INTERNAL_SERVER_ERROR, "SW-002"));

        assertThrows(SubscriptionWorkflowException.class, () ->
                subscriptionService.startSubscriptionProcess("subscriptionFulfillmentProcess", subscriptionRequest, "TXN-001", "CORR-001"));

        verify(subscriptionWorkflowWrapper, times(1)).startSubscriptionProcess(
                anyString(), any(SubscriptionRequest.class), anyString(), anyString());
    }

    @Test
    @DisplayName("completeTask - should return SuccessResponse when task completed successfully")
    void completeTask_success() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("subscriptionStatus", "PROVISIONED");
        doNothing().when(subscriptionWorkflowWrapper).completeTask(anyString(), any());

        SuccessResponse result = subscriptionService.completeTask("TASK-001", variables);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("Task completed successfully", result.getMessage());
        verify(subscriptionWorkflowWrapper, times(1)).completeTask("TASK-001", variables);
    }

    @Test
    @DisplayName("completeTask - should return SuccessResponse with null variables")
    void completeTask_nullVariables() {
        doNothing().when(subscriptionWorkflowWrapper).completeTask(anyString(), any());

        SuccessResponse result = subscriptionService.completeTask("TASK-001", null);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
    }

    @Test
    @DisplayName("completeTask - should propagate SubscriptionWorkflowException when task not found")
    void completeTask_notFound() {
        doThrow(new SubscriptionWorkflowException("Task not found", HttpStatus.NOT_FOUND, "SW-003"))
                .when(subscriptionWorkflowWrapper).completeTask(anyString(), any());

        assertThrows(SubscriptionWorkflowException.class, () ->
                subscriptionService.completeTask("INVALID-TASK", null));
    }

    @Test
    @DisplayName("sendSignalToProcess - should return SuccessResponse when signal sent successfully")
    void sendSignalToProcess_success() {
        Map<String, Object> variables = new HashMap<>();
        doNothing().when(subscriptionWorkflowWrapper).sendSignalToProcess(anyString(), anyString(), any());

        SuccessResponse result = subscriptionService.sendSignalToProcess("PI-001", "provisioningComplete", variables);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("Signal sent successfully", result.getMessage());
        verify(subscriptionWorkflowWrapper, times(1)).sendSignalToProcess("PI-001", "provisioningComplete", variables);
    }

    @Test
    @DisplayName("sendSignalToProcess - should propagate exception when signal fails")
    void sendSignalToProcess_exception() {
        doThrow(new SubscriptionWorkflowException("Signal failed", HttpStatus.INTERNAL_SERVER_ERROR, "SW-004"))
                .when(subscriptionWorkflowWrapper).sendSignalToProcess(anyString(), anyString(), any());

        assertThrows(SubscriptionWorkflowException.class, () ->
                subscriptionService.sendSignalToProcess("PI-INVALID", "someSignal", null));
    }

    @Test
    @DisplayName("startSubscriptionProcess - should pass all parameters correctly to wrapper")
    void startSubscriptionProcess_parametersPassedCorrectly() {
        when(subscriptionWorkflowWrapper.startSubscriptionProcess("subscriptionFulfillmentProcess", subscriptionRequest, "TXN-999", "CORR-999"))
                .thenReturn(subscriptionResponse);

        subscriptionService.startSubscriptionProcess("subscriptionFulfillmentProcess", subscriptionRequest, "TXN-999", "CORR-999");

        verify(subscriptionWorkflowWrapper, times(1))
                .startSubscriptionProcess("subscriptionFulfillmentProcess", subscriptionRequest, "TXN-999", "CORR-999");
    }
}
