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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DemoService {

    private static final Random RANDOM = new Random();
    private RestTemplate rest;
    private boolean errorsFlag;

    public DemoService(RestTemplate rest) {
        this.rest = rest;
        this.errorsFlag = false;
    }

    public Map get() {
        Map response = new HashMap();
        response.put("My Message", "Seems like everything works! It is awesome. Life is so amazing! No errors...");
        response.put("0_0", "----------SMILE! NO ERROR!-------------");
        return response;
    }

    public void disableErrors() {
        this.errorsFlag = false;
    }

    public void enableErrors() {
        this.errorsFlag = true;
    }

    public Map delay(int seconds) {
        return rest.getForObject("https://httpbin.org/delay/" + seconds, Map.class);
    }

    public Supplier<Map> delaySupplier(int seconds) {
        return () -> this.delay(seconds);
    }

    public Supplier<Map> halfThrowsError() {
        return () -> {
            //boolean willBeError = RANDOM.nextBoolean();
            if (this.errorsFlag) {
                throw new RuntimeException("Oops... Service does not work");
            }
            return get();
        };
    }
}
