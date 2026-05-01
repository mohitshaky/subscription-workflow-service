package com.mohit.subscription.workflow.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohit.subscription.workflow.constants.Constants;
import com.mohit.subscription.workflow.model.SubscriptionStatusEvent;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UpdateSubscriptionStatusDelegateTest class - Unit tests for UpdateSubscriptionStatusDelegate Flowable delegate
 *
 * @author mohit
 */
@ExtendWith(MockitoExtension.class)
class UpdateSubscriptionStatusDelegateTest {

    @InjectMocks
    private UpdateSubscriptionStatusDelegate updateSubscriptionStatusDelegate;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DelegateExecution execution;

    @BeforeEach
    void setUp() {
        when(execution.getVariable("subscriptionId")).thenReturn("SUB-001");
        when(execution.getVariable("subscriptionStatus")).thenReturn("PROVISIONED");
        when(execution.getVariable("customerId")).thenReturn("CUST-001");
        when(execution.getVariable("tenantId")).thenReturn("TENANT-001");
        when(execution.getProcessInstanceId()).thenReturn("PI-001");
        when(execution.getVariable("transactionId")).thenReturn("TXN-001");
        when(execution.getVariable("correlationId")).thenReturn("CORR-001");
    }

    @Test
    @DisplayName("execute - should publish SubscriptionStatusEvent to SW_SUBSCRIPTION_STATUS topic")
    void execute_publishesKafkaEvent() {
        updateSubscriptionStatusDelegate.execute(execution);

        verify(kafkaTemplate, times(1)).send(eq(Constants.SUBSCRIPTION_STATUS_TOPIC), any(SubscriptionStatusEvent.class));
    }

    @Test
    @DisplayName("execute - should set subscriptionId correctly from execution variable")
    void execute_setsSubscriptionIdCorrectly() {
        ArgumentCaptor<SubscriptionStatusEvent> eventCaptor = ArgumentCaptor.forClass(SubscriptionStatusEvent.class);

        updateSubscriptionStatusDelegate.execute(execution);

        verify(kafkaTemplate).send(eq(Constants.SUBSCRIPTION_STATUS_TOPIC), eventCaptor.capture());
        SubscriptionStatusEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals("SUB-001", capturedEvent.getSubscriptionId());
    }

    @Test
    @DisplayName("execute - should set status correctly from subscriptionStatus execution variable")
    void execute_setsStatusCorrectly() {
        ArgumentCaptor<SubscriptionStatusEvent> eventCaptor = ArgumentCaptor.forClass(SubscriptionStatusEvent.class);

        updateSubscriptionStatusDelegate.execute(execution);

        verify(kafkaTemplate).send(eq(Constants.SUBSCRIPTION_STATUS_TOPIC), eventCaptor.capture());
        SubscriptionStatusEvent capturedEvent = eventCaptor.getValue();
        assertEquals("PROVISIONED", capturedEvent.getStatus());
    }

    @Test
    @DisplayName("execute - should set status variable on execution after publishing")
    void execute_setsStatusVariableOnExecution() {
        updateSubscriptionStatusDelegate.execute(execution);

        verify(execution, times(1)).setVariable(eq("status"), any());
    }

    @Test
    @DisplayName("execute - should use empty string when subscriptionId variable is null")
    void execute_nullSubscriptionId_usesEmptyString() {
        when(execution.getVariable("subscriptionId")).thenReturn(null);
        ArgumentCaptor<SubscriptionStatusEvent> eventCaptor = ArgumentCaptor.forClass(SubscriptionStatusEvent.class);

        updateSubscriptionStatusDelegate.execute(execution);

        verify(kafkaTemplate).send(eq(Constants.SUBSCRIPTION_STATUS_TOPIC), eventCaptor.capture());
        assertEquals("", eventCaptor.getValue().getSubscriptionId());
    }
}
