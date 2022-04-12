package com.kelmorgan.rabbitstockquoteservice.service;

import com.kelmorgan.rabbitstockquoteservice.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.rabbitmq.Receiver;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteRunner implements CommandLineRunner {

    private final QuoteMessageSender quoteMessageSender;
    private final QuoteGeneratorService service;
    private final Receiver receiver;

    @Override
    public void run(String... args) throws Exception {


        service.fetchQuoteStream(Duration.ofMillis(100))
                .take(25)
                .log("Got Quote")
                .flatMap(quoteMessageSender::sendQuoteMessage)
                .subscribe(result -> {
                    log.debug("Send Message to Rabbit");
                }, throwable -> log.error("Got Error", throwable), () -> log.debug("All Done"));

        AtomicInteger receivedCount = new AtomicInteger();

        receiver.consumeAutoAck(RabbitConfig.QUEUE)
                .log("Message Receiver")
                .subscribe(msg -> log.debug("Received Message # {} - {}", receivedCount.incrementAndGet(), new String(msg.getBody())),
                        throwable -> log.error("Error Receiving", throwable)
                        , () -> log.debug("Complete"));


    }
}
