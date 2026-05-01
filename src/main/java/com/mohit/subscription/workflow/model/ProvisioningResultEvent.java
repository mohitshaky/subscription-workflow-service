package com.mohit.subscription.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ProvisioningResultEvent class - Kafka event model for provisioning result updates
 *
 * @author mohit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProvisioningResultEvent {

    private String subscriptionId;
    private String provisioningStatus;
    private String customerId;
    private String tenantId;
    private String processInstanceId;
    private String transactionId;
    private String correlationId;
    private String errorCode;
    private String errorMessage;
    private String timestamp;
}
