/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.circuitbreaker.demo.resilience4jcircuitbreakerdemo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    Logger LOG = LoggerFactory.getLogger(DemoController.class);

    private CircuitBreaker circuitBreaker;
    private DemoService httpBin;

    public DemoController(CircuitBreaker circuitBreaker, DemoService demoService) {
        this.circuitBreaker = circuitBreaker;
        this.httpBin = demoService;
    }

    @GetMapping("/get")
    public Map get() {
        return httpBin.get();
    }


    @PutMapping("/disableErrors")
    public void disableErrors() {
        httpBin.disableErrors();
    }


    @PutMapping("/enableErrors")
    public void enableErrors() {
        httpBin.enableErrors();
    }

    @GetMapping("/state")
    public Map<String, Object> state() {
        Map<String, Object> state = new HashMap<>();
        state.put("Name", circuitBreaker.getName());
        state.put("State", circuitBreaker.getState());
        state.put("Metrics", circuitBreaker.getMetrics());
        state.put("Timestamp", circuitBreaker.getTimestampUnit());
        state.put("Tags", circuitBreaker.getTags());
        state.put("Config", circuitBreaker.getCircuitBreakerConfig());
        return state;
    }

    @GetMapping("/getWithCircuitBreaker")
    public Map getWithCircuitBreaker() {
        Supplier<Map> decoratedSupplier = Decorators
                .ofSupplier(httpBin.halfThrowsError())
                .withCircuitBreaker(circuitBreaker)
                //.withRetry()
                //.withRateLimiter()
                //.withThreadPoolBulkhead()
                .withFallback(e -> Collections.singletonMap(e, "Fallback message"))
                .decorate();
        Map result = Try.ofSupplier(decoratedSupplier)
                .recover(throwable -> Collections.singletonMap(throwable, "Hello from recovery"))
                .get();
        return result;
    }
}
