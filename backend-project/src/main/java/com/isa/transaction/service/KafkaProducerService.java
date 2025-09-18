package com.isa.transaction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isa.transaction.dto.TransactionResultEvent;
import com.isa.transaction.entity.DeadLetterMessage;
import com.isa.transaction.repository.DeadLetterMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private DeadLetterMessageRepository deadLetterMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.kafka.transaction-results-topic:transaction-results}")
    private String transactionResultsTopic;

    @Value("${app.kafka.retry-attempts:3}")
    private int retryAttempts;

    // Envia un evento de resultado de transaccion a Kafka
    // Implementa logica de reintento y cola de mensajes muertos para mensajes fallidos
    public void sendTransactionResult(TransactionResultEvent event) {
        sendMessageWithRetry(transactionResultsTopic, event.getTransactionId(), event, 0);
    }

    private void sendMessageWithRetry(String topic, String key, TransactionResultEvent event, int attemptCount) {
        logger.debug("Sending transaction result to topic {} (attempt {})", topic, attemptCount + 1);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, throwable) -> {
            if (throwable == null) {
                logger.info("Transaction result sent successfully for transaction {} to topic {} at offset {}",
                           event.getTransactionId(), topic, result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send transaction result for transaction {} to topic {} (attempt {}): {}",
                            event.getTransactionId(), topic, attemptCount + 1, throwable.getMessage());

                if (attemptCount < retryAttempts - 1) {
                    logger.info("Retrying to send transaction result for transaction {} (attempt {})",
                               event.getTransactionId(), attemptCount + 2);
                    sendMessageWithRetry(topic, key, event, attemptCount + 1);
                } else {
                    logger.error("All retry attempts exhausted for transaction {}. Sending to dead letter queue.",
                                event.getTransactionId());
                    saveToDeadLetterQueue(topic, event, throwable);
                }
            }
        });
    }

    // Guarda el mensaje fallido en la cola de mensajes muertos en la base de datos
    private void saveToDeadLetterQueue(String topic, TransactionResultEvent event, Throwable error) {
        try {
            String messagePayload = objectMapper.writeValueAsString(event);
            String errorMessage = error != null ? error.getMessage() : "Unknown error";

            DeadLetterMessage deadLetterMessage = new DeadLetterMessage(topic, messagePayload, errorMessage);
            deadLetterMessageRepository.save(deadLetterMessage);

            logger.info("Transaction result for transaction {} saved to dead letter queue", event.getTransactionId());

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize transaction result event for dead letter queue: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to save transaction result to dead letter queue: {}", e.getMessage());
        }
    }

    // Envia un mensaje sin procesar a Kafka (para pruebas u otros fines)
    public void sendMessage(String topic, String key, Object message) {
        try {
            kafkaTemplate.send(topic, key, message);
            logger.debug("Message sent to topic {} with key {}", topic, key);
        } catch (Exception e) {
            logger.error("Failed to send message to topic {}: {}", topic, e.getMessage());
            throw e;
        }
    }

    // Reintenta los mensajes fallidos de la cola de mensajes muertos
    // Esto podria ser llamado por un trabajo programado o un endpoint de administracion
    public void retryDeadLetterMessages() {
        try {
            java.util.List<DeadLetterMessage> deadLetterMessages =
                deadLetterMessageRepository.findAllByOrderByCreatedDateDesc();

            logger.info("Found {} messages in dead letter queue to retry", deadLetterMessages.size());

            for (DeadLetterMessage dlm : deadLetterMessages) {
                if (dlm.getTopic().equals(transactionResultsTopic)) {
                    try {
                        TransactionResultEvent event = objectMapper.readValue(dlm.getMessagePayload(),
                                                                             TransactionResultEvent.class);

                        CompletableFuture<SendResult<String, Object>> future =
                            kafkaTemplate.send(dlm.getTopic(), event.getTransactionId(), event);

                        future.whenComplete((result, throwable) -> {
                            if (throwable == null) {
                                deadLetterMessageRepository.delete(dlm);
                                logger.info("Successfully retried dead letter message {} for transaction {}",
                                           dlm.getId(), event.getTransactionId());
                            } else {
                                logger.warn("Failed to retry dead letter message {} for transaction {}: {}",
                                           dlm.getId(), event.getTransactionId(), throwable.getMessage());
                            }
                        });

                    } catch (JsonProcessingException e) {
                        logger.error("Failed to deserialize dead letter message {}: {}", dlm.getId(), e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Failed to retry dead letter messages: {}", e.getMessage());
        }
    }

    // Obtiene el numero de mensajes en la cola de mensajes muertos
    public long getDeadLetterQueueSize() {
        return deadLetterMessageRepository.count();
    }

    // Obtiene el numero de mensajes en la cola de mensajes muertos para un tema especifico
    public long getDeadLetterQueueSize(String topic) {
        return deadLetterMessageRepository.countByTopic(topic);
    }
}
