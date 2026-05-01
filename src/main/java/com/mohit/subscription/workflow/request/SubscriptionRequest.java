package com.mohit.subscription.workflow.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * SubscriptionRequest class - REST API request DTO for starting a subscription process
 *
 * @author mohit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SubscriptionRequest {

    private String subscriptionId;
    private String customerId;
    private String tenantId;
    private String subscriptionType;
    private String productId;
    private String serviceType;
    private Map<String, Object> additionalVariables;
}
