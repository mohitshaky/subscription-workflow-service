package com.mohit.subscription.workflow.constants;

/**
 * Constants class - holds all application-level string constants
 *
 * @author mohit
 */
public final class Constants {

    private Constants() {
        // utility class
    }

    // Kafka topics
    public static final String SUBSCRIPTION_STATUS_TOPIC = "SW_SUBSCRIPTION_STATUS";
    public static final String PROVISIONING_RESULT_TOPIC = "SW_PROVISIONING_RESULT";
    public static final String VALIDATION_STATUS_TOPIC = "SW_VALIDATION_STATUS";
    public static final String NOTIFICATION_TOPIC = "SW_NOTIFICATION";

    // Subscription status values
    public static final String SUBSCRIPTION_STATUS_RECEIVED = "RECEIVED";
    public static final String SUBSCRIPTION_STATUS_VALIDATED = "VALIDATED";
    public static final String SUBSCRIPTION_STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String SUBSCRIPTION_STATUS_PROVISIONED = "PROVISIONED";
    public static final String SUBSCRIPTION_STATUS_COMPLETED = "COMPLETED";
    public static final String SUBSCRIPTION_STATUS_FAILED = "FAILED";
    public static final String SUBSCRIPTION_STATUS_CANCELLED = "CANCELLED";

    // HTTP header names
    public static final String HEADER_TRANSACTION_ID = "transactionId";
    public static final String HEADER_CORRELATION_ID = "correlationId";
    public static final String HEADER_SOURCE_CHANNEL = "sourceChannel";
    public static final String HEADER_TENANT_ID = "tenantId";

    // Flowable process variables
    public static final String VAR_SUBSCRIPTION_ID = "subscriptionId";
    public static final String VAR_SUBSCRIPTION_STATUS = "subscriptionStatus";
    public static final String VAR_CUSTOMER_ID = "customerId";
    public static final String VAR_PROCESS_INSTANCE_ID = "processInstanceId";
    public static final String VAR_STATUS = "status";
    public static final String VAR_TENANT_ID = "tenantId";

    // MongoDB collections
    public static final String COLLECTION_PROCESS_INSTANCE_DETAILS = "process_instance_details";

    // Kafka consumer groups
    public static final String SUBSCRIPTION_STATUS_GRP = "SW_SUBSCRIPTION_STATUS_GRP";
    public static final String PROVISIONING_RESULT_GRP = "SW_PROVISIONING_RESULT_GRP";

    // Kafka container factories
    public static final String SUBSCRIPTION_STATUS_LISTENER_FACTORY = "subscriptionStatusListenerFactory";
    public static final String PROVISIONING_RESULT_LISTENER_FACTORY = "provisioningResultListenerFactory";

    // Flowable process keys
    public static final String PROCESS_KEY_SUBSCRIPTION_FULFILLMENT = "subscriptionFulfillmentProcess";

    // Response messages
    public static final String MSG_SUBSCRIPTION_STARTED = "Subscription process started successfully";
    public static final String MSG_TASK_COMPLETED = "Task completed successfully";
    public static final String MSG_SIGNAL_SENT = "Signal sent successfully";

    // Error codes
    public static final String ERR_SUBSCRIPTION_NOT_FOUND = "SW-001";
    public static final String ERR_PROCESS_START_FAILED = "SW-002";
    public static final String ERR_TASK_COMPLETE_FAILED = "SW-003";
    public static final String ERR_SIGNAL_FAILED = "SW-004";
    public static final String ERR_INTERNAL_ERROR = "SW-500";
}
