package com.kelmorgan.rabbitstockquoteservice.service;


import com.kelmorgan.rabbitstockquoteservice.model.Quote;
import reactor.core.publisher.Flux;

import java.time.Duration;

public interface QuoteGeneratorService {

    Flux<Quote> fetchQuoteStream (Duration period);
}
