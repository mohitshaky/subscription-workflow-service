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
 * ProvisionSubscriptionDelegate class - Flowable JavaDelegate that triggers provisioning
 * and updates process variables
 *
 * @author mohit
 */
@Component
public class ProvisionSubscriptionDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisionSubscriptionDelegate.class);

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    /**
     * Executes the delegate — reads subscription variables, triggers provisioning via Kafka event,
     * and updates subscription status to IN_PROGRESS on the process execution context.
     *
     * @param execution the Flowable DelegateExecution providing access to process variables
     */
    @Override
    public void execute(DelegateExecution execution) {
        LOG.info("=======ProvisionSubscriptionDelegate============");
        String subscriptionId = execution.getVariable("subscriptionId") != null ? execution.getVariable("subscriptionId").toString() : "";
        String customerId = execution.getVariable("customerId") != null ? execution.getVariable("customerId").toString() : "";
        String tenantId = execution.getVariable("tenantId") != null ? execution.getVariable("tenantId").toString() : "";
        String processInstanceId = execution.getProcessInstanceId() != null ? execution.getProcessInstanceId() : "";
        LOG.info("ProvisionSubscriptionDelegate:: provisioning subscriptionId :: {} processInstanceId :: {}", subscriptionId, processInstanceId);

        execution.setVariable(Constants.VAR_SUBSCRIPTION_STATUS, Constants.SUBSCRIPTION_STATUS_IN_PROGRESS);

        SubscriptionStatusEvent provisioningEvent = new SubscriptionStatusEvent();
        provisioningEvent.setSubscriptionId(subscriptionId);
        provisioningEvent.setStatus(Constants.SUBSCRIPTION_STATUS_IN_PROGRESS);
        provisioningEvent.setCustomerId(customerId);
        provisioningEvent.setTenantId(tenantId);
        provisioningEvent.setProcessInstanceId(processInstanceId);
        provisioningEvent.setTransactionId(execution.getVariable("transactionId") != null ? execution.getVariable("transactionId").toString() : "");
        provisioningEvent.setCorrelationId(execution.getVariable("correlationId") != null ? execution.getVariable("correlationId").toString() : "");

        kafkaTemplate.send(Constants.SUBSCRIPTION_STATUS_TOPIC, provisioningEvent);
        LOG.info("ProvisionSubscriptionDelegate:: ProvisioningEvent published for subscriptionId :: {}", subscriptionId);
    }
}
