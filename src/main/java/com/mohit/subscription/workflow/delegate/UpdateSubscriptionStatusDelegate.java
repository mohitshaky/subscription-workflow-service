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
 * UpdateSubscriptionStatusDelegate class - Flowable JavaDelegate that publishes subscription
 * status events to Kafka
 *
 * @author mohit
 */
@Component
public class UpdateSubscriptionStatusDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateSubscriptionStatusDelegate.class);

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    /**
     * Executes the delegate — reads subscription variables from the process execution context,
     * builds a SubscriptionStatusEvent and publishes it to the SW_SUBSCRIPTION_STATUS Kafka topic.
     *
     * @param execution the Flowable DelegateExecution providing access to process variables
     */
    @Override
    public void execute(DelegateExecution execution) {
        LOG.info("=======UpdateSubscriptionStatusDelegate============");
        SubscriptionStatusEvent event = new SubscriptionStatusEvent();
        event.setSubscriptionId(execution.getVariable("subscriptionId") != null ? execution.getVariable("subscriptionId").toString() : "");
        event.setStatus(execution.getVariable("subscriptionStatus") != null ? execution.getVariable("subscriptionStatus").toString() : "");
        event.setCustomerId(execution.getVariable("customerId") != null ? execution.getVariable("customerId").toString() : "");
        event.setTenantId(execution.getVariable("tenantId") != null ? execution.getVariable("tenantId").toString() : "");
        event.setProcessInstanceId(execution.getProcessInstanceId() != null ? execution.getProcessInstanceId() : "");
        event.setTransactionId(execution.getVariable("transactionId") != null ? execution.getVariable("transactionId").toString() : "");
        event.setCorrelationId(execution.getVariable("correlationId") != null ? execution.getVariable("correlationId").toString() : "");
        // publish to kafka
        kafkaTemplate.send(Constants.SUBSCRIPTION_STATUS_TOPIC, event);
        LOG.info("SubscriptionStatusEvent published :: {}", event);
        execution.setVariable("status", execution.getVariable("subscriptionStatus"));
    }
}
