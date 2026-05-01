package com.mohit.subscription.workflow.service;

import com.mohit.subscription.workflow.constants.Constants;
import com.mohit.subscription.workflow.request.SubscriptionRequest;
import com.mohit.subscription.workflow.response.SubscriptionResponse;
import com.mohit.subscription.workflow.response.SuccessResponse;
import com.mohit.subscription.workflow.wrapper.ISubscriptionWorkflowWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * SubscriptionService class - Thin service layer that delegates subscription workflow operations
 * to the wrapper
 *
 * @author mohit
 */
@Service
public class SubscriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionService.class);

    @Autowired
    private ISubscriptionWorkflowWrapper subscriptionWorkflowWrapper;

    /**
     * Starts a new subscription fulfillment process for the given process key
     *
     * @param processKey          BPMN process definition key
     * @param subscriptionRequest the subscription request payload
     * @param transactionId       transaction identifier header value
     * @param correlationId       correlation identifier header value
     * @return SuccessResponse wrapping the SubscriptionResponse
     */
    public SuccessResponse startSubscriptionProcess(String processKey, SubscriptionRequest subscriptionRequest,
                                                    String transactionId, String correlationId) {
        LOG.info("SubscriptionService:: startSubscriptionProcess method started");
        SubscriptionResponse subscriptionResponse = subscriptionWorkflowWrapper.startSubscriptionProcess(
                processKey, subscriptionRequest, transactionId, correlationId);
        return SuccessResponse.builder()
                .status("SUCCESS")
                .message(Constants.MSG_SUBSCRIPTION_STARTED)
                .data(subscriptionResponse)
                .build();
    }

    /**
     * Completes a Flowable user task by taskId
     *
     * @param taskId    Flowable task identifier
     * @param variables variables to pass on task completion
     * @return SuccessResponse confirming task completion
     */
    public SuccessResponse completeTask(String taskId, Map<String, Object> variables) {
        LOG.info("SubscriptionService:: completeTask method started");
        subscriptionWorkflowWrapper.completeTask(taskId, variables);
        return SuccessResponse.builder()
                .status("SUCCESS")
                .message(Constants.MSG_TASK_COMPLETED)
                .data(null)
                .build();
    }

    /**
     * Sends a signal to a running Flowable process instance
     *
     * @param processInstanceId Flowable process instance identifier
     * @param signalName        name of the signal event
     * @param variables         variables to attach to the signal
     * @return SuccessResponse confirming signal was sent
     */
    public SuccessResponse sendSignalToProcess(String processInstanceId, String signalName,
                                               Map<String, Object> variables) {
        LOG.info("SubscriptionService:: sendSignalToProcess method started");
        subscriptionWorkflowWrapper.sendSignalToProcess(processInstanceId, signalName, variables);
        return SuccessResponse.builder()
                .status("SUCCESS")
                .message(Constants.MSG_SIGNAL_SENT)
                .data(null)
                .build();
    }
}
