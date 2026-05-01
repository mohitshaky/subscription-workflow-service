package com.mohit.subscription.workflow.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * KafkaConsumerConfig class - Kafka consumer factory configuration for subscription workflow service
 *
 * @author mohit
 */
@Configuration
public class KafkaConsumerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Creates a ConsumerFactory for SubscriptionStatusEvent messages
     *
     * @return ConsumerFactory configured for JSON deserialization
     */
    @Bean
    public ConsumerFactory<String, Object> subscriptionStatusConsumerFactory() {
        LOG.info("KafkaConsumerConfig:: subscriptionStatusConsumerFactory method started");
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.mohit.subscription.workflow.model");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.mohit.subscription.workflow.model.SubscriptionStatusEvent");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Creates a ConcurrentKafkaListenerContainerFactory for subscription status topic
     *
     * @return ConcurrentKafkaListenerContainerFactory
     */
    @Bean(name = "subscriptionStatusListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> subscriptionStatusListenerFactory() {
        LOG.info("KafkaConsumerConfig:: subscriptionStatusListenerFactory method started");
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(subscriptionStatusConsumerFactory());
        return factory;
    }

    /**
     * Creates a ConsumerFactory for ProvisioningResultEvent messages
     *
     * @return ConsumerFactory configured for JSON deserialization
     */
    @Bean
    public ConsumerFactory<String, Object> provisioningResultConsumerFactory() {
        LOG.info("KafkaConsumerConfig:: provisioningResultConsumerFactory method started");
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.mohit.subscription.workflow.model");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.mohit.subscription.workflow.model.ProvisioningResultEvent");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Creates a ConcurrentKafkaListenerContainerFactory for provisioning result topic
     *
     * @return ConcurrentKafkaListenerContainerFactory
     */
    @Bean(name = "provisioningResultListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> provisioningResultListenerFactory() {
        LOG.info("KafkaConsumerConfig:: provisioningResultListenerFactory method started");
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(provisioningResultConsumerFactory());
        return factory;
    }
}
