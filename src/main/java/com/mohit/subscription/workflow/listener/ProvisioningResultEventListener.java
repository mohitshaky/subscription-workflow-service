package com.mohit.subscription.workflow.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohit.subscription.workflow.constants.Constants;
import com.mohit.subscription.workflow.model.ProcessInstanceDetails;
import com.mohit.subscription.workflow.model.ProvisioningResultEvent;
import lombok.AllArgsConstructor;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ProvisioningResultEventListener class - Kafka listener for SW_PROVISIONING_RESULT topic events.
 * Advances Flowable user tasks upon receiving provisioning result events from downstream systems.
 *
 * @author mohit
 */
@Component
@AllArgsConstructor
@KafkaListener(topics = Constants.PROVISIONING_RESULT_TOPIC,
        concurrency = "#{${provisioningResultConcurrency}}",
        groupId = "SW_PROVISIONING_RESULT_GRP",
        containerFactory = "provisioningResultListenerFactory")
public class ProvisioningResultEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisioningResultEventListener.class);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    @Qualifier("primaryMongoTemplate")
    MongoTemplate mongoTemplate;

    @Autowired
    TaskService taskService;

    @Autowired
    RuntimeService runtimeService;

    /**
     * Handles incoming ProvisioningResultEvent messages from SW_PROVISIONING_RESULT topic.
     * Completes the waiting Flowable user task to advance the subscription fulfillment process.
     *
     * @param provisioningResultEvent the deserialized ProvisioningResultEvent payload
     */
    @KafkaHandler
    public void listen(@Payload ProvisioningResultEvent provisioningResultEvent) {
        LOG.debug("ProvisioningResultEvent listener {} {}", provisioningResultEvent.getSubscriptionId(), provisioningResultEvent.getProvisioningStatus());
        LOG.info("ProvisioningResultEventListener:: listen method started for subscriptionId :: {}", provisioningResultEvent.getSubscriptionId());
        try {
            String processInstanceId = getProcessInstanceId(provisioningResultEvent.getSubscriptionId());
            if (processInstanceId != null && !processInstanceId.isEmpty()) {
                completeProvisioningTask(processInstanceId, provisioningResultEvent);
                updateProcessInstanceStatus(provisioningResultEvent.getSubscriptionId(), provisioningResultEvent.getProvisioningStatus());
            } else {
                LOG.error("Exception in listen: processInstanceId not found for subscriptionId :: {}",
                        provisioningResultEvent.getSubscriptionId());
            }
        } catch (Exception e) {
            LOG.error("Exception in listen: ", e);
        }
    }

    /**
     * Completes the active Flowable user task for a given processInstanceId with provisioning variables.
     *
     * @param processInstanceId the Flowable process instance identifier
     * @param event             the ProvisioningResultEvent containing status and metadata
     */
    private void completeProvisioningTask(String processInstanceId, ProvisioningResultEvent event) {
        LOG.info("ProvisioningResultEventListener:: completeProvisioningTask method started for processInstanceId :: {}", processInstanceId);
        try {
            Task task = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .active()
                    .singleResult();
            if (task != null) {
                Map<String, Object> variables = new HashMap<>();
                variables.put(Constants.VAR_SUBSCRIPTION_STATUS, event.getProvisioningStatus() != null ? event.getProvisioningStatus() : "");
                variables.put(Constants.VAR_STATUS, event.getProvisioningStatus() != null ? event.getProvisioningStatus() : "");
                taskService.complete(task.getId(), variables);
                LOG.info("ProvisioningResultEventListener:: provisioning task completed :: {} for processInstanceId :: {}",
                        task.getId(), processInstanceId);
            } else {
                LOG.info("ProvisioningResultEventListener:: no active task found for processInstanceId :: {}", processInstanceId);
            }
        } catch (Exception e) {
            LOG.error("Exception in completeProvisioningTask: ", e);
        }
    }

    /**
     * Queries MongoDB using MongoTemplate + Criteria pattern to find the processInstanceId
     * for a given subscriptionId.
     *
     * @param subscriptionId the subscription identifier to look up
     * @return processInstanceId string, or empty string if not found
     */
    private String getProcessInstanceId(String subscriptionId) {
        LOG.info("ProvisioningResultEventListener:: getProcessInstanceId method started for subscriptionId :: {}", subscriptionId);
        try {
            Query query = new Query(Criteria.where("subscriptionId").is(subscriptionId));
            ProcessInstanceDetails details = mongoTemplate.findOne(
                    query, ProcessInstanceDetails.class, Constants.COLLECTION_PROCESS_INSTANCE_DETAILS);
            if (details != null) {
                return details.getProcessInstanceId() != null ? details.getProcessInstanceId() : "";
            }
        } catch (Exception e) {
            LOG.error("Exception in getProcessInstanceId: ", e);
        }
        return "";
    }

    /**
     * Updates the subscription status in MongoDB for the given subscriptionId.
     *
     * @param subscriptionId the subscription identifier to update
     * @param status         the new status value
     */
    private void updateProcessInstanceStatus(String subscriptionId, String status) {
        LOG.info("ProvisioningResultEventListener:: updateProcessInstanceStatus method started for subscriptionId :: {}", subscriptionId);
        try {
            Query query = new Query(Criteria.where("subscriptionId").is(subscriptionId));
            Update update = new Update()
                    .set("status", status != null ? status : "")
                    .set("updatedAt", LocalDateTime.now());
            mongoTemplate.updateFirst(query, update, Constants.COLLECTION_PROCESS_INSTANCE_DETAILS);
        } catch (Exception e) {
            LOG.error("Exception in updateProcessInstanceStatus: ", e);
        }
    }
}
