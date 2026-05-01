package com.mohit.subscription.workflow.service;

import com.mohit.subscription.workflow.constants.Constants;
import com.mohit.subscription.workflow.model.ProcessInstanceDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SubscriptionQueryService class - Provides MongoDB queries for subscription history
 * and process instance details
 *
 * @author mohit
 */
@Service
public class SubscriptionQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionQueryService.class);

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * Retrieves the ProcessInstanceDetails for a given subscriptionId
     *
     * @param subscriptionId the subscription identifier to search for
     * @return ProcessInstanceDetails or null if not found
     */
    public ProcessInstanceDetails getProcessInstanceBySubscriptionId(String subscriptionId) {
        LOG.info("SubscriptionQueryService:: getProcessInstanceBySubscriptionId method started");
        try {
            Query query = new Query(Criteria.where("subscriptionId").is(subscriptionId));
            return mongoTemplate.findOne(query, ProcessInstanceDetails.class,
                    Constants.COLLECTION_PROCESS_INSTANCE_DETAILS);
        } catch (Exception e) {
            LOG.error("Exception in getProcessInstanceBySubscriptionId: ", e);
            return null;
        }
    }

    /**
     * Retrieves the ProcessInstanceDetails for a given processInstanceId
     *
     * @param processInstanceId Flowable process instance identifier
     * @return ProcessInstanceDetails or null if not found
     */
    public ProcessInstanceDetails getByProcessInstanceId(String processInstanceId) {
        LOG.info("SubscriptionQueryService:: getByProcessInstanceId method started");
        try {
            Query query = new Query(Criteria.where("processInstanceId").is(processInstanceId));
            return mongoTemplate.findOne(query, ProcessInstanceDetails.class,
                    Constants.COLLECTION_PROCESS_INSTANCE_DETAILS);
        } catch (Exception e) {
            LOG.error("Exception in getByProcessInstanceId: ", e);
            return null;
        }
    }

    /**
     * Retrieves all ProcessInstanceDetails for a given customerId
     *
     * @param customerId the customer identifier
     * @return List of ProcessInstanceDetails
     */
    public List<ProcessInstanceDetails> getSubscriptionsByCustomerId(String customerId) {
        LOG.info("SubscriptionQueryService:: getSubscriptionsByCustomerId method started");
        try {
            Query query = new Query(Criteria.where("customerId").is(customerId));
            return mongoTemplate.find(query, ProcessInstanceDetails.class,
                    Constants.COLLECTION_PROCESS_INSTANCE_DETAILS);
        } catch (Exception e) {
            LOG.error("Exception in getSubscriptionsByCustomerId: ", e);
            return List.of();
        }
    }

    /**
     * Retrieves all ProcessInstanceDetails matching a given status
     *
     * @param status the subscription status to filter by
     * @return List of ProcessInstanceDetails
     */
    public List<ProcessInstanceDetails> getSubscriptionsByStatus(String status) {
        LOG.info("SubscriptionQueryService:: getSubscriptionsByStatus method started");
        try {
            Query query = new Query(Criteria.where("status").is(status));
            return mongoTemplate.find(query, ProcessInstanceDetails.class,
                    Constants.COLLECTION_PROCESS_INSTANCE_DETAILS);
        } catch (Exception e) {
            LOG.error("Exception in getSubscriptionsByStatus: ", e);
            return List.of();
        }
    }
}
