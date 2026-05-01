package com.mohit.subscription.workflow.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohit.subscription.workflow.constants.Constants;
import com.mohit.subscription.workflow.model.SubscriptionStatusEvent;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * ValidateSubscriptionDelegate class - Flowable JavaDelegate that validates subscription data
 * and publishes a validation event to Kafka
 *
 * @author mohit
 */
@Component
public class ValidateSubscriptionDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateSubscriptionDelegate.class);

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    /**
     * Executes the delegate — validates subscription details from the process execution context
     * and publishes a validation status event to the SW_VALIDATION_STATUS Kafka topic.
     *
     * @param execution the Flowable DelegateExecution providing access to process variables
     */
    @Override
    public void execute(DelegateExecution execution) {
        LOG.info("=======ValidateSubscriptionDelegate============");
        String subscriptionId = execution.getVariable("subscriptionId") != null ? execution.getVariable("subscriptionId").toString() : "";
        String customerId = execution.getVariable("customerId") != null ? execution.getVariable("customerId").toString() : "";
        String tenantId = execution.getVariable("tenantId") != null ? execution.getVariable("tenantId").toString() : "";
        LOG.info("ValidateSubscriptionDelegate:: validating subscriptionId :: {} for customerId :: {}", subscriptionId, customerId);

        // set validated status on process variables
        execution.setVariable(Constants.VAR_SUBSCRIPTION_STATUS, Constants.SUBSCRIPTION_STATUS_VALIDATED);

        SubscriptionStatusEvent validationEvent = new SubscriptionStatusEvent();
        validationEvent.setSubscriptionId(subscriptionId);
        validationEvent.setStatus(Constants.SUBSCRIPTION_STATUS_VALIDATED);
        validationEvent.setCustomerId(customerId);
        validationEvent.setTenantId(tenantId);
        validationEvent.setProcessInstanceId(execution.getProcessInstanceId() != null ? execution.getProcessInstanceId() : "");
        validationEvent.setTransactionId(execution.getVariable("transactionId") != null ? execution.getVariable("transactionId").toString() : "");
        validationEvent.setCorrelationId(execution.getVariable("correlationId") != null ? execution.getVariable("correlationId").toString() : "");

        kafkaTemplate.send(Constants.VALIDATION_STATUS_TOPIC, validationEvent);
        LOG.info("ValidationEvent published for subscriptionId :: {}", subscriptionId);
    }
}
