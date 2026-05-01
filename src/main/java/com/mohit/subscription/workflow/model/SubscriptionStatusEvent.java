package com.mohit.subscription.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * SubscriptionStatusEvent class - Kafka event model for subscription status updates
 *
 * @author mohit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SubscriptionStatusEvent {

    private String subscriptionId;
    private String status;
    private String customerId;
    private String tenantId;
    private String processInstanceId;
    private String transactionId;
    private String correlationId;
    private String message;
    private String timestamp;
}
