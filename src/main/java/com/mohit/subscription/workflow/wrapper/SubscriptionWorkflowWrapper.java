package com.mohit.subscription.workflow.wrapper;

import com.mohit.subscription.workflow.constants.Constants;
import com.mohit.subscription.workflow.exception.SubscriptionWorkflowException;
import com.mohit.subscription.workflow.model.ProcessInstanceDetails;
import com.mohit.subscription.workflow.request.SubscriptionRequest;
import com.mohit.subscription.workflow.response.SubscriptionResponse;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * SubscriptionWorkflowWrapper class - Implements ISubscriptionWorkflowWrapper,
 * wraps Flowable RuntimeService and TaskService
 *
 * @author mohit
 */
@Component
public class SubscriptionWorkflowWrapper implements ISubscriptionWorkflowWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionWorkflowWrapper.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * Starts a new Flowable process instance for the given process key
     *
     * @param processKey          BPMN process definition key
     * @param subscriptionRequest the subscription request payload
     * @param transactionId       transaction identifier
     * @param correlationId       correlation identifier
     * @return SubscriptionResponse with processInstanceId and initial status
     */
    @Override
    public SubscriptionResponse startSubscriptionProcess(String processKey, SubscriptionRequest subscriptionRequest,
                                                         String transactionId, String correlationId) {
        LOG.info("SubscriptionWorkflowWrapper:: startSubscriptionProcess method started");
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put(Constants.VAR_SUBSCRIPTION_ID, subscriptionRequest.getSubscriptionId() != null ? subscriptionRequest.getSubscriptionId() : "");
            variables.put(Constants.VAR_CUSTOMER_ID, subscriptionRequest.getCustomerId() != null ? subscriptionRequest.getCustomerId() : "");
            variables.put(Constants.VAR_TENANT_ID, subscriptionRequest.getTenantId() != null ? subscriptionRequest.getTenantId() : "");
            variables.put(Constants.VAR_SUBSCRIPTION_STATUS, Constants.SUBSCRIPTION_STATUS_RECEIVED);
            variables.put(Constants.HEADER_TRANSACTION_ID, transactionId != null ? transactionId : "");
            variables.put(Constants.HEADER_CORRELATION_ID, correlationId != null ? correlationId : "");

            if (subscriptionRequest.getAdditionalVariables() != null) {
                variables.putAll(subscriptionRequest.getAdditionalVariables());
            }

            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey, variables);
            LOG.info("SubscriptionWorkflowWrapper:: Process instance started :: {}", processInstance.getId());

            ProcessInstanceDetails details = ProcessInstanceDetails.builder()
                    .subscriptionId(subscriptionRequest.getSubscriptionId())
                    .processInstanceId(processInstance.getId())
                    .processKey(processKey)
                    .status(Constants.SUBSCRIPTION_STATUS_RECEIVED)
                    .customerId(subscriptionRequest.getCustomerId())
                    .tenantId(subscriptionRequest.getTenantId())
                    .transactionId(transactionId)
                    .correlationId(correlationId)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            mongoTemplate.save(details, Constants.COLLECTION_PROCESS_INSTANCE_DETAILS);

            SubscriptionResponse response = new SubscriptionResponse();
            response.setSubscriptionId(subscriptionRequest.getSubscriptionId());
            response.setProcessInstanceId(processInstance.getId());
            response.setStatus(Constants.SUBSCRIPTION_STATUS_RECEIVED);
            response.setMessage(Constants.MSG_SUBSCRIPTION_STARTED);
            response.setTransactionId(transactionId);
            response.setCorrelationId(correlationId);
            return response;
        } catch (Exception e) {
            LOG.error("Exception in startSubscriptionProcess: ", e);
            throw new SubscriptionWorkflowException("Failed to start subscription process: " + e.getMessage(),
                    e, HttpStatus.INTERNAL_SERVER_ERROR, Constants.ERR_PROCESS_START_FAILED);
        }
    }

    /**
     * Completes a Flowable user task by taskId with optional variables
     *
     * @param taskId    Flowable task identifier
     * @param variables variables to set on task completion
     */
    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {
        LOG.info("SubscriptionWorkflowWrapper:: completeTask method started for taskId :: {}", taskId);
        try {
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                LOG.error("Exception in completeTask: task not found for taskId :: {}", taskId);
                throw new SubscriptionWorkflowException("Task not found for taskId: " + taskId,
                        HttpStatus.NOT_FOUND, Constants.ERR_TASK_COMPLETE_FAILED);
            }
            taskService.complete(taskId, variables != null ? variables : new HashMap<>());
            LOG.info("SubscriptionWorkflowWrapper:: Task completed successfully :: {}", taskId);
        } catch (SubscriptionWorkflowException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Exception in completeTask: ", e);
            throw new SubscriptionWorkflowException("Failed to complete task: " + e.getMessage(),
                    e, HttpStatus.INTERNAL_SERVER_ERROR, Constants.ERR_TASK_COMPLETE_FAILED);
        }
    }

    /**
     * Sends a named signal to a running process instance
     *
     * @param processInstanceId Flowable process instance identifier
     * @param signalName        name of the signal event
     * @param variables         variables to attach to the signal
     */
    @Override
    public void sendSignalToProcess(String processInstanceId, String signalName, Map<String, Object> variables) {
        LOG.info("SubscriptionWorkflowWrapper:: sendSignalToProcess method started for processInstanceId :: {}", processInstanceId);
        try {
            runtimeService.signalEventReceived(signalName, processInstanceId,
                    variables != null ? variables : new HashMap<>());
            LOG.info("SubscriptionWorkflowWrapper:: Signal sent successfully :: {} to processInstance :: {}", signalName, processInstanceId);
        } catch (Exception e) {
            LOG.error("Exception in sendSignalToProcess: ", e);
            throw new SubscriptionWorkflowException("Failed to send signal: " + e.getMessage(),
                    e, HttpStatus.INTERNAL_SERVER_ERROR, Constants.ERR_SIGNAL_FAILED);
        }
    }
}
