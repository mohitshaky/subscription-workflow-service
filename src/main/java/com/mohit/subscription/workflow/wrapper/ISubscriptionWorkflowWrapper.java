package com.mohit.subscription.workflow.wrapper;

import com.mohit.subscription.workflow.request.SubscriptionRequest;
import com.mohit.subscription.workflow.response.SubscriptionResponse;

import java.util.Map;

/**
 * ISubscriptionWorkflowWrapper interface - Contract for Flowable workflow operations
 *
 * @author mohit
 */
public interface ISubscriptionWorkflowWrapper {

    /**
     * Starts a new Flowable process instance for the given process key
     *
     * @param processKey         BPMN process definition key
     * @param subscriptionRequest the subscription request payload
     * @param transactionId      transaction identifier header value
     * @param correlationId      correlation identifier header value
     * @return SubscriptionResponse containing processInstanceId and status
     */
    SubscriptionResponse startSubscriptionProcess(String processKey, SubscriptionRequest subscriptionRequest,
                                                  String transactionId, String correlationId);

    /**
     * Completes a Flowable user task by taskId with optional variables
     *
     * @param taskId    Flowable task identifier
     * @param variables variables to set on task completion
     */
    void completeTask(String taskId, Map<String, Object> variables);

    /**
     * Sends a named signal to a running process instance
     *
     * @param processInstanceId Flowable process instance identifier
     * @param signalName        name of the signal event to trigger
     * @param variables         variables to attach to the signal
     */
    void sendSignalToProcess(String processInstanceId, String signalName, Map<String, Object> variables);
}
