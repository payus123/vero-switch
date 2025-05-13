package com.vero.coreprocessor.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;

import static com.vero.coreprocessor.rabbitmq.RabbitMqConstants.*;


@Configuration
public class TransactionQueueConfig {


    @Bean
    public Queue TransactionQueue() {
        return new Queue(TRANSACTION_QUEUE);
    }

    @Bean
    public TopicExchange TransactionExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE);
    }

    @Bean
    public Binding TransactionBinding() {
        return BindingBuilder.bind(TransactionQueue()).to(TransactionExchange()).with(TRANSACTION_ROUTING_KEY);
    }

}
