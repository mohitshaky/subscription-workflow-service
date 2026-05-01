package com.mohit.subscription.workflow.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * SubscriptionResponse class - REST API response DTO for subscription process operations
 *
 * @author mohit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SubscriptionResponse {

    private String subscriptionId;
    private String processInstanceId;
    private String status;
    private String message;
    private String transactionId;
    private String correlationId;
}
