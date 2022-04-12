package com.kelmorgan.rabbitstockquoteservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kelmorgan.rabbitstockquoteservice.config.RabbitConfig;
import com.kelmorgan.rabbitstockquoteservice.model.Quote;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.OutboundMessageResult;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Sender;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteMessageSender {

    private final ObjectMapper objectMapper;
    private final Sender sender;

    @SneakyThrows
    Mono<Void> sendQuoteMessage(Quote quote){
        byte[] jsonBytes = objectMapper.writeValueAsBytes(quote);
        Flux<OutboundMessageResult> confirmations = sender.sendWithPublishConfirms(
                Flux.just(new OutboundMessage("", RabbitConfig.QUEUE,jsonBytes)));

        sender.declareQueue(QueueSpecification.queue(RabbitConfig.QUEUE))
                .thenMany(confirmations)
                .doOnError(e -> log.error("Send failed",e))
                .subscribe(r -> {
                    if (r.isAck()) log.info("Message sent successfully {}", new String(r.getOutboundMessage().getBody()));
                });

        return Mono.empty();
    }
}
