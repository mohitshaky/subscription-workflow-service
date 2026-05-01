package com.mohit.subscription.workflow.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohit.subscription.workflow.constants.Constants;
import com.mohit.subscription.workflow.model.ProcessInstanceDetails;
import com.mohit.subscription.workflow.model.SubscriptionStatusEvent;
import lombok.AllArgsConstructor;
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
 * SubscriptionStatusEventListener class - Kafka listener for SW_SUBSCRIPTION_STATUS topic events.
 * Completes Flowable user tasks based on received subscription status events.
 *
 * @author mohit
 */
@Component
@AllArgsConstructor
@KafkaListener(topics = Constants.SUBSCRIPTION_STATUS_TOPIC,
        concurrency = "#{${subscriptionStatusConcurrency}}",
        groupId = "SW_SUBSCRIPTION_STATUS_GRP",
        containerFactory = "subscriptionStatusListenerFactory")
public class SubscriptionStatusEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionStatusEventListener.class);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    @Qualifier("primaryMongoTemplate")
    MongoTemplate mongoTemplate;

    @Autowired
    TaskService taskService;

    /**
     * Handles incoming SubscriptionStatusEvent messages from SW_SUBSCRIPTION_STATUS topic.
     * Looks up the process instance from MongoDB and completes the associated Flowable user task.
     *
     * @param subscriptionStatusEvent the deserialized SubscriptionStatusEvent payload
     */
    @KafkaHandler
    public void listen(@Payload SubscriptionStatusEvent subscriptionStatusEvent) {
        LOG.debug("SubscriptionStatusEvent listener {} {}", subscriptionStatusEvent.getSubscriptionId(), subscriptionStatusEvent.getStatus());
        LOG.info("SubscriptionStatusEventListener:: listen method started for subscriptionId :: {}", subscriptionStatusEvent.getSubscriptionId());
        try {
            String processInstanceId = getProcessInstanceId(subscriptionStatusEvent.getSubscriptionId());
            if (processInstanceId != null && !processInstanceId.isEmpty()) {
                completeAssigneeTask(processInstanceId, subscriptionStatusEvent.getStatus(), subscriptionStatusEvent);
                updateProcessInstanceStatus(subscriptionStatusEvent.getSubscriptionId(), subscriptionStatusEvent.getStatus());
            } else {
                LOG.error("Exception in listen: processInstanceId not found for subscriptionId :: {}",
                        subscriptionStatusEvent.getSubscriptionId());
            }
        } catch (Exception e) {
            LOG.error("Exception in listen: ", e);
        }
    }

    /**
     * Completes the active Flowable user task for a given processInstanceId and sets status variables.
     *
     * @param processInstanceId the Flowable process instance identifier
     * @param status            the subscription status to set on task completion
     * @param event             the full SubscriptionStatusEvent for additional variable mapping
     */
    private void completeAssigneeTask(String processInstanceId, String status, SubscriptionStatusEvent event) {
        LOG.info("SubscriptionStatusEventListener:: completeAssigneeTask method started for processInstanceId :: {}", processInstanceId);
        try {
            Task task = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .active()
                    .singleResult();
            if (task != null) {
                Map<String, Object> variables = new HashMap<>();
                variables.put(Constants.VAR_SUBSCRIPTION_STATUS, status != null ? status : "");
                variables.put(Constants.VAR_STATUS, status != null ? status : "");
                taskService.complete(task.getId(), variables);
                LOG.info("SubscriptionStatusEventListener:: task completed :: {} for processInstanceId :: {}",
                        task.getId(), processInstanceId);
            } else {
                LOG.info("SubscriptionStatusEventListener:: no active task found for processInstanceId :: {}", processInstanceId);
            }
        } catch (Exception e) {
            LOG.error("Exception in completeAssigneeTask: ", e);
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
        LOG.info("SubscriptionStatusEventListener:: getProcessInstanceId method started for subscriptionId :: {}", subscriptionId);
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
        LOG.info("SubscriptionStatusEventListener:: updateProcessInstanceStatus method started for subscriptionId :: {}", subscriptionId);
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
