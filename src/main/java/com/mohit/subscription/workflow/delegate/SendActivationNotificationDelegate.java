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
 * SendActivationNotificationDelegate class - Flowable JavaDelegate that publishes an activation
 * notification event to Kafka upon subscription completion
 *
 * @author mohit
 */
@Component
public class SendActivationNotificationDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(SendActivationNotificationDelegate.class);

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    /**
     * Executes the delegate — reads subscription and customer details from execution context,
     * builds a SubscriptionStatusEvent for activation notification and publishes it to SW_NOTIFICATION topic.
     *
     * @param execution the Flowable DelegateExecution providing access to process variables
     */
    @Override
    public void execute(DelegateExecution execution) {
        LOG.info("=======SendActivationNotificationDelegate============");
        String subscriptionId = execution.getVariable("subscriptionId") != null ? execution.getVariable("subscriptionId").toString() : "";
        String customerId = execution.getVariable("customerId") != null ? execution.getVariable("customerId").toString() : "";
        String tenantId = execution.getVariable("tenantId") != null ? execution.getVariable("tenantId").toString() : "";
        String subscriptionStatus = execution.getVariable("subscriptionStatus") != null ? execution.getVariable("subscriptionStatus").toString() : Constants.SUBSCRIPTION_STATUS_COMPLETED;
        String processInstanceId = execution.getProcessInstanceId() != null ? execution.getProcessInstanceId() : "";

        LOG.info("SendActivationNotificationDelegate:: notifying customer :: {} for subscriptionId :: {}", customerId, subscriptionId);

        execution.setVariable(Constants.VAR_SUBSCRIPTION_STATUS, Constants.SUBSCRIPTION_STATUS_COMPLETED);

        SubscriptionStatusEvent notificationEvent = new SubscriptionStatusEvent();
        notificationEvent.setSubscriptionId(subscriptionId);
        notificationEvent.setStatus(Constants.SUBSCRIPTION_STATUS_COMPLETED);
        notificationEvent.setCustomerId(customerId);
        notificationEvent.setTenantId(tenantId);
        notificationEvent.setProcessInstanceId(processInstanceId);
        notificationEvent.setTransactionId(execution.getVariable("transactionId") != null ? execution.getVariable("transactionId").toString() : "");
        notificationEvent.setCorrelationId(execution.getVariable("correlationId") != null ? execution.getVariable("correlationId").toString() : "");
        notificationEvent.setMessage("Your subscription " + subscriptionId + " has been " + subscriptionStatus + " successfully.");

        kafkaTemplate.send(Constants.NOTIFICATION_TOPIC, notificationEvent);
        LOG.info("SendActivationNotificationDelegate:: ActivationNotificationEvent published for customerId :: {}", customerId);
    }
}
