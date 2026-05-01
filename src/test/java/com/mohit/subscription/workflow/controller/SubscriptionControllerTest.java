package com.mohit.subscription.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohit.subscription.workflow.exception.SubscriptionWorkflowException;
import com.mohit.subscription.workflow.request.SubscriptionRequest;
import com.mohit.subscription.workflow.response.SuccessResponse;
import com.mohit.subscription.workflow.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SubscriptionControllerTest class - Unit tests for SubscriptionController REST endpoints
 *
 * @author mohit
 */
@WebMvcTest(SubscriptionController.class)
@WithMockUser(username = "admin", roles = {"USER"})
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionService subscriptionService;

    private SubscriptionRequest subscriptionRequest;
    private SuccessResponse successResponse;

    @BeforeEach
    void setUp() {
        subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setSubscriptionId("SUB-001");
        subscriptionRequest.setCustomerId("CUST-001");
        subscriptionRequest.setTenantId("TENANT-001");
        subscriptionRequest.setSubscriptionType("MOBILE");
        subscriptionRequest.setProductId("PROD-001");

        successResponse = SuccessResponse.builder()
                .status("SUCCESS")
                .message("Subscription process started successfully")
                .data(null)
                .build();
    }

    @Test
    @DisplayName("POST /subscription/start/{processKey} - should return 200 when process started successfully")
    void startSubscriptionProcess_success() throws Exception {
        when(subscriptionService.startSubscriptionProcess(anyString(), any(SubscriptionRequest.class), anyString(), anyString()))
                .thenReturn(successResponse);

        mockMvc.perform(post("/subscription/start/subscriptionFulfillmentProcess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("transactionId", "TXN-001")
                        .header("correlationId", "CORR-001")
                        .header("sourceChannel", "WEB")
                        .header("tenantId", "TENANT-001")
                        .content(objectMapper.writeValueAsString(subscriptionRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Subscription process started successfully"));
    }

    @Test
    @DisplayName("POST /subscription/start/{processKey} - should return 500 when service throws exception")
    void startSubscriptionProcess_serviceException() throws Exception {
        when(subscriptionService.startSubscriptionProcess(anyString(), any(SubscriptionRequest.class), anyString(), anyString()))
                .thenThrow(new SubscriptionWorkflowException("Failed to start process",
                        HttpStatus.INTERNAL_SERVER_ERROR, "SW-002"));

        mockMvc.perform(post("/subscription/start/subscriptionFulfillmentProcess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("transactionId", "TXN-001")
                        .header("correlationId", "CORR-001")
                        .header("sourceChannel", "WEB")
                        .header("tenantId", "TENANT-001")
                        .content(objectMapper.writeValueAsString(subscriptionRequest)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("SW-002"));
    }

    @Test
    @DisplayName("PATCH /subscription/task/{taskId}/complete - should return 200 when task completed successfully")
    void completeTask_success() throws Exception {
        SuccessResponse taskResponse = SuccessResponse.builder()
                .status("SUCCESS")
                .message("Task completed successfully")
                .data(null)
                .build();
        when(subscriptionService.completeTask(anyString(), any())).thenReturn(taskResponse);

        Map<String, Object> variables = new HashMap<>();
        variables.put("subscriptionStatus", "PROVISIONED");

        mockMvc.perform(patch("/subscription/task/TASK-001/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("transactionId", "TXN-001")
                        .header("correlationId", "CORR-001")
                        .header("sourceChannel", "WEB")
                        .header("tenantId", "TENANT-001")
                        .content(objectMapper.writeValueAsString(variables)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Task completed successfully"));
    }

    @Test
    @DisplayName("PATCH /subscription/task/{taskId}/complete - should return 404 when task not found")
    void completeTask_notFound() throws Exception {
        when(subscriptionService.completeTask(anyString(), any()))
                .thenThrow(new SubscriptionWorkflowException("Task not found", HttpStatus.NOT_FOUND, "SW-003"));

        mockMvc.perform(patch("/subscription/task/INVALID-TASK/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("transactionId", "TXN-001")
                        .header("correlationId", "CORR-001")
                        .header("sourceChannel", "WEB")
                        .header("tenantId", "TENANT-001"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("SW-003"));
    }

    @Test
    @DisplayName("PATCH /subscription/{processInstanceId}/{signalName}/signal - should return 200 when signal sent successfully")
    void sendSignal_success() throws Exception {
        SuccessResponse signalResponse = SuccessResponse.builder()
                .status("SUCCESS")
                .message("Signal sent successfully")
                .data(null)
                .build();
        when(subscriptionService.sendSignalToProcess(anyString(), anyString(), any())).thenReturn(signalResponse);

        mockMvc.perform(patch("/subscription/PI-001/provisioningComplete/signal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("transactionId", "TXN-001")
                        .header("correlationId", "CORR-001")
                        .header("sourceChannel", "WEB")
                        .header("tenantId", "TENANT-001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Signal sent successfully"));
    }

    @Test
    @DisplayName("PATCH /subscription/{processInstanceId}/{signalName}/signal - should return 500 when signal fails")
    void sendSignal_serviceException() throws Exception {
        when(subscriptionService.sendSignalToProcess(anyString(), anyString(), any()))
                .thenThrow(new SubscriptionWorkflowException("Signal failed",
                        HttpStatus.INTERNAL_SERVER_ERROR, "SW-004"));

        mockMvc.perform(patch("/subscription/PI-INVALID/someSignal/signal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("transactionId", "TXN-001")
                        .header("correlationId", "CORR-001")
                        .header("sourceChannel", "WEB")
                        .header("tenantId", "TENANT-001"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("SW-004"));
    }
}
